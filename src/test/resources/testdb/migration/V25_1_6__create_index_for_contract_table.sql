CREATE INDEX idx_contract_datastart_state ON car_rental.contract(data_start, state_id);
CREATE INDEX idx_contract_car_client ON car_rental.contract(car_id, client_id);