ALTER TABLE car_rental.favorite
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE car_rental.favorite
    ALTER COLUMN created_at SET NOT NULL;
