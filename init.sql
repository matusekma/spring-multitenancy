SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

CREATE SCHEMA admin;


ALTER SCHEMA admin OWNER TO wallet;


CREATE SCHEMA tenant1;


ALTER SCHEMA tenant1 OWNER TO wallet;


CREATE SCHEMA tenant2;


ALTER SCHEMA tenant2 OWNER TO wallet;

SET default_tablespace = '';

SET default_table_access_method = heap;

CREATE TABLE admin.tenants (
    id integer,
    name text
);

ALTER TABLE admin.tenants OWNER TO wallet;


INSERT INTO tenant1.passes (name, id) VALUES ('tenant1pass', 1);


INSERT INTO tenant2.passes (name, id) VALUES ('tenant2pass', 1);


