package org.example.carshering.fleet.domain.model;


import lombok.Getter;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.common.exceptions.custom.BusinessException;
import org.example.carshering.fleet.domain.valueobject.*;
import org.example.carshering.fleet.domain.valueobject.id.ModelId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * CarDomain - Aggregate Root для автомобиля в Fleet Context
 * Отвечает за управление состоянием автомобиля и его бизнес-логикой
 */
@Getter
public class CarDomain {
    // identifier identity
    private final CarId id;

    // (изменяемые) свойства автомобиля
    private Vin vin;              // VIN может измениться (исправление ошибки ввода)
    private GosNumber gosNumber;  // Гос номер может измениться (перерегистрация)
    private Year yearOfIssue;     // Год выпуска может измениться (исправление ошибки ввода)
    private ModelId modelId;      // Модель может измениться (исправление ошибки ввода)

    private Money dailyRate;      // Цена аренды в день
    private CarStateType state;   // Текущее состояние машины
    private List<ImageData> images = new ArrayList<>();

    private CarDomain(CarId id, GosNumber gosNumber, Vin vin, Money dailyRate, Year yearOfIssue, ModelId modelId, CarStateType state) {
        this.id = id;
        this.gosNumber = gosNumber;
        this.vin = vin;
        this.dailyRate = dailyRate;
        this.yearOfIssue = yearOfIssue;
        this.modelId = modelId;
        this.state = state;
    }


    /**
     * Создать новый автомобиль
     *
     * @param gosNumber    - государственный номер
     * @param vin          - VIN номер
     * @param dailyRate    - цена аренды в день
     * @param yearOfIssue  - год выпуска
     * @param modelId      - ID модели автомобиля (обязательно!)
     * @param initialState - начальное состояние (AVAILABLE, CONFIRMED и т.д.)
     * @return новый CarDomain с указанным состоянием
     */
    public static CarDomain create(GosNumber gosNumber, Vin vin, Money dailyRate, Year yearOfIssue, ModelId modelId, CarStateType initialState) {
        // Валидация обязательных полей
        validateRequiredFields(gosNumber, vin, dailyRate, yearOfIssue, modelId);

        if (initialState == null) {
            throw new IllegalArgumentException("Initial state is required");
        }

        // Создаем машину с указанным начальным состоянием
        return new CarDomain(null, gosNumber, vin, dailyRate, yearOfIssue, modelId, initialState);
    }

    /**
     * Восстановить агрегат из БД
     *
     * @param id          - ID автомобиля (обязательно для существующих)
     * @param gosNumber   - государственный номер
     * @param vin         - VIN номер
     * @param dailyRate   - цена аренды в день
     * @param yearOfIssue - год выпуска
     * @param modelId     - ID модели автомобиля
     * @param state       - текущее состояние
     * @param imageIds    - список ID изображений
     * @return восстановленный CarDomain
     */
    /**
     * Восстановить агрегат из БД
     *
     * @param id          - ID автомобиля (обязательно для существующих)
     * @param gosNumber   - государственный номер
     * @param vin         - VIN номер
     * @param dailyRate   - цена аренды в день
     * @param yearOfIssue - год выпуска
     * @param modelId     - ID модели автомобиля
     * @param state       - текущее состояние
     * @param images      - список изображений (Value Objects)
     * @return восстановленный CarDomain
     */
    public static CarDomain restore(CarId id, GosNumber gosNumber, Vin vin,
                                    Money dailyRate, Year yearOfIssue,
                                    ModelId modelId, CarStateType state,
                                    List<ImageData> images) {
        validateRequiredFields(gosNumber, vin, dailyRate, yearOfIssue, modelId);

        if (id == null) {
            throw new IllegalArgumentException("CarId cannot be null when restoring from DB");
        }
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }

        CarDomain car = new CarDomain(id, gosNumber, vin, dailyRate, yearOfIssue, modelId, state);

        // Восстанавливаем изображения
        if (images != null && !images.isEmpty()) {
            car.images = new ArrayList<>(images);
        }

