ALTER TABLE car_rental.client
    DROP CONSTRAINT IF EXISTS ukbfgjs3fem0hmjhvih80158x29;

ALTER TABLE car_rental.client
    DROP CONSTRAINT IF EXISTS ukgg7oafsubulcaholss4i5kfgl;
ALTER TABLE car_rental.client
    DROP CONSTRAINT IF EXISTS ukqe9dtj732yl9u3oqhhsee4lps;


CREATE UNIQUE INDEX uk_client_login_active ON car_rental.client (login) WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_client_email_active ON car_rental.client (email) WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_client_phone_active ON car_rental.client (phone) WHERE is_deleted = false;
