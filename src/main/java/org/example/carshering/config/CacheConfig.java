package org.example.carshering.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация кэширования для справочников
 *
 * СПРАВОЧНИКИ (кэшируются):
 * - brands - список всех брендов
 * - brandByName - поиск бренда по имени
 * - models - список моделей (если добавите)
 * - carClasses - список классов автомобилей (если добавите)
 *
 * ПОЧЕМУ КЭШИРУЕМ:
 * 1. Данные редко изменяются (новый бренд раз в месяц)
 * 2. Читаются очень часто (при каждом запросе машин)
 * 3. Небольшой объем данных (пара сотен записей)
 *
 * КОГДА СБРАСЫВАЕТСЯ КЭШ:
 * - При добавлении нового бренда
 * - При обновлении бренда
 * - Можно добавить scheduled task для очистки раз в час
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * Simple in-memory cache manager
     *
     * Для production рекомендуется:
     * - Redis (распределенный кэш)
     * - Caffeine (более продвинутый in-memory кэш с TTL)
     * - Ehcache (enterprise-grade кэш)
     */
    @Bean
    public CacheManager cacheManager() {
        log.info("Initializing cache manager for reference data");
        
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                "brands",           // Все бренды
                "brandByName",      // Поиск бренда по имени
                "modelNames",       // Все названия моделей (Camry, Corolla и т.д.)
                "modelNameByName",  // Поиск названия модели по имени
                "carClasses",       // Все классы автомобилей (Economy, Business и т.д.)
                "carClassByName"    // Поиск класса по имени
        );
        
        return cacheManager;
    }
}

