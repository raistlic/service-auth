insert into permission (id, name)
values ('00000000-0000-0000-0000-000000000101', 'hello_world_message:read')
on conflict (name) do nothing;

insert into permission (id, name)
values ('00000000-0000-0000-0000-000000000102', 'hello_world_message:write')
on conflict (name) do nothing;

insert into app_user (id, username, password_hash)
values ('00000000-0000-0000-0000-000000000201', 'e2e-admin', '${e2e_admin_password_hash}')
on conflict (username) do update set password_hash = excluded.password_hash;

insert into app_user (id, username, password_hash)
values ('00000000-0000-0000-0000-000000000202', 'e2e-reader', '${e2e_reader_password_hash}')
on conflict (username) do update set password_hash = excluded.password_hash;

insert into app_user_permission (id, user_id, permission_id)
values ('00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000101')
on conflict (user_id, permission_id) do nothing;

insert into app_user_permission (id, user_id, permission_id)
values ('00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000102')
on conflict (user_id, permission_id) do nothing;

insert into app_user_permission (id, user_id, permission_id)
values ('00000000-0000-0000-0000-000000000303', '00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000101')
on conflict (user_id, permission_id) do nothing;
