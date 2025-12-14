UPDATE car_rental.contract
SET state = (ARRAY[
    'PENDING',
    'CONFIRMED',
    'ACTIVE',
    'COMPLETED',
    'CANCELLED',
    'CANCELLATION_REQUESTED'
    ])[floor(random() * 6 + 1)];