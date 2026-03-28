merge into permission as target
using (values ('30000000-0000-0000-0000-000000000101', 'hello_world_message:read')) as source(id, name)
on target.name = source.name
when not matched then
    insert (id, name) values (source.id, source.name);

merge into permission as target
using (values ('30000000-0000-0000-0000-000000000102', 'hello_world_message:write')) as source(id, name)
on target.name = source.name
when not matched then
    insert (id, name) values (source.id, source.name);

merge into app_user as target
using (
    values (
        '30000000-0000-0000-0000-000000000201',
        'dev-admin',
        '$2a$10$wO7Ig7MwwdMiMDBWDX3DVOeIYpBGTj3Zq1ORG9E7cWKYkrgkU.rna'
    )
) as source(id, username, password_hash)
on target.username = source.username
when not matched then
    insert (id, username, password_hash) values (source.id, source.username, source.password_hash);

merge into app_user as target
using (
    values (
        '30000000-0000-0000-0000-000000000202',
        'dev-reader',
        '$2a$10$GG9LwnB1Xd5jDZlJ9svgAeDHPfZ9Ga7WNexqFSk6Byf0Ti7/1pO8u'
    )
) as source(id, username, password_hash)
on target.username = source.username
when not matched then
    insert (id, username, password_hash) values (source.id, source.username, source.password_hash);

merge into app_user_permission as target
using (
    values (
        '30000000-0000-0000-0000-000000000301',
        '30000000-0000-0000-0000-000000000201',
        '30000000-0000-0000-0000-000000000101'
    )
) as source(id, user_id, permission_id)
on target.user_id = source.user_id and target.permission_id = source.permission_id
when not matched then
    insert (id, user_id, permission_id) values (source.id, source.user_id, source.permission_id);

merge into app_user_permission as target
using (
    values (
        '30000000-0000-0000-0000-000000000302',
        '30000000-0000-0000-0000-000000000201',
        '30000000-0000-0000-0000-000000000102'
    )
) as source(id, user_id, permission_id)
on target.user_id = source.user_id and target.permission_id = source.permission_id
when not matched then
    insert (id, user_id, permission_id) values (source.id, source.user_id, source.permission_id);

merge into app_user_permission as target
using (
    values (
        '30000000-0000-0000-0000-000000000303',
        '30000000-0000-0000-0000-000000000202',
        '30000000-0000-0000-0000-000000000101'
    )
) as source(id, user_id, permission_id)
on target.user_id = source.user_id and target.permission_id = source.permission_id
when not matched then
    insert (id, user_id, permission_id) values (source.id, source.user_id, source.permission_id);
