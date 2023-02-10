ALTER TABLE product ALTER COLUMN stock TYPE integer USING stock::integer;

ALTER TABLE product
ALTER COLUMN price SET DATA TYPE double precision USING price::double precision;