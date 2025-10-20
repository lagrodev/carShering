-- 1. Сначала базовые справочники
INSERT INTO car_rental.role (name) VALUES
                                       ('CLIENT'),
                                       ('ADMIN'),
                                       ('MANAGER');

INSERT INTO car_rental.car_state (status) VALUES
                                              ('AVAILABLE'),
                                              ('CONFIRMED'),
                                              ('ACTIVE'),
                                              ('CANCELLED'),
                                              ('CLOSED');

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
INSERT INTO car_rental.car_model (body_type, brand, car_class, model) VALUES
-- Toyota (10 моделей)
('SEDAN', 'Toyota', 'MID_SIZE', 'Camry'),
('SUV', 'Toyota', 'SUV', 'RAV4'),
('HATCHBACK', 'Toyota', 'COMPACT', 'Corolla'),
('SUV', 'Toyota', 'LARGE_SUV', 'Highlander'),
('PICKUP', 'Toyota', 'TRUCK', 'Tacoma'),
('MINIVAN', 'Toyota', 'VAN', 'Sienna'),
('HYBRID', 'Toyota', 'ECO', 'Prius'),
('COUPE', 'Toyota', 'SPORTS', 'Supra'),
('SEDAN', 'Toyota', 'LUXURY', 'Avalon'),
('ELECTRIC', 'Toyota', 'ECO', 'bZ4X'),

-- Honda (10 моделей)
('SEDAN', 'Honda', 'MID_SIZE', 'Accord'),
('SUV', 'Honda', 'COMPACT_SUV', 'CR-V'),
('HATCHBACK', 'Honda', 'COMPACT', 'Civic'),
('SUV', 'Honda', 'MID_SIZE', 'Pilot'),
('MINIVAN', 'Honda', 'VAN', 'Odyssey'),
('HYBRID', 'Honda', 'ECO', 'Accord Hybrid'),
('CROSSOVER', 'Honda', 'CROSSOVER', 'HR-V'),
('ELECTRIC', 'Honda', 'ECO', 'e'),
('SEDAN', 'Honda', 'EXECUTIVE', 'Insight'),
('HATCHBACK', 'Honda', 'HOT_HATCH', 'Civic Type R'),

-- BMW (10 моделей)
('SEDAN', 'BMW', 'LUXURY', '3 Series'),
('SUV', 'BMW', 'SUV', 'X3'),
('COUPE', 'BMW', 'SPORTS', '4 Series'),
('SEDAN', 'BMW', 'EXECUTIVE', '5 Series'),
('SUV', 'BMW', 'LUXURY_SUV', 'X5'),
('ELECTRIC', 'BMW', 'ECO', 'i4'),
('SEDAN', 'BMW', 'LUXURY', '7 Series'),
('SUV', 'BMW', 'COUPE_SUV', 'X6'),
('CONVERTIBLE', 'BMW', 'SPORTS', 'Z4'),
('HATCHBACK', 'BMW', 'COMPACT', '1 Series'),

-- Mercedes-Benz (10 моделей)
('SEDAN', 'Mercedes-Benz', 'LUXURY', 'C-Class'),
('SUV', 'Mercedes-Benz', 'SUV', 'GLC'),
('SEDAN', 'Mercedes-Benz', 'EXECUTIVE', 'E-Class'),
('SUV', 'Mercedes-Benz', 'LUXURY_SUV', 'GLE'),
('COUPE', 'Mercedes-Benz', 'SPORTS', 'E-Class Coupe'),
('ELECTRIC', 'Mercedes-Benz', 'ECO', 'EQS'),
('SEDAN', 'Mercedes-Benz', 'LUXURY', 'S-Class'),
('SUV', 'Mercedes-Benz', 'COMPACT_SUV', 'GLA'),
('HATCHBACK', 'Mercedes-Benz', 'COMPACT', 'A-Class'),
('CONVERTIBLE', 'Mercedes-Benz', 'SPORTS', 'C-Class Cabriolet'),

