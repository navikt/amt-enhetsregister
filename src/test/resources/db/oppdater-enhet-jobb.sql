-- Testdata for OppdaterEnhetJobbRepository

INSERT INTO oppdater_enhet_jobb (id, current_page, page_size, total_pages, type, status)
    VALUES (1, 0, 10000, 50000, 'MODERENHET', 'IN_PROGRESS');

INSERT INTO oppdater_enhet_jobb (id, current_page, page_size, total_pages, type, status)
    VALUES (2, 0, 10000, 50000, 'UNDERENHET', 'IN_PROGRESS');

INSERT INTO oppdater_enhet_jobb (id, current_page, page_size, total_pages, type, status)
    VALUES (3, 500, 10000, 50000, 'MODERENHET', 'PAUSED');

INSERT INTO oppdater_enhet_jobb (id, current_page, page_size, total_pages, type, status)
    VALUES (4, 50000, 10000, 50000, 'MODERENHET', 'COMPLETED');

ALTER SEQUENCE oppdater_enhet_jobb_id_seq RESTART WITH 5;
