CREATE TABLE IF NOT EXISTS shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

create table if not exists customer
(
    birth_date    date         null,
    ssn           int          null,
    id            bigint       not null
        primary key,
    email_address varchar(255) null,
    first_name    varchar(255) null,
    last_name     varchar(255) null,
    created_at date not null
);

create table  if not exists customer_seq
(
    next_val bigint null
);

create table if not exists outbox_message
(
    sent          bit          null,
    creation_date datetime(6)  null,
    id            bigint       not null
        primary key,
    last_update   datetime(6)  null,
    event_type    varchar(255) not null,
    payload       text         not null
);

create table if not exists outbox_message_seq
(
    next_val bigint null
);

-- Create the sequence table if it doesn't exist
create table if not exists customer_seq
(
    next_val bigint null
);



