ALTER TABLE car_rental.car
    drop if exists rent;


ALTER TABLE car_rental.car
    ADD if not exists  rent DECIMAL;

alter table car_rental.contract
    drop  if exists total_cost;

ALTER TABLE car_rental.car ADD COLUMN if not exists currency VARCHAR(3) DEFAULT 'RUB' NOT NULL;
ALTER TABLE car_rental.contract ADD  COLUMN if not exists  currency VARCHAR(3) DEFAULT 'RUB' NOT NULL;
ALTER TABLE car_rental.contract ADD COLUMN if not exists  total_cost DECIMAL;
