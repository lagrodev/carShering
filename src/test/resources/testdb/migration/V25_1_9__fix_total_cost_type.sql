-- Fix total_cost column type from numeric to double precision
ALTER TABLE car_rental.contract
    ALTER COLUMN total_cost TYPE DOUBLE PRECISION;
