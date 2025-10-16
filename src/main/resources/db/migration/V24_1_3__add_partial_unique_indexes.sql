CREATE UNIQUE INDEX uk_client_login_active ON car_rental.client (login) WHERE is_deleted = false;
CREATE UNIQUE INDEX uk_client_email_active ON car_rental.client (email) WHERE is_deleted = false;