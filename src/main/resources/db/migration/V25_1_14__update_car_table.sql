UPDATE car_rental.car
SET rent = (1000 + (random() * 14000))::int;