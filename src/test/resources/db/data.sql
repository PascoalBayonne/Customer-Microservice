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

-- Insert test customer with ID 5
insert into customer (id, birth_date, ssn, email_address, first_name, last_name, created_at )
values (1,'1890-07-11', 888888886, 'john.doe@gmail.pt', 'John', 'Doe', '');

-- Initialize the sequence to start from a value higher than any existing ID
insert into customer_seq (next_val) values (2);
