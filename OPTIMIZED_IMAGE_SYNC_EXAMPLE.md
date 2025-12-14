# Оптимизированная синхронизация изображений

## Проблема текущей реализации

**Текущий подход: DELETE ALL + INSERT ALL**

```java
if (carDomain.getId() != null) {
    imageRepository.deleteByCarId(carId);  // Удаляем ВСЕ
}
List<ImageData> savedImages = saveImages(savedCarId, carDomain.getImages()); // Вставляем ВСЕ заново
```

**Почему так сделано:**
- `ImageData` - это Value Object БЕЗ ID
- JPA не может определить, какие изображения новые, а какие существующие
- Самый простой способ - удалить всё и вставить заново

**Минусы:**
- Лишние DELETE запросы
- Лишние INSERT запросы  
- Потеря ID изображений (после re-insert у них будут новые ID)
- Плохая производительность при большом количестве изображений

---

## Решение 1: Умная синхронизация (Smart Sync)

### Идея
Сравниваем старые и новые изображения по содержимому (URL + fileName) и:
- Удаляем только те, которых нет в новом списке
- Добавляем только новые
- Не трогаем те, что не изменились

### Реализация

```java
@Repository
@RequiredArgsConstructor
public class CarRepositoryAdapter implements CarDomainRepository {

    private final CarRepository carRepository;
    private final ImageRepository imageRepository;
    private final CarMapperForDomain mapper;

    @Override
    @Transactional
    public CarDomain save(CarDomain carDomain) {
        Car entity = mapper.toEntity(carDomain);
        Car savedEntity = carRepository.save(entity);
        
        Long carId = savedEntity.getId();
        CarId savedCarId = new CarId(carId);
        
        // Умная синхронизация изображений
        List<ImageData> savedImages = smartSyncImages(savedCarId, carDomain.getImages());
        
        return mapper.toDomain(savedEntity, savedImages);
    }

    /**
     * Умная синхронизация изображений
     * Сравнивает по URL и fileName, обновляет только разницу
     */
    private List<ImageData> smartSyncImages(CarId carId, List<ImageData> newImages) {
        // Случай 1: Новый список пустой - удаляем все изображения
        if (newImages == null || newImages.isEmpty()) {
            imageRepository.deleteByCarId(carId.value());
            return List.of();
        }
        
        // Случай 2: Загружаем текущие изображения из БД
        List<Image> existingImages = imageRepository.findByCarId(carId.value());
        
        // Случай 3: В БД нет изображений - просто добавляем все новые
        if (existingImages.isEmpty()) {
            return saveAllImages(carId, newImages);
        }
        
        // Случай 4: Есть и старые, и новые - делаем diff
        Set<ImageData> existingSet = existingImages.stream()
                .map(this::toImageData)
                .collect(Collectors.toSet());
        
        Set<ImageData> newSet = new HashSet<>(newImages);
        
        // Находим изображения на удаление (есть в БД, но нет в новом списке)
        List<Long> idsToDelete = existingImages.stream()
                .filter(img -> !newSet.contains(toImageData(img)))
                .map(Image::getId)
                .toList();
        
        // Находим изображения на добавление (нет в БД, но есть в новом списке)
        List<ImageData> imagesToAdd = newImages.stream()
                .filter(img -> !existingSet.contains(img))
                .toList();
        
        // Удаляем устаревшие
        if (!idsToDelete.isEmpty()) {
            imageRepository.deleteAllById(idsToDelete);
        }
        
        // Добавляем новые
        if (!imagesToAdd.isEmpty()) {
            List<Image> newEntities = imagesToAdd.stream()
                    .map(imageData -> toImageEntity(carId, imageData))
                    .toList();
            imageRepository.saveAll(newEntities);
        }
        
        // Возвращаем актуальный список из БД
        return imageRepository.findByCarId(carId.value())
                .stream()
                .map(this::toImageData)
                .toList();
    }

    private List<ImageData> saveAllImages(CarId carId, List<ImageData> images) {
        List<Image> imageEntities = images.stream()
                .map(imageData -> toImageEntity(carId, imageData))
                .toList();
        
        return imageRepository.saveAll(imageEntities)
                .stream()
                .map(this::toImageData)
                .toList();
    }

    private ImageData toImageData(Image entity) {
        return ImageData.create(entity.getFileName(), entity.getUrl());
    }

    private Image toImageEntity(CarId carId, ImageData imageData) {
        return Image.builder()
                .car(carId)
                .fileName(imageData.fileName())
                .url(imageData.url())
                .build();
    }
}
```

### Производительность

**Было (DELETE ALL + INSERT ALL):**
- 5 изображений в БД, обновляем 1 → **5 DELETE + 5 INSERT = 10 запросов**

