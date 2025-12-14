package org.example.carshering.fleet.infrastructure.persistence.repository;

import org.example.carshering.fleet.infrastructure.persistence.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository для работы с изображениями автомобилей
 */
public interface ImageRepository extends JpaRepository<Image, Long> {
    
    /**
     * Найти все изображения по ID автомобиля
     * 
     * @param carId - ID автомобиля
     * @return список изображений
     */
    @Query("SELECT i FROM Image i WHERE i.car.value = :carId")
    List<Image> findByCarId(@Param("carId") Long carId);
    
    /**
     * Удалить все изображения автомобиля
     * 
     * @param carId - ID автомобиля
     */
    @Modifying
    @Query("DELETE FROM Image i WHERE i.car.value = :carId")
    void deleteByCarId(@Param("carId") Long carId);
}

