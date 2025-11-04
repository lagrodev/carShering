DELETE FROM car_rental.document d
    USING car_rental.document dup
WHERE d.id > dup.id
  AND LOWER(d.series) = LOWER(dup.series)
  AND LOWER(d.number) = LOWER(dup.number)
  AND d.is_deleted = false
  AND dup.is_deleted = false;

CREATE UNIQUE INDEX IF NOT EXISTS uk_document_series_number_active
    ON car_rental.document (LOWER(series), LOWER(number))
    WHERE is_deleted = false;