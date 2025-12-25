ALTER TABLE car_rental.favorite
ADD CONSTRAINT uk_favorite_client_car
    UNIQUE (client_id, car_id);