        return car;
    }


    private static void validateRequiredFields(GosNumber gosNumber, Vin vin, Money dailyRate, Year yearOfIssue, ModelId modelId) {
        if (gosNumber == null) {
            throw new IllegalArgumentException("GosNumber is required");
        }
        if (vin == null) {
            throw new IllegalArgumentException("Vin is required");
        }
        if (dailyRate == null) {
            throw new IllegalArgumentException("DailyRate is required");
        }
        if (dailyRate.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("DailyRate must be positive");
        }
        if (yearOfIssue == null) {
            throw new IllegalArgumentException("YearOfIssue is required");
        }
        if (modelId == null) {
            throw new IllegalArgumentException("ModelId is required - car must have a model");
        }
    }


    /**
     * Обновить VIN номер (например, при исправлении ошибки ввода)
     *
     * @param newVin - новый VIN
     */
    public void updateVin(Vin newVin) {
        if (newVin == null) {
            throw new IllegalArgumentException("Vin cannot be null");
        }

        // Бизнес-правило: нельзя менять VIN для машины в аренде
//        if (state == CarStateType.ACTIVE || state == CarStateType.CONFIRMED) {
//            throw new BusinessException("Cannot change VIN for car in rental. Current state: " + state);
//        } //todo Domain Event по созданию аренды, надо менять статус автомобиля? тогда тут  логику делать

        this.vin = newVin;
        // TODO: Domain Event - CarVinUpdated
    }

    /**
     * Обновить государственный номер (например, при перерегистрации)
     *
     * @param newGosNumber - новый гос номер
     */
    public void updateGosNumber(GosNumber newGosNumber) {
        if (newGosNumber == null) {
            throw new IllegalArgumentException("GosNumber cannot be null");
        }

        // Бизнес-правило: нельзя менять номер для машины в аренде
        //if (state == CarStateType.ACTIVE || state == CarStateType.CONFIRMED) {
//            throw new BusinessException("Cannot change VIN for car in rental. Current state: " + state);
//        } //todo Domain Event по созданию аренды, надо менять статус автомобиля? тогда тут  логику делать


        this.gosNumber = newGosNumber;
        // TODO: Domain Event - CarGosNumberUpdated
    }

    /**
     * Обновить год выпуска (например, при исправлении ошибки ввода)
     *
     * @param newYear - новый год выпуска
     */
    public void updateYearOfIssue(Year newYear) {
        if (newYear == null) {
            throw new IllegalArgumentException("Year cannot be null");
        }

        this.yearOfIssue = newYear;
        // TODO: Domain Event - CarYearUpdated
    }

    /**
     * Обновить модель автомобиля (например, при исправлении ошибки ввода)
     *
     * @param newModelId - новый ID модели
     */
    public void updateModel(ModelId newModelId) {
        if (newModelId == null) {
            throw new IllegalArgumentException("ModelId cannot be null");
        }

        //if (state == CarStateType.ACTIVE || state == CarStateType.CONFIRMED) {
//            throw new BusinessException("Cannot change VIN for car in rental. Current state: " + state);
//        } //todo Domain Event по созданию аренды, надо менять статус автомобиля? тогда тут  логику делать


        this.modelId = newModelId;
        // TODO: Domain Event - CarModelUpdated
    }

    /**
     * Изменить цену аренды
     *
     * @param newRate - новая цена
     */
    public void updateDailyRate(Money newRate) {
        if (newRate == null) {
            throw new IllegalArgumentException("DailyRate cannot be null");
        }


        //if (state == CarStateType.ACTIVE || state == CarStateType.CONFIRMED) {
//            throw new BusinessException("Cannot change VIN for car in rental. Current state: " + state);
//        } //todo Domain Event по созданию аренды, надо менять статус автомобиля? тогда тут  логику делать


        this.dailyRate = newRate;
        // TODO: Domain Event - CarDailyRateChanged
    }

    /**
     * Забронировать машину
     */
    public void reserve() {
        if (state != CarStateType.AVAILABLE) {
            throw new BusinessException("Can only reserve available cars. Current state: " + state);
        }

        this.state = CarStateType.CONFIRMED;
        // TODO: Domain Event - CarReserved
    }

    /**
     * Начать аренду
     */
    public void startRental() {
        if (state != CarStateType.CONFIRMED) {
            throw new BusinessException("Can only start rental for confirmed cars. Current state: " + state);
        }

        this.state = CarStateType.ACTIVE;
        // TODO: Domain Event - CarRentalStarted
    }

    /**
     * Завершить аренду и вернуть машину
     */
    public void completeRental() {
        if (state != CarStateType.ACTIVE) {
            throw new BusinessException("Can only complete active rentals. Current state: " + state);
        }

        this.state = CarStateType.CLOSED;
        // TODO: Domain Event - CarRentalCompleted
    }

    /**
     * Вернуть машину в доступные (после завершения аренды)
     */
    public void makeAvailable() {
        if (state != CarStateType.CLOSED && state != CarStateType.CANCELLED) {
            throw new BusinessException("Can only make available from closed/cancelled state. Current state: " + state);
        }

        this.state = CarStateType.AVAILABLE;
        // TODO: Domain Event - CarMadeAvailable
    }

    /**
     * Отменить бронирование
     */
    public void cancelReservation() {
        if (state != CarStateType.CONFIRMED) {
            throw new BusinessException("Can only cancel confirmed reservations. Current state: " + state);
        }

        this.state = CarStateType.CANCELLED;
        // TODO: Domain Event - CarReservationCancelled
    }

    /**
     * Пометить автомобиль как удаленный (soft delete)
     */
    public void markAsDeleted() {
        // Можно удалить автомобиль в любом состоянии, кроме активной аренды
        if (state == CarStateType.ACTIVE) {
            throw new BusinessException("Cannot delete car during active rental. Current state: " + state);
        }

        this.state = CarStateType.CLOSED;
        // TODO: Domain Event - CarDeleted
    }

    /**
     * Проверка, доступна ли машина для аренды
     */
    public boolean isAvailableForRental() {
        return state == CarStateType.AVAILABLE;
    }

    /**
     * Проверка, находится ли машина в аренде
     */
    public boolean isInRental() {
        return state == CarStateType.ACTIVE;
    }

    /**
     * Проверка, забронирована ли машина
     */
    public boolean isReserved() {
        return state == CarStateType.CONFIRMED;
    }

    // ========== РАБОТА С ИЗОБРАЖЕНИЯМИ ==========

    /**
     * Добавить изображение к автомобилю
     *
     * @param fileName - имя файла
     * @param url      - URL изображения
     */
    public void addImage(FileName fileName, ImageUrl url) {
        if (fileName == null) {
            throw new IllegalArgumentException("FileName cannot be null");
        }
        if (url == null) {
            throw new IllegalArgumentException("ImageUrl cannot be null");
        }

        // Бизнес-правило: максимум 10 изображений
        if (images.size() >= 10) {
            throw new BusinessException("Cannot add more than 10 images per car");
        }

        // Создаем Value Object
        ImageData newImage = ImageData.create(fileName, url);

        // Бизнес-правило: нельзя добавлять дубликаты (по fileName и url)
        if (images.contains(newImage)) {
            throw new BusinessException("Image with same fileName and url already exists");
        }

        images.add(newImage);
        // TODO: Domain Event - CarImageAdded
    }

    /**
     * Удалить изображение по fileName
     *
     * @param fileName - имя файла для удаления
     */
    public void removeImageByFileName(FileName fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("FileName cannot be null");
        }

        boolean removed = images.removeIf(img -> img.fileName().equals(fileName));

        if (!removed) {
            throw new BusinessException("Image not found for this car");
        }

        // TODO: Domain Event - CarImageRemoved
    }

    /**
     * Удалить все изображения
     */
    public void removeAllImages() {
        if (images.isEmpty()) {
            return; // ничего не делаем, если уже пусто
        }

        images.clear();
        // TODO: Domain Event - CarAllImagesRemoved
    }

    /**
     * Проверить, есть ли изображения
     */
    public boolean hasImages() {
        return !images.isEmpty();
    }

    /**
     * Получить количество изображений
     */
    public int getImageCount() {
        return images.size();
    }

    /**
     * Получить копию списка изображений (защита от изменения извне)
     *
     * @return копия списка изображений
     */
    public List<ImageData> getImages() {
        return new ArrayList<>(images);
    }
}
