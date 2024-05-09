create table if not exists users
(
    id       uuid         not null
        primary key,
    username varchar(30)  not null
        unique,
    email    varchar(255) not null
        unique,
    password varchar(255) not null,
    role     varchar(255) not null
        constraint users_role_check
            check ((role)::text = ANY
                   ((ARRAY ['ROLE_USER'::character varying, 'ROLE_ADMIN'::character varying])::text[]))
);

alter table users
    owner to postgres;

DO
$do$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'unique_username_email'
        ) THEN
            ALTER TABLE users ADD CONSTRAINT unique_username_email UNIQUE (username, email);
        END IF;
    END
$do$;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO users (id, username, email, password, role)
SELECT uuid_generate_v4(), 'admin', 'admin@mail.ru', '$2a$10$GymN0/Tn67huu5IX3gqP4.KfWWf8uSfoz.DSPO5c1UHuBEyCx35oC', 'ROLE_ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
) AND NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@mail.ru'
);




