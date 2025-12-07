ALTER TABLE car_rental.contract
    DROP CONSTRAINT fk1imu9u0vjfpxljn0keruwa2yi;

ALTER TABLE car_rental.contract
    DROP CONSTRAINT fkixovqbw2hcpmeuelx4a7la9td;

ALTER TABLE car_rental.contract add
    CONSTRAINT fk_contract_client
        FOREIGN KEY (client_id) REFERENCES car_rental.client(id);

ALTER TABLE car_rental.contract add
    CONSTRAINT fk_contract_car
    FOREIGN KEY (car_id) REFERENCES car_rental.car(id);

ALTER TABLE car_rental.contract
    DROP CONSTRAINT fklhq3p3xl25vvnfvyfc51ica0j;

ALTER TABLE car_rental.contract
    ADD state VARCHAR(255);

ALTER TABLE car_rental.contract
    DROP COLUMN state_id;


ALTER TABLE car_rental.contract
    ALTER COLUMN currency TYPE VARCHAR(255) USING (currency::VARCHAR(255));

ALTER TABLE car_rental.contract
    ALTER COLUMN currency DROP NOT NULL;

UPDATE car_rental.contract SET duration_minutes = 0 WHERE duration_minutes IS NULL;
ALTER TABLE car_rental.contract ALTER COLUMN duration_minutes SET DEFAULT 0;
ALTER TABLE car_rental.contract ALTER COLUMN duration_minutes SET NOT NULL;