ALTER TABLE car_rental.client
    ADD created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();

ALTER TABLE car_rental.client
    ADD email_verified BOOLEAN DEFAULT FALSE;

ALTER TABLE car_rental.client
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();

ALTER TABLE car_rental.client
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE car_rental.client
    ALTER COLUMN email_verified SET NOT NULL;

ALTER TABLE car_rental.client
    ALTER COLUMN updated_at SET NOT NULL;