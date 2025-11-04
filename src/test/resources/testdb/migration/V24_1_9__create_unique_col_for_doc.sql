
CREATE UNIQUE INDEX IF NOT EXISTS uk_document_series_number_active
    ON car_rental.document (LOWER(series), LOWER(number))
    WHERE is_deleted = false;