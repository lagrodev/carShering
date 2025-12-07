ALTER TABLE car_rental.contract
    DROP CONSTRAINT fk1imu9u0vjfpxljn0keruwa2yi;

ALTER TABLE car_rental.contract
    DROP CONSTRAINT fkixovqbw2hcpmeuelx4a7la9td;

ALTER TABLE car_rental.contract
    DROP CONSTRAINT fklhq3p3xl25vvnfvyfc51ica0j;

ALTER TABLE car_rental.contract
    ADD state VARCHAR(255);

ALTER TABLE car_rental.contract
    DROP COLUMN state_id;

DROP SEQUENCE car_rental.brands_id_seq1 CASCADE;

DROP SEQUENCE car_rental.models_id_seq1 CASCADE;

ALTER TABLE car_rental.contract
    ALTER COLUMN currency TYPE VARCHAR(255) USING (currency::VARCHAR(255));

ALTER TABLE car_rental.contract
    ALTER COLUMN currency DROP NOT NULL;

ALTER TABLE car_rental.contract
    ALTER COLUMN duration_minutes SET NOT NULL;