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

-- Create the sequence table if it doesn't exist
create table if not exists customer_seq
(
    next_val bigint null
);

