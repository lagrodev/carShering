-- Создаём новые таблицы
CREATE TABLE car_rental.brands (
                                   id BIGSERIAL PRIMARY KEY,
                                   name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE car_rental.models (
                                   id BIGSERIAL PRIMARY KEY,
                                   name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE car_rental.car_classes (
                                        id BIGSERIAL PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL UNIQUE
);

-- Заполняем уникальными значениями из старой таблицы (если есть данные)
INSERT INTO car_rental.brands (name)
SELECT DISTINCT brand FROM car_rental.car_model WHERE brand IS NOT NULL;

INSERT INTO car_rental.models (name)
SELECT DISTINCT model FROM car_rental.car_model WHERE model IS NOT NULL;

INSERT INTO car_rental.car_classes (name)
SELECT DISTINCT car_class FROM car_rental.car_model WHERE car_class IS NOT NULL;

-- Добавляем новые столбцы-ссылки
ALTER TABLE car_rental.car_model
    ADD COLUMN brand_id BIGINT,
    ADD COLUMN model_id BIGINT,
    ADD COLUMN car_class_id BIGINT;

-- Обновляем ссылки на основе имён
UPDATE car_rental.car_model cm
SET
    brand_id = b.id
FROM car_rental.brands b
WHERE b.name = cm.brand;

UPDATE car_rental.car_model cm
SET
    model_id = m.id
FROM car_rental.models m
WHERE m.name = cm.model;

UPDATE car_rental.car_model cm
SET
    car_class_id = cc.id
FROM car_rental.car_classes cc
WHERE cc.name = cm.car_class;

-- Делаем столбцы обязательными (если все строки обновились успешно)
ALTER TABLE car_rental.car_model
    ALTER COLUMN brand_id SET NOT NULL,
    ALTER COLUMN model_id SET NOT NULL;

-- Удаляем старые текстовые столбцы
ALTER TABLE car_rental.car_model
    DROP COLUMN brand,
    DROP COLUMN model,
    DROP COLUMN car_class;

-- Добавляем внешние ключи
ALTER TABLE car_rental.car_model
    ADD CONSTRAINT fk_car_model_brand FOREIGN KEY (brand_id) REFERENCES car_rental.brands(id),
    ADD CONSTRAINT fk_car_model_model FOREIGN KEY (model_id) REFERENCES car_rental.models(id),
    ADD CONSTRAINT fk_car_model_car_class FOREIGN KEY (car_class_id) REFERENCES car_rental.car_classes(id);