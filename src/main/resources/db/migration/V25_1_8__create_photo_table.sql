ALTER TABLE car_rental.images
    ALTER COLUMN url TYPE TEXT;

-- И, возможно, для file_name тоже:
ALTER TABLE car_rental.images
    ALTER COLUMN file_name TYPE TEXT;