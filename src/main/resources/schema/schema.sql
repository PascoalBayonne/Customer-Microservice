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
    last_name     varchar(255) null
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

-- Insert test customer with ID 5
insert into customer (id, birth_date, ssn, email_address, first_name, last_name)
values (1,'1890-07-11', 888888886, 'john.doe@gmail.pt', 'John', 'Doe');

-- Initialize the sequence to start from a value higher than any existing ID
insert into customer_seq (next_val) values (2);

