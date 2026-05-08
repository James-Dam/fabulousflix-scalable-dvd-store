DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_movie(
    IN p_title VARCHAR(100),
    IN p_year INT,
    IN p_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_bYear INT,  -- Optional birth year
    IN p_genre_name VARCHAR(32),
    OUT p_out_movie_id VARCHAR(10),
    OUT p_out_status VARCHAR(10),
    OUT p_out_star_status VARCHAR(10),
    OUT p_out_genre_status VARCHAR(10)
)
add_mov: BEGIN
    DECLARE v_movie_id VARCHAR(10);
    DECLARE v_star_id VARCHAR(10);
    DECLARE v_genre_id INT;
    DECLARE last_movie_id VARCHAR(10);
    DECLARE last_star_id VARCHAR(10);
    DECLARE numeric_part INT;

    -- Check if the movie already exists
SELECT id INTO v_movie_id FROM movies
WHERE title = p_title AND year = p_year AND director = p_director
    LIMIT 1;

IF v_movie_id IS NOT NULL THEN
        -- Movie already exists
        SET p_out_movie_id = v_movie_id;
        SET p_out_status = "EXISTS";
        SET p_out_star_status = "UNCHANGED";
        SET p_out_genre_status = "UNCHANGED";
        LEAVE add_mov;
ELSE
        -- Generate a new Movie ID in 'ttXXXXXXX' format
SELECT MAX(id) INTO last_movie_id FROM movies WHERE id LIKE 'tt%';

IF last_movie_id IS NOT NULL THEN
            SET numeric_part = CAST(SUBSTRING(last_movie_id, 3) AS UNSIGNED) + 1;
ELSE
            SET numeric_part = 1;
END IF;

        SET v_movie_id = CONCAT('tt', LPAD(numeric_part, 7, '0'));

        -- Insert the new movie
INSERT INTO movies (id, title, year, director)
VALUES (v_movie_id, p_title, p_year, p_director);

-- Set output values
SET p_out_movie_id = v_movie_id;
        SET p_out_status = "INSERTED";
END IF;

    -- Check if the star exists
SELECT id INTO v_star_id FROM stars WHERE name = p_star_name LIMIT 1;

IF v_star_id IS NOT NULL THEN
        SET p_out_star_status = "EXISTS";  -- Star already exists
ELSE
        -- Generate a new Star ID in 'nmXXXXXXX' format
SELECT MAX(id) INTO last_star_id FROM stars WHERE id LIKE 'nm%';

IF last_star_id IS NOT NULL THEN
            SET numeric_part = CAST(SUBSTRING(last_star_id, 3) AS UNSIGNED) + 1;
ELSE
            SET numeric_part = 1;
END IF;

        SET v_star_id = CONCAT('nm', LPAD(numeric_part, 7, '0'));

        -- Insert new star, with optional birth year
INSERT INTO stars (id, name, birthYear)
VALUES (v_star_id, p_star_name, IFNULL(p_bYear, NULL));

SET p_out_star_status = "INSERTED";  -- New star added
END IF;

    -- Check if the genre exists
SELECT id INTO v_genre_id FROM genres WHERE name = p_genre_name LIMIT 1;

IF v_genre_id IS NOT NULL THEN
        SET p_out_genre_status = "EXISTS";  -- Genre already exists
ELSE
        -- Insert new genre
        INSERT INTO genres (name) VALUES (p_genre_name);
        SET v_genre_id = LAST_INSERT_ID();
        SET p_out_genre_status = "INSERTED";  -- New genre added
END IF;

    -- Link movie to star **only if not already linked**
    IF NOT EXISTS (
        SELECT 1 FROM stars_in_movies WHERE starId = v_star_id AND movieId = v_movie_id
    ) THEN
        INSERT INTO stars_in_movies (starId, movieId) VALUES (v_star_id, v_movie_id);
END IF;

    -- Link movie to genre only if not already linked
    IF NOT EXISTS (
        SELECT 1 FROM genres_in_movies WHERE genreId = v_genre_id AND movieId = v_movie_id
    ) THEN
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (v_genre_id, v_movie_id);
END IF;

END add_mov $$

DELIMITER ;
