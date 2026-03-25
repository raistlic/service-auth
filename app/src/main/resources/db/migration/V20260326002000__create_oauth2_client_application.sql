create table oauth2_client_application (
    client_id varchar(100) primary key,
    client_secret_hash varchar(255) not null,
    client_name varchar(255) not null
);

create table oauth2_client_redirect_uri (
    id varchar(36) primary key,
    client_application_id varchar(36) not null,
    redirect_uri varchar(2048) not null,
    constraint fk_oauth2_client_redirect_uri_application
        foreign key (client_application_id) references oauth2_client_application(client_id) on delete cascade,
    constraint uk_oauth2_client_redirect_uri_value
        unique (client_application_id, redirect_uri)
);

create table oauth2_client_grant_type (
    id varchar(36) primary key,
    client_application_id varchar(36) not null,
    grant_type varchar(100) not null,
    constraint fk_oauth2_client_grant_type_application
        foreign key (client_application_id) references oauth2_client_application(client_id) on delete cascade,
    constraint uk_oauth2_client_grant_type_value
        unique (client_application_id, grant_type)
);

create table oauth2_client_scope (
    id varchar(36) primary key,
    client_application_id varchar(36) not null,
    scope varchar(100) not null,
    constraint fk_oauth2_client_scope_application
        foreign key (client_application_id) references oauth2_client_application(client_id) on delete cascade,
    constraint uk_oauth2_client_scope_value
        unique (client_application_id, scope)
);
