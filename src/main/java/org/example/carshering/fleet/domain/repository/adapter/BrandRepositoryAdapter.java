package org.example.carshering.fleet.domain.repository.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.fleet.domain.model.CarBrandDomain;
import org.example.carshering.fleet.domain.repository.BrandDomainRepository;
import org.example.carshering.fleet.domain.valueobject.id.BrandId;
import org.example.carshering.fleet.domain.valueobject.name.BrandName;
import org.example.carshering.fleet.infrastructure.persistence.entity.Brand;
import org.example.carshering.fleet.infrastructure.persistence.mapper.BrandMapper;
import org.example.carshering.fleet.infrastructure.persistence.repository.BrandRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Адаптер для работы с брендами
 *
 * ВАЖНО: Бренды - это СПРАВОЧНИК, поэтому используется кэширование!
 * - findAll() кэшируется полностью
 * - findByName() кэшируется для быстрого поиска
 * - При save() кэш инвалидируется
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class BrandRepositoryAdapter implements BrandDomainRepository {

    private final BrandRepository brandRepository;
    private final BrandMapper mapper;

    /**
     * Сохранить бренд и сбросить кэш
     */
    @Override
    @Transactional
    @CacheEvict(value = {"brands", "brandByName"}, allEntries = true)
    public CarBrandDomain save(CarBrandDomain brand) {
        log.debug("Saving brand and evicting cache: {}", brand.getName());

        Brand entity = mapper.toEntity(brand);
        Brand savedEntity = brandRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    /**
     * Найти бренд по ID (без кэша, т.к. используется редко)
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CarBrandDomain> findById(BrandId brandId) {
        return brandRepository.findById(brandId.value())
                .map(mapper::toDomain);
    }

    /**
     * Найти бренд по имени (с кэшем!)
     * Часто используется при создании/обновлении машин
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "brandByName", key = "#brandName.value()")
    public Optional<CarBrandDomain> findByName(BrandName brandName) {
        log.debug("Finding brand by name (will be cached): {}", brandName.value());

        return brandRepository.findByNameIgnoreCase(brandName.value())
                .map(mapper::toDomain);
    }

    /**
     * Получить все бренды (с кэшем!)
     * Это СПРАВОЧНИК - кэшируется полностью
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "brands")
    public List<CarBrandDomain> findAll() {
        log.debug("Loading all brands (will be cached)");

        return brandRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(BrandId brandId) {
        return brandRepository.existsById(brandId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(BrandName brandName) {
        return brandRepository.findByNameIgnoreCase(brandName.value()).isPresent();
    }
}