-- Volkswagen (10 моделей)
('SEDAN', 'Volkswagen', 'ECONOMY', 'Jetta'),
('HATCHBACK', 'Volkswagen', 'COMPACT', 'Golf'),
('SUV', 'Volkswagen', 'COMPACT_SUV', 'Tiguan'),
('SUV', 'Volkswagen', 'MID_SIZE', 'Atlas'),
('ELECTRIC', 'Volkswagen', 'ECO', 'ID.4'),
('SEDAN', 'Volkswagen', 'EXECUTIVE', 'Arteon'),
('MINIVAN', 'Volkswagen', 'VAN', 'Touran'),
('HATCHBACK', 'Volkswagen', 'HOT_HATCH', 'Golf GTI'),
('CROSSOVER', 'Volkswagen', 'CROSSOVER', 'Taos'),
('MINIVAN', 'Volkswagen', 'VAN', 'ID. Buzz');
-- 3. Клиенты (500 клиентов)
DO $$
    DECLARE
        i INTEGER;
        first_names TEXT[] := ARRAY['Ivan', 'Sergey', 'Alexey', 'Dmitry', 'Andrey', 'Mikhail', 'Pavel', 'Roman', 'Vladimir', 'Nikolay', 'Anna', 'Elena', 'Olga', 'Maria', 'Svetlana', 'Tatyana', 'Natalia', 'Irina', 'Ekaterina', 'Yulia'];
        last_names TEXT[] := ARRAY['Ivanov', 'Petrov', 'Sidorov', 'Smirnov', 'Kuznetsov', 'Popov', 'Lebedev', 'Kozlov', 'Novikov', 'Morozov', 'Volkov', 'Solovyov', 'Vasilyev', 'Zaytsev', 'Pavlov', 'Semyonov', 'Golubev', 'Vinogradov', 'Bogdanov', 'Vorobyov'];
        domains TEXT[] := ARRAY['gmail.com', 'mail.ru', 'yandex.ru', 'outlook.com', 'hotmail.com'];
        common_password TEXT := '$2a$10$1RrXcR.H26thUG.WZGRjWeVRhHbOfdZwJHqEFisQFq447q67mOG8K';
    BEGIN
        FOR i IN 1..500 LOOP
                INSERT INTO car_rental.client (
                    role_id, first_name, last_name, login, email, password, phone, is_banned, is_deleted
                ) VALUES (
                             CASE WHEN i = 101 THEN 2 ELSE 1 END,  -- user101 -> ADMIN (role_id=2)
                             first_names[1 + (i-1) % array_length(first_names, 1)],
                             last_names[1 + (i-1) % array_length(last_names, 1)],
                             'user' || i,
                             'user' || i || '@' || domains[1 + (i-1) % array_length(domains, 1)],
                             common_password,
                             '+7916' || lpad(i::text, 7, '0'),
                             false,
                             false
                         );
            END LOOP;
    END $$;

-- 4. Документы (ТОЛЬКО 100 документов, для первых 100 клиентов)
DO $$
    DECLARE
        i INTEGER;
        series_arr TEXT[] := ARRAY['45 00', '46 00', '47 00', '48 00', '49 00', '50 00', '51 00', '52 00', '53 00', '54 00'];
        auth_arr TEXT[] := ARRAY['ОУФМС России по г. Москве', 'ОУФМС России по г. Санкт-Петербургу', 'ОУФМС России по Московской области', 'ОУФМС России по Ленинградской области', 'ОУФМС России по Краснодарскому краю'];
    BEGIN
        FOR i IN 1..100 LOOP
                INSERT INTO car_rental.document (
                    date_of_issue, client_id, doctype_id, issuing_authority, number, series, is_verified, is_deleted
                ) VALUES (
                             (CURRENT_DATE - INTERVAL '5 years' + (random() * 1825 || ' days')::INTERVAL)::DATE,
                             i,
                             1 + (i-1) % 3,
                             auth_arr[1 + (i-1) % array_length(auth_arr, 1)],
                             lpad((i * 12345)::text, 6, '0'),
                             series_arr[1 + (i-1) % array_length(series_arr, 1)],
                             true,   -- можно поставить false, если нужно
                             false
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
        all_models BIGINT[];
        model_count INTEGER;
        random_year INTEGER;
    BEGIN
        SELECT array_agg(id_model) INTO all_models FROM car_rental.car_model;
        model_count := array_length(all_models, 1);

        FOR i IN 1..500 LOOP
                -- Генерируем случайный год от 2020 до 2025
                random_year := 2020 + (i % 6);

                INSERT INTO car_rental.car (
                    model_id, state_id, rent, gos_number, image_url, vin, year_of_issue
                ) VALUES (
                             all_models[1 + ((i-1) % model_count)],
                             1 + ((i-1) % 4),
                             (random() * 9000 + 1000)::NUMERIC(10,2),
                             letters[1 + ((i-1) % array_length(letters, 1))] ||
                             lpad(((i * 97) % 999)::text, 3, '0') ||
                             letters[1 + (((i+2)-1) % array_length(letters, 1))] ||
                             letters[1 + (((i+5)-1) % array_length(letters, 1))] ||
                             ' ' ||
                             regions[1 + ((i-1) % array_length(regions, 1))],
                             image_urls[1 + ((i-1) % array_length(image_urls, 1))],
                             'VIN' || lpad(i::text, 6, '0') ||
                             substring(vin_chars FROM (1 + ((i*3) % length(vin_chars))) FOR 1) ||
                             substring(vin_chars FROM (1 + ((i*7) % length(vin_chars))) FOR 1) ||
                             lpad(((i * 13) % 10000)::text, 4, '0'),
                             random_year
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
        SELECT COUNT(*) INTO car_count FROM car_rental.car;
        SELECT COUNT(*) INTO client_count FROM car_rental.client;

        FOR i IN 1..100 LOOP
                start_date := (CURRENT_DATE - INTERVAL '30 days' + ((random() * 60)::INTEGER || ' days')::INTERVAL)::DATE;
                end_date := (start_date + INTERVAL '1 day' + ((random() * 14)::INTEGER || ' days')::INTERVAL)::DATE;
                total_cost := (random() * 10000 + 1000)::NUMERIC(10,2);

                INSERT INTO car_rental.contract (
                    data_end, data_start, total_cost, car_id, client_id, state_id, comment
                ) VALUES (
                             end_date,
                             start_date,
                             total_cost,
                             1 + ((i-1) % car_count),
                             1 + ((i-1) % 100),  -- только первые 100 клиентов
                             1 + ((i-1) % 4),    -- rental_state: 1–4
                             NULL
                         );
            END LOOP;
    END $$;