-- initial data
INSERT INTO "T_User" (user_id, email, nickname, password, application_role, status)
VALUES
(1, 'testuser@investigation.de', 'testy', '{bcrypt}$2a$10$/rbWcV1DDh.ra40c2Kdwf.nJaGtRsm6EsGyDMwGnanHvex20rsPI.', 'ADMIN', 'ACTIVE');
