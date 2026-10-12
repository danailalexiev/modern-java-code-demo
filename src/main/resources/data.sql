-- user 1: active, has a payment method on file
INSERT INTO users (id, email, plan, suspended, payment_method_on_file)
VALUES (1, 'alice@example.com', 'FREE', false, true);

-- user 2: active, but no payment method on file (Act 0's 500)
INSERT INTO users (id, email, plan, suspended, payment_method_on_file)
VALUES (2, 'bob@example.com', 'FREE', false, false);

-- user 3: suspended
INSERT INTO users (id, email, plan, suspended, payment_method_on_file)
VALUES (3, 'carol@example.com', 'FREE', true, true);
