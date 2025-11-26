ALTER TABLE car_rental.contract
    ADD duration_minutes BIGINT;


ALTER TABLE car_rental.contract
    ALTER COLUMN data_end SET NOT NULL;

ALTER TABLE car_rental.contract
    ALTER COLUMN data_start SET NOT NULL;