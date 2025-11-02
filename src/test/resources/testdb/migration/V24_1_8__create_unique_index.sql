-- Уникальный регистронезависимый индекс для brands.name
CREATE UNIQUE INDEX IF NOT EXISTS uk_brands_name_lower ON car_rental.brands (LOWER(name));

-- Уникальный регистронезависимый индекс для car_classes.name
CREATE UNIQUE INDEX IF NOT EXISTS uk_car_classes_name_lower ON car_rental.car_classes (LOWER(name));

-- Уникальный регистронезависимый индекс для car_state.status
CREATE UNIQUE INDEX IF NOT EXISTS uk_car_state_status_lower ON car_rental.car_state (LOWER(status));

-- Уникальный регистронезависимый индекс для doctype.name
CREATE UNIQUE INDEX IF NOT EXISTS uk_doctype_name_lower ON car_rental.doctype (LOWER(name));

-- Уникальный регистронезависимый индекс для models.name
CREATE UNIQUE INDEX IF NOT EXISTS uk_models_name_lower ON car_rental.models (LOWER(name));

-- Уникальный регистронезависимый индекс для rental_state.name
CREATE UNIQUE INDEX IF NOT EXISTS uk_rental_state_name_lower ON car_rental.rental_state (LOWER(name));

-- Уникальный регистронезависимый индекс для role.name
CREATE UNIQUE INDEX IF NOT EXISTS uk_role_name_lower ON car_rental.role (LOWER(name));