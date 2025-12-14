
ALTER TABLE car_rental.car
    ADD state_type VARCHAR(255);




UPDATE car_rental.car c
SET state_type = cs.status
FROM car_rental.car_state cs
WHERE c.state_id = cs.id;


ALTER TABLE car_rental.car
    ALTER COLUMN state_type SET NOT NULL;

ALTER TABLE car_rental.car
    DROP COLUMN state_id;

ALTER TABLE car_rental.car
    ALTER COLUMN currency TYPE VARCHAR(255) USING (currency::VARCHAR(255));

ALTER TABLE car_rental.car
    ALTER COLUMN currency DROP NOT NULL;

ALTER TABLE car_rental.car
    ALTER COLUMN year_of_issue SET NOT NULL;

ALTER TABLE car_rental.car
    DROP CONSTRAINT IF EXISTS fkr7ylxgrplnijyxbh6lkn1eisl;
