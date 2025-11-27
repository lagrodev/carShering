ALTER TABLE car_rental.contract
    ALTER COLUMN data_start TYPE timestamp USING data_start::timestamp,
    ALTER COLUMN data_end TYPE timestamp USING data_end::timestamp;
