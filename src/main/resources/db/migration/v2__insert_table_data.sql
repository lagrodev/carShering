-- =============================================
-- Заполняем справочные таблицы
-- =============================================

-- Роли
INSERT INTO car_rental.role (name)
VALUES ('CLIENT'), ('MANAGER'), ('ADMIN'), ('SUPPORT');

-- Состояния аренды
INSERT INTO car_rental.rental_state (name)
VALUES ('ACTIVE'), ('COMPLETED'), ('CANCELLED'), ('PENDING'), ('OVERDUE');

-- Состояния машин
INSERT INTO car_rental.car_state (name)
VALUES ('AVAILABLE'), ('RENTED'), ('MAINTENANCE'), ('OUT_OF_SERVICE'), ('IN_TRANSIT');

-- Модели автомобилей (~50 шт)
INSERT INTO car_rental.car_model (brand, model, body_type, car_class, year_of_issue)
SELECT
    brand,
    model,
    (ARRAY['sedan', 'hatchback', 'suv', 'wagon', 'coupe'])[1 + (random() * 4)::int],
    (ARRAY['ECONOMY', 'COMPACT', 'MIDSIZE', 'PREMIUM', 'LUXURY'])[1 + (random() * 4)::int],
    2015 + (random() * 9)::int
FROM generate_series(1, 50) AS s(i),
     (VALUES
          ('Toyota', 'Camry'),
          ('Toyota', 'Corolla'),
          ('Honda', 'Civic'),
          ('Honda', 'Accord'),
          ('BMW', '3 Series'),
          ('BMW', 'X5'),
          ('Mercedes-Benz', 'C-Class'),
          ('Mercedes-Benz', 'E-Class'),
          ('Audi', 'A4'),
          ('Audi', 'Q7'),
          ('Volkswagen', 'Golf'),
          ('Volkswagen', 'Passat'),
          ('Ford', 'Focus'),
          ('Ford', 'Explorer'),
          ('Nissan', 'Altima'),
          ('Nissan', 'X-Trail'),
          ('Hyundai', 'Elantra'),
          ('Hyundai', 'Santa Fe'),
          ('Kia', 'Optima'),
          ('Kia', 'Sportage')
     ) AS brands_models(brand, model)
LIMIT 50;

-- Машины (~600 шт)
INSERT INTO car_rental.car (gos_number, vin, model_id, state_id)
SELECT
    upper(substring(md5(random()::text), 1, 4) || '-' ||
          (random() * 99)::int || '-' ||
          substring(md5(random()::text), 1, 2)),
    upper(left(md5(random()::text || clock_timestamp()::text), 17)),
    (1 + (random() * 49)::int),
    (1 + (random() * 4)::int)
FROM generate_series(1, 600);

-- Клиенты (~550 шт)
WITH names AS (
    SELECT *
    FROM (VALUES
              ('Ivan', 'Ivanov'),
              ('Petr', 'Petrov'),
              ('Anna', 'Smirnova'),
              ('Olga', 'Kuznetsova'),
              ('Dmitry', 'Sidorov'),
              ('Elena', 'Morozova'),
              ('Alexey', 'Lebedev'),
              ('Maria', 'Fedorova'),
              ('Andrey', 'Popov'),
              ('Tatiana', 'Volkova'),
              ('Sergey', 'Orlov'),
              ('Natalia', 'Belova'),
              ('Maxim', 'Kozlov'),
              ('Yulia', 'Nikitina'),
              ('Artem', 'Sokolov'),
              ('Karina', 'Pavlova'),
              ('Roman', 'Soloviev'),
              ('Alena', 'Leonteva'),
              ('Kirill', 'Kozhevnikov'),
              ('Daria', 'Samoylova')
         ) AS v(first_name, second_name)
)
INSERT INTO car_rental.client (
    first_name, second_name, phone, mail, login, password, role_id
)
SELECT
    n.first_name,
    n.second_name,
    '+7' || lpad(floor(random() * 999999999)::text, 9, '0'),
    lower(n.first_name || '.' || n.second_name || i || '@example.com'),
    lower(n.first_name || '.' || n.second_name || i),
    md5('password' || i), -- Не использовать в продакшене!
    (1 + (random() * 3)::int)
FROM generate_series(1, 550) AS i,
     names n
OFFSET floor(random() * 20)
    LIMIT 550;

-- Документы (~550 шт)
INSERT INTO car_rental.document (
    document_type, series, number, date_of_issue, issuing_authority, client_id
)
SELECT
    'PASSPORT',
    lpad((1000 + (random() * 8999)::int)::text, 4, '0'),
    lpad((100000 + (random() * 899999)::int)::text, 6, '0'),
    current_date - (random() * 365 * 10)::int,
    'UFMS ' || (1 + (random() * 100)::int),
    id
FROM car_rental.client
ORDER BY random()
LIMIT 550;

-- Контракты (~600 шт)
INSERT INTO car_rental.contract (
    data_start, data_end, total_cost, client_id, car_id, state_id
)
SELECT
    start_date::date,
    (start_date + ((1 + random() * 30)::int || ' days')::interval)::date,
    round((1000 + random() * 50000)::numeric, 2),
    (SELECT id FROM car_rental.client ORDER BY random() LIMIT 1),
    (SELECT id FROM car_rental.car ORDER BY random() LIMIT 1),
    (1 + (random() * 4)::int)
FROM (
         SELECT current_date - (random() * 365 * 3)::int AS start_date
         FROM generate_series(1, 600)
     ) AS dates;

-- =============================================
-- Проверка количества записей
-- =============================================
SELECT 'role' as table_name, COUNT(*) FROM car_rental.role
UNION ALL
SELECT 'client', COUNT(*) FROM car_rental.client
UNION ALL
SELECT 'document', COUNT(*) FROM car_rental.document
UNION ALL
SELECT 'car_model', COUNT(*) FROM car_rental.car_model
UNION ALL
SELECT 'car_state', COUNT(*) FROM car_rental.car_state
UNION ALL
SELECT 'car', COUNT(*) FROM car_rental.car
UNION ALL
SELECT 'rental_state', COUNT(*) FROM car_rental.rental_state
UNION ALL
SELECT 'contract', COUNT(*) FROM car_rental.contract;