**Стало (Smart Sync):**
- 5 изображений в БД, обновляем 1 → **1 SELECT + 4 DELETE + 1 INSERT = 6 запросов**
- Если не изменилось ничего → **1 SELECT + 0 операций**

### Важное замечание

**ImageData должен правильно реализовать equals() и hashCode():**

```java
public record ImageData(
    FileName fileName,
    ImageUrl url
) {
    // Record автоматически генерирует equals() и hashCode() на основе всех полей
    // Это значит, что два ImageData равны, если у них одинаковые fileName и url
}
```

Если у `FileName` и `ImageUrl` правильно реализованы equals/hashCode, то всё работает!

---

## Решение 2: Разделение обновления Car и Images

### Идея
Не смешивать обновление автомобиля и изображений в одном методе

### Реализация

#### Repository
```java
public interface CarDomainRepository {
    // Сохранение БЕЗ изображений
    CarDomain save(CarDomain car);
    
    // Отдельное управление изображениями
    void replaceImages(CarId carId, List<ImageData> images);
    void addImages(CarId carId, List<ImageData> images);
    void removeImages(CarId carId, List<ImageData> images);
}
```

```java
@Repository
@RequiredArgsConstructor
public class CarRepositoryAdapter implements CarDomainRepository {
    
    @Override
    @Transactional
    public CarDomain save(CarDomain carDomain) {
        Car entity = mapper.toEntity(carDomain);
        Car savedEntity = carRepository.save(entity);
        
        // НЕ сохраняем изображения здесь!
        return mapper.toDomain(savedEntity, List.of());
    }

    @Override
    @Transactional
    public void replaceImages(CarId carId, List<ImageData> images) {
        // DELETE ALL + INSERT ALL (но явно вызывается только когда нужно)
        imageRepository.deleteByCarId(carId.value());
        
        if (images != null && !images.isEmpty()) {
            List<Image> imageEntities = images.stream()
                    .map(img -> toImageEntity(carId, img))
                    .toList();
            imageRepository.saveAll(imageEntities);
        }
    }

    @Override
    @Transactional
    public void addImages(CarId carId, List<ImageData> images) {
        if (images == null || images.isEmpty()) return;
        
        List<Image> imageEntities = images.stream()
                .map(img -> toImageEntity(carId, img))
                .toList();
        imageRepository.saveAll(imageEntities);
    }

    @Override
    @Transactional
    public void removeImages(CarId carId, List<ImageData> imagesToRemove) {
        if (imagesToRemove == null || imagesToRemove.isEmpty()) return;
        
        // Находим ID изображений в БД по URL
        List<Image> existingImages = imageRepository.findByCarId(carId.value());
        Set<ImageData> toRemove = new HashSet<>(imagesToRemove);
        
        List<Long> idsToDelete = existingImages.stream()
                .filter(img -> toRemove.contains(toImageData(img)))
                .map(Image::getId)
                .toList();
        
        if (!idsToDelete.isEmpty()) {
            imageRepository.deleteAllById(idsToDelete);
        }
    }
    
    // ...остальное
}
```

#### Application Service
```java
@Service
@RequiredArgsConstructor
public class CarApplicationService {
    
    private final CarDomainRepository carRepository;

    /**
     * Обновить основные данные машины (без изображений)
     */
    @Transactional
    public CarDto updateCar(UpdateCarCommand command) {
        CarDomain car = carRepository.findById(new CarId(command.carId()));
        
        car.updateDailyRate(command.dailyRate());
        car.updateGosNumber(command.gosNumber());
        // ...другие обновления
        
        CarDomain saved = carRepository.save(car); // БЕЗ изображений
        
        return mapper.toDto(saved);
    }

    /**
     * Заменить ВСЕ изображения машины
     */
    @Transactional
    public void replaceCarImages(Long carId, List<ImageDto> images) {
        CarId id = new CarId(carId);
        List<ImageData> imageData = images.stream()
                .map(dto -> ImageData.create(
                    new FileName(dto.fileName()),
                    new ImageUrl(dto.url())
                ))
                .toList();
        
        carRepository.replaceImages(id, imageData);
    }

    /**
     * Добавить новые изображения (не удаляя старые)
     */
    @Transactional
    public void addCarImages(Long carId, List<ImageDto> images) {
        CarId id = new CarId(carId);
        List<ImageData> imageData = images.stream()
                .map(dto -> ImageData.create(
                    new FileName(dto.fileName()),
                    new ImageUrl(dto.url())
                ))
                .toList();
        
        carRepository.addImages(id, imageData);
    }

    /**
     * Удалить конкретные изображения
     */
    @Transactional
    public void removeCarImages(Long carId, List<String> imageUrls) {
        CarId id = new CarId(carId);
        List<ImageData> imagesToRemove = imageUrls.stream()
                .map(url -> {
                    // Находим изображение по URL
                    // В реальности лучше передавать ID изображений
                    return ImageData.create(
                        new FileName(""), // Не важно для поиска
                        new ImageUrl(url)
                    );
                })
                .toList();
        
        carRepository.removeImages(id, imagesToRemove);
    }
}
```

