create table app_user (
    id varchar(36) primary key,
    username varchar(255) not null,
    password_hash varchar(255) not null,
    constraint uk_app_user_username unique (username)
);

create table permission (
    id varchar(36) primary key,
    name varchar(255) not null,
    constraint uk_permission_name unique (name)
);

create table app_user_permission (
    id varchar(36) primary key,
    user_id varchar(36) not null,
    permission_id varchar(36) not null,
    constraint fk_app_user_permission_user
        foreign key (user_id) references app_user(id) on delete cascade,
    constraint fk_app_user_permission_permission
        foreign key (permission_id) references permission(id) on delete cascade,
    constraint uk_app_user_permission_pair unique (user_id, permission_id)
);
