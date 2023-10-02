INSERT INTO auth_system.roles (NAME, DESCRIPTION, PRIORITY_VALUE)
VALUES
    ('Admin', 'The master of the system', 0),
    ('Reviewer', 'Have the ability to review and mark other''s work', 10),
    ('User', 'A common user of the system with read-only permissions', 100)
ON CONFLICT DO NOTHING;

INSERT INTO auth_system.users (LOGIN, EMAIL, PASSWORD_HASH, REGISTRATION_TIMESTAMP, BIRTHDAY, ROLE_ID)
VALUES
    ('syakim', 'yakimychev@gmail.com', 'admin', NOW(), DATE('1997-08-26'), 1),
    ('defaultReviewer', 'someReviwerEmail@mail.com', 'some_hash', NOW(), DATE('1995-10-02'), 2),
    ('simpleUser', 'simpleUserEmail@gmail.com', 'hash', NOW(), DATE('1990-05-12'), 3)
ON CONFLICT DO NOTHING;
