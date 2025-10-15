package org.example.carshering.repository;

import ch.qos.logback.core.model.Model;
import org.example.carshering.entity.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarModelRepository extends JpaRepository<CarModel,Long> {
}