### Плюсы подхода
- ✅ Явное управление - понятно, когда изображения обновляются
- ✅ Гибкость - можно добавлять/удалять без полной замены
- ✅ Производительность - обновляем только то, что изменилось
- ✅ Разделение ответственности

### Минусы
- ❌ Больше кода
- ❌ Нужно следить, чтобы изображения синхронизировались явно

---

## Решение 3: Добавить ID в ImageData (НЕ рекомендуется)

### НЕ делай так!

```java
// ❌ ПЛОХО - ImageData перестаёт быть Value Object
public record ImageData(
    Long id,  // <- Идентификатор сущности!
    FileName fileName,
    ImageUrl url
) {}
```

**Почему плохо:**
- Value Object НЕ должен иметь идентичности
- Нарушение принципов DDD
- ImageData становится Entity, но в Domain слое

**Если очень нужен ID:**
Сделай отдельную Entity в Domain:

```java
// Domain Entity
public class CarImage {
    private final ImageId id;  // Value Object ID
    private FileName fileName;
    private ImageUrl url;
    
    // ...бизнес-логика
}
```

Но это усложняет модель без реальной пользы в данном случае.

---

## Мои рекомендации

### Для вашего проекта (каршеринг):

**Используйте Решение 1 (Smart Sync)**, потому что:
1. ✅ Изображений обычно 3-10 штук на машину
2. ✅ Обновления редкие (машины добавляются, но не меняются часто)
3. ✅ Оптимально по производительности и сложности
4. ✅ Прозрачно - всё в одном методе `save()`

### Когда использовать Решение 2:

Если вам нужен отдельный API для управления изображениями:
```
POST /api/cars/{id}/images        - добавить изображения
DELETE /api/cars/{id}/images      - удалить конкретные изображения
PUT /api/cars/{id}/images         - заменить все изображения
```

### Когда текущее решение (DELETE ALL + INSERT ALL) приемлемо:

- MVP / прототип
- Изображений мало (1-3)
- Обновления редкие
- Производительность не критична

---

## Код для замены в CarRepositoryAdapter

Замените метод `save()` на версию со Smart Sync:

```java
@Override
@Transactional
public CarDomain save(CarDomain carDomain) {
    Car entity = mapper.toEntity(carDomain);
    Car savedEntity = carRepository.save(entity);
    
    Long carId = savedEntity.getId();
    CarId savedCarId = new CarId(carId);
    
    // Умная синхронизация вместо DELETE ALL + INSERT ALL
    List<ImageData> savedImages = smartSyncImages(savedCarId, carDomain.getImages());
    
    return mapper.toDomain(savedEntity, savedImages);
}

private List<ImageData> smartSyncImages(CarId carId, List<ImageData> newImages) {
    if (newImages == null || newImages.isEmpty()) {
        imageRepository.deleteByCarId(carId.value());
        return List.of();
    }
    
    List<Image> existingImages = imageRepository.findByCarId(carId.value());
    
    if (existingImages.isEmpty()) {
        return saveImages(carId, newImages);
    }
    
    Set<ImageData> existingSet = existingImages.stream()
            .map(this::toImageData)
            .collect(Collectors.toSet());
    
    Set<ImageData> newSet = new HashSet<>(newImages);
    
    // Удаляем те, которых нет в новом списке
    List<Long> idsToDelete = existingImages.stream()
            .filter(img -> !newSet.contains(toImageData(img)))
            .map(Image::getId)
            .toList();
    
    // Находим новые изображения
    List<ImageData> imagesToAdd = newImages.stream()
            .filter(img -> !existingSet.contains(img))
            .toList();
    
    if (!idsToDelete.isEmpty()) {
        imageRepository.deleteAllById(idsToDelete);
    }
    
    if (!imagesToAdd.isEmpty()) {
        List<Image> newEntities = imagesToAdd.stream()
                .map(imageData -> toImageEntity(carId, imageData))
                .toList();
        imageRepository.saveAll(newEntities);
    }
    
    return imageRepository.findByCarId(carId.value())
            .stream()
            .map(this::toImageData)
            .toList();
}
```

Добавьте импорт:
```java
import java.util.HashSet;
import java.util.Set;
```

---

**Удачи с оптимизацией! 🚀**

