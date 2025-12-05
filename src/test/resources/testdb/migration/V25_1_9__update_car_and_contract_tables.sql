ALTER TABLE car_rental.car
    drop if exists rent;

ALTER TABLE car_rental.car
    ADD if not exists currency VARCHAR(255);

ALTER TABLE car_rental.car
    ADD if not exists  rent DECIMAL;

alter table car_rental.contract
    drop  if exists total_cost;

alter table car_rental.contract
    add if not exists  total_cost DECIMAL;