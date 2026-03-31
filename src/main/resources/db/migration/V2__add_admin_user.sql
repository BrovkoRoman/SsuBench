INSERT INTO users (login, password, role, money, blocked)
VALUES ('admin',
        '$2a$12$5FN/U1kewhd4M2dNdTNTmO4Ds6Z0Cgq.05l/xr6EWhzLs0jrk.gIe', -- password='admin'
        0,
        1000,
        false) ON CONFLICT (login) DO NOTHING;