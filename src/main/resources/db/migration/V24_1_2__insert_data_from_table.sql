-- 1. Сначала базовые справочники
INSERT INTO car_rental.role (name) VALUES
                                       ('CLIENT'),
                                       ('ADMIN'),
                                       ('MANAGER');

-- Исправляем car_state - делаем уникальные статусы
INSERT INTO car_rental.car_state (status) VALUES
                                                    ('AVAILABLE'),
                                                    ('RENTED'),
                                                    ('MAINTENANCE'),
                                                    ('UNAVAILABLE');

INSERT INTO car_rental.rental_state (name) VALUES
                                               ('ACTIVE'),
                                               ('COMPLETED'),
                                               ('CANCELLED'),
                                               ('PENDING');

INSERT INTO car_rental.doctype (name) VALUES
                                          ('PASSPORT'),
                                          ('DRIVER_LICENSE'),
                                          ('INTERNATIONAL_PASSPORT');

-- 2. Модели автомобилей (50 моделей)
INSERT INTO car_rental.car_model (year_of_issue, body_type, brand, car_class, model) VALUES
-- Toyota (10 моделей)
(2022, 'SEDAN', 'Toyota', 'MID_SIZE', 'Camry'),
(2023, 'SUV', 'Toyota', 'SUV', 'RAV4'),
(2022, 'HATCHBACK', 'Toyota', 'COMPACT', 'Corolla'),
(2023, 'SUV', 'Toyota', 'LARGE_SUV', 'Highlander'),
(2022, 'PICKUP', 'Toyota', 'TRUCK', 'Tacoma'),
(2023, 'MINIVAN', 'Toyota', 'VAN', 'Sienna'),
(2022, 'HYBRID', 'Toyota', 'ECO', 'Prius'),
(2023, 'COUPE', 'Toyota', 'SPORTS', 'Supra'),
(2022, 'SEDAN', 'Toyota', 'LUXURY', 'Avalon'),
(2023, 'ELECTRIC', 'Toyota', 'ECO', 'bZ4X'),

-- Honda (10 моделей)
(2022, 'SEDAN', 'Honda', 'MID_SIZE', 'Accord'),
(2023, 'SUV', 'Honda', 'COMPACT_SUV', 'CR-V'),
(2022, 'HATCHBACK', 'Honda', 'COMPACT', 'Civic'),
(2023, 'SUV', 'Honda', 'MID_SIZE', 'Pilot'),
(2022, 'MINIVAN', 'Honda', 'VAN', 'Odyssey'),
(2023, 'HYBRID', 'Honda', 'ECO', 'Accord Hybrid'),
(2022, 'CROSSOVER', 'Honda', 'CROSSOVER', 'HR-V'),
(2023, 'ELECTRIC', 'Honda', 'ECO', 'e'),
(2022, 'SEDAN', 'Honda', 'EXECUTIVE', 'Insight'),
(2023, 'HATCHBACK', 'Honda', 'HOT_HATCH', 'Civic Type R'),

-- BMW (10 моделей)
(2022, 'SEDAN', 'BMW', 'LUXURY', '3 Series'),
(2023, 'SUV', 'BMW', 'SUV', 'X3'),
(2022, 'COUPE', 'BMW', 'SPORTS', '4 Series'),
(2023, 'SEDAN', 'BMW', 'EXECUTIVE', '5 Series'),
(2022, 'SUV', 'BMW', 'LUXURY_SUV', 'X5'),
(2023, 'ELECTRIC', 'BMW', 'ECO', 'i4'),
(2022, 'SEDAN', 'BMW', 'LUXURY', '7 Series'),
(2023, 'SUV', 'BMW', 'COUPE_SUV', 'X6'),
(2022, 'CONVERTIBLE', 'BMW', 'SPORTS', 'Z4'),
(2023, 'HATCHBACK', 'BMW', 'COMPACT', '1 Series'),

