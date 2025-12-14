CREATE TABLE car_rental.permission (
                                       id BIGSERIAL PRIMARY KEY,
                                       name VARCHAR(50) NOT NULL UNIQUE,
                                       description VARCHAR(255)
);

CREATE TABLE car_rental.role_permission (
                                            role_id BIGINT NOT NULL REFERENCES car_rental.role(id) ON DELETE CASCADE,
                                            permission_id BIGINT NOT NULL REFERENCES car_rental.permission(id) ON DELETE CASCADE,
                                            PRIMARY KEY (role_id, permission_id)
);

-- Вставляем права из твоего enum Permission
INSERT INTO car_rental.permission (name, description) VALUES
                                                          ('VIEW_USERS', 'Просмотр пользователей'),
                                                          ('MANAGE_USERS', 'Управление пользователями'),
                                                          ('BAN_USERS', 'Блокировка пользователей'),
                                                          ('VIEW_DOCUMENTS', 'Просмотр документов'),
                                                          ('VERIFY_DOCUMENTS', 'Верификация документов'),
                                                          ('CREATE_CONTRACTS', 'Создание договоров'),
                                                          ('UPDATE_CONTRACTS', 'Изменение договоров'),
                                                          ('CANCEL_CONTRACTS', 'Отмена договоров'),
                                                          ('MANAGE_CARS', 'Управление автомобилями'),
                                                          ('VIEW_REPORTS', 'Просмотр отчетов'),
                                                          ('GENERATE_REPORTS', 'Генерация отчетов');

-- Назначаем права ролям
-- CLIENT (id=1)
INSERT INTO car_rental.role_permission (role_id, permission_id) VALUES
                                                                    (1, (SELECT id FROM car_rental.permission WHERE name = 'VIEW_DOCUMENTS')),
                                                                    (1, (SELECT id FROM car_rental.permission WHERE name = 'CREATE_CONTRACTS'));

-- ADMIN (id=2)
INSERT INTO car_rental.role_permission (role_id, permission_id)
SELECT 2, id FROM car_rental.permission; -- все права

-- MANAGER (id=3)
INSERT INTO car_rental.role_permission (role_id, permission_id) VALUES
                                                                    (3, (SELECT id FROM car_rental.permission WHERE name = 'VIEW_USERS')),
                                                                    (3, (SELECT id FROM car_rental.permission WHERE name = 'VIEW_DOCUMENTS')),
                                                                    (3, (SELECT id FROM car_rental.permission WHERE name = 'VERIFY_DOCUMENTS')),
                                                            (3, (SELECT id FROM car_rental.permission WHERE name = 'CREATE_CONTRACTS')),
                                                                    (3, (SELECT id FROM car_rental.permission WHERE name = 'UPDATE_CONTRACTS')),
                                                                    (3, (SELECT id FROM car_rental.permission WHERE name = 'MANAGE_CARS'));