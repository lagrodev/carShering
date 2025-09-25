-- Создаем схему
CREATE SCHEMA IF NOT EXISTS car_rental;

-- Таблица ролей
CREATE TABLE car_rental.role (
                                 id BIGSERIAL PRIMARY KEY,
                                 name VARCHAR(100) NOT NULL UNIQUE
);

-- Таблица клиентов
CREATE TABLE car_rental.client (
                                   id BIGSERIAL PRIMARY KEY,
                                   first_name VARCHAR(100) NOT NULL,
                                   second_name VARCHAR(100),
                                   phone VARCHAR(20) NOT NULL UNIQUE,
                                   mail VARCHAR(255) NOT NULL UNIQUE,
                                   login VARCHAR(100) NOT NULL UNIQUE,
                                   password VARCHAR(255) NOT NULL,
                                   role_id BIGINT REFERENCES car_rental.role(id)
);

-- Таблица документов
CREATE TABLE car_rental.document (
                                     id BIGSERIAL PRIMARY KEY,
                                     document_type VARCHAR(100),
                                     series VARCHAR(50),
                                     number VARCHAR(50),
                                     date_of_issue DATE,
                                     issuing_authority VARCHAR(255),
                                     client_id BIGINT UNIQUE NOT NULL REFERENCES car_rental.client(id)
);

-- Таблица состояний аренды
CREATE TABLE car_rental.rental_state (
                                         id BIGSERIAL PRIMARY KEY,
                                         name VARCHAR(100) NOT NULL UNIQUE
);

-- Таблица моделей машин
CREATE TABLE car_rental.car_model (
                                      id BIGSERIAL PRIMARY KEY,
                                      brand VARCHAR(100) NOT NULL,
                                      model VARCHAR(100) NOT NULL,
                                      body_type VARCHAR(50),
                                      car_class VARCHAR(50),
                                      year_of_issue INT
);

-- Таблица состояний машин
CREATE TABLE car_rental.car_state (
                                      id BIGSERIAL PRIMARY KEY,
                                      name VARCHAR(100) NOT NULL UNIQUE
);

-- Таблица машин
CREATE TABLE car_rental.car (
                                id BIGSERIAL PRIMARY KEY,
                                gos_number VARCHAR(20) NOT NULL UNIQUE,
                                vin VARCHAR(50) NOT NULL UNIQUE,
                                model_id BIGINT NOT NULL REFERENCES car_rental.car_model(id),
                                state_id BIGINT NOT NULL REFERENCES car_rental.car_state(id)
);

-- Таблица контрактов
CREATE TABLE car_rental.contract (
                                     id BIGSERIAL PRIMARY KEY,
                                     data_start DATE,
                                     data_end DATE,
                                     total_cost NUMERIC(10,2) NOT NULL,
                                     client_id BIGINT NOT NULL REFERENCES car_rental.client(id),
                                     car_id BIGINT NOT NULL REFERENCES car_rental.car(id),
                                     state_id BIGINT NOT NULL REFERENCES car_rental.rental_state(id)
);