-- Mercedes-Benz (10 моделей)
(2022, 'SEDAN', 'Mercedes-Benz', 'LUXURY', 'C-Class'),
(2023, 'SUV', 'Mercedes-Benz', 'SUV', 'GLC'),
(2022, 'SEDAN', 'Mercedes-Benz', 'EXECUTIVE', 'E-Class'),
(2023, 'SUV', 'Mercedes-Benz', 'LUXURY_SUV', 'GLE'),
(2022, 'COUPE', 'Mercedes-Benz', 'SPORTS', 'E-Class Coupe'),
(2023, 'ELECTRIC', 'Mercedes-Benz', 'ECO', 'EQS'),
(2022, 'SEDAN', 'Mercedes-Benz', 'LUXURY', 'S-Class'),
(2023, 'SUV', 'Mercedes-Benz', 'COMPACT_SUV', 'GLA'),
(2022, 'HATCHBACK', 'Mercedes-Benz', 'COMPACT', 'A-Class'),
(2023, 'CONVERTIBLE', 'Mercedes-Benz', 'SPORTS', 'C-Class Cabriolet'),

-- Volkswagen (10 моделей)
(2022, 'SEDAN', 'Volkswagen', 'ECONOMY', 'Jetta'),
(2023, 'HATCHBACK', 'Volkswagen', 'COMPACT', 'Golf'),
(2022, 'SUV', 'Volkswagen', 'COMPACT_SUV', 'Tiguan'),
(2023, 'SUV', 'Volkswagen', 'MID_SIZE', 'Atlas'),
(2022, 'ELECTRIC', 'Volkswagen', 'ECO', 'ID.4'),
(2023, 'SEDAN', 'Volkswagen', 'EXECUTIVE', 'Arteon'),
(2022, 'MINIVAN', 'Volkswagen', 'VAN', 'Touran'),
(2023, 'HATCHBACK', 'Volkswagen', 'HOT_HATCH', 'Golf GTI'),
(2022, 'CROSSOVER', 'Volkswagen', 'CROSSOVER', 'Taos'),
(2023, 'MINIVAN', 'Volkswagen', 'VAN', 'ID. Buzz');

-- 3. Клиенты (500 клиентов)
DO $$
    DECLARE
        i INTEGER;
        first_names TEXT[] := ARRAY['Ivan', 'Sergey', 'Alexey', 'Dmitry', 'Andrey', 'Mikhail', 'Pavel', 'Roman', 'Vladimir', 'Nikolay', 'Anna', 'Elena', 'Olga', 'Maria', 'Svetlana', 'Tatyana', 'Natalia', 'Irina', 'Ekaterina', 'Yulia'];
        last_names TEXT[] := ARRAY['Ivanov', 'Petrov', 'Sidorov', 'Smirnov', 'Kuznetsov', 'Popov', 'Lebedev', 'Kozlov', 'Novikov', 'Morozov', 'Volkov', 'Solovyov', 'Vasilyev', 'Zaytsev', 'Pavlov', 'Semyonov', 'Golubev', 'Vinogradov', 'Bogdanov', 'Vorobyov'];
        domains TEXT[] := ARRAY['gmail.com', 'mail.ru', 'yandex.ru', 'outlook.com', 'hotmail.com'];
        -- Реальные bcrypt хеши для пароля "123456"
        passwords TEXT[] := ARRAY[
            '$2a$10$RRXBFDvt2.B34wsDyUFfa.q5bqA1Sb9zHDKy2ncULZ6qizeFAbwyG'];
    BEGIN
        FOR i IN 1..500 LOOP
                INSERT INTO car_rental.client (role_id, first_name, last_name, login, email, password, phone)
                VALUES (
                           1,
                           first_names[1 + (i-1) % array_length(first_names, 1)],
                           last_names[1 + (i-1) % array_length(last_names, 1)],
                           'user' || i,
                           'user' || i || '@' || domains[1 + (i-1) % array_length(domains, 1)],
                           passwords[1 + (i-1) % array_length(passwords, 1)],  -- Используем реальные хеши
                           '+7916' || lpad((i)::text, 7, '0')
                       );
            END LOOP;
    END $$;

-- 4. Документы (ТОЛЬКО 100 документов)
DO $$
    DECLARE
        i INTEGER;
        series_arr TEXT[] := ARRAY['45 00', '46 00', '47 00', '48 00', '49 00', '50 00', '51 00', '52 00', '53 00', '54 00'];
        auth_arr TEXT[] := ARRAY['ОУФМС России по г. Москве', 'ОУФМС России по г. Санкт-Петербургу', 'ОУФМС России по Московской области', 'ОУФМС России по Ленинградской области', 'ОУФМС России по Краснодарскому краю'];
    BEGIN
        FOR i IN 1..100 LOOP  -- ТОЛЬКО 100 документов!
        INSERT INTO car_rental.document (date_of_issue, client_id, doctype_id, issuing_authority, number, series)
        VALUES (
                   (CURRENT_DATE - INTERVAL '5 years' + (random() * 1825 || ' days')::INTERVAL)::DATE,
                   i,  -- Только для первых 100 клиентов
                   1 + (i-1) % 3,
                   auth_arr[1 + (i-1) % array_length(auth_arr, 1)],
                   lpad((i * 12345)::text, 6, '0'),
                   series_arr[1 + (i-1) % array_length(series_arr, 1)]
               );
            END LOOP;
    END $$;

-- 5. Автомобили (500 автомобилей)
DO $$
    DECLARE
        i INTEGER;
        regions TEXT[] := ARRAY['77', '78', '50', '90', '150', '190', '750', '777', '99', '97', '177', '197', '199'];
        letters TEXT[] := ARRAY['A', 'B', 'E', 'K', 'M', 'H', 'O', 'P', 'C', 'T', 'Y', 'X'];
        vin_chars TEXT := 'ABCDEFGHJKLMNPRSTUVWXYZ1234567890';
        image_urls TEXT[] := ARRAY[
            'https://example.com/car1.jpg',
            'https://example.com/car2.jpg',
            'https://example.com/car3.jpg',
            'https://example.com/car4.jpg',
            'https://example.com/car5.jpg'
            ];
        model_count INTEGER;
        all_models INTEGER[];
    BEGIN
        -- Получаем все ID моделей
        SELECT array_agg(id_model) INTO all_models FROM car_rental.car_model;
        model_count := array_length(all_models, 1);

        FOR i IN 1..500 LOOP
                INSERT INTO car_rental.car (model_id, state_id, rent, gos_number, image_url, vin)
                VALUES (
                           -- Используем ВСЕ модели равномерно
                           all_models[1 + ((i-1) % model_count)],
                           -- state_id от 1 до 4
                           1 + ((i-1) % 4),
                           -- Стоимость аренды от 1000 до 10000
                           (random() * 9000 + 1000)::NUMERIC(10,2),
                           -- Госномер
                           letters[1 + ((i-1) % array_length(letters, 1))] ||
                           lpad(((i * 97) % 999)::text, 3, '0') ||
                           letters[1 + (((i+2)-1) % array_length(letters, 1))] ||
                           letters[1 + (((i+5)-1) % array_length(letters, 1))] ||
                           ' ' ||
                           regions[1 + ((i-1) % array_length(regions, 1))],
                           -- Картинка
                           image_urls[1 + ((i-1) % array_length(image_urls, 1))],
                           -- VIN
                           'VIN' || lpad(i::text, 6, '0') ||
                           substring(vin_chars FROM (1 + ((i*3) % length(vin_chars))) FOR 1) ||
                           substring(vin_chars FROM (1 + ((i*7) % length(vin_chars))) FOR 1) ||
                           lpad(((i * 13) % 10000)::text, 4, '0')
                       );
            END LOOP;
    END $$;

-- 6. Контракты (ТОЛЬКО 100 контрактов)
DO $$
    DECLARE
        i INTEGER;
        start_date DATE;
        end_date DATE;
        total_cost NUMERIC;
        car_count INTEGER;
        client_count INTEGER;
    BEGIN
        -- Получаем количество записей
        SELECT COUNT(*) INTO car_count FROM car_rental.car;
        SELECT COUNT(*) INTO client_count FROM car_rental.client;

        FOR i IN 1..100 LOOP  -- ТОЛЬКО 100 контрактов!
        start_date := (CURRENT_DATE - INTERVAL '30 days' + ((random() * 60)::INTEGER || ' days')::INTERVAL)::DATE;
        end_date := (start_date + INTERVAL '1 day' + ((random() * 14)::INTEGER || ' days')::INTERVAL)::DATE;
        total_cost := (random() * 10000 + 1000)::NUMERIC(10,2);

        INSERT INTO car_rental.contract (data_end, data_start, total_cost, car_id, client_id, state_id)
        VALUES (
                   end_date,
                   start_date,
                   total_cost,
                   -- car_id от 1 до car_count
                   1 + ((i-1) % car_count),
                   -- client_id от 1 до client_count (только первые 100 клиентов)
                   1 + ((i-1) % 100),  -- Только первые 100 клиентов
                   -- state_id от 1 до 4
                   1 + ((i-1) % 4)
               );
            END LOOP;
    END $$;