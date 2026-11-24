-- Users ------------------------------------------------------------------
CREATE TABLE users (
    id            UUID         PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_users_email ON users (lower(email));

-- Accounts ---------------------------------------------------------------
CREATE TABLE accounts (
    id              UUID         PRIMARY KEY,
    user_id         UUID         NOT NULL REFERENCES users (id),
    name            VARCHAR(100) NOT NULL,
    type            VARCHAR(10)  NOT NULL CHECK (type IN ('CURRENT', 'SAVINGS', 'CASH')),
    initial_balance BIGINT       NOT NULL DEFAULT 0,          -- cents, may be negative
    is_archived     BOOLEAN      NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_accounts_id_user UNIQUE (id, user_id)
);
CREATE INDEX ix_accounts_user ON accounts (user_id);

-- Categories -------------------------------------------------------------
CREATE TABLE categories (
    id          UUID        PRIMARY KEY,
    user_id     UUID        NOT NULL REFERENCES users (id),
    name        VARCHAR(50) NOT NULL,
    type        VARCHAR(7)  NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    color       CHAR(7)     NOT NULL CHECK (color ~ '^#[0-9A-Fa-f]{6}$'),
    is_archived BOOLEAN     NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_categories_id_user_type UNIQUE (id, user_id, type)
);
CREATE INDEX ix_categories_user_type ON categories (user_id, type);

-- Transactions (income and expenses) -------------------------------------
CREATE TABLE transactions (
    id          UUID         PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id),
    account_id  UUID         NOT NULL,
    category_id UUID         NOT NULL,
    type        VARCHAR(7)   NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    amount      BIGINT       NOT NULL CHECK (amount > 0),     -- cents
    description VARCHAR(255),
    date        DATE         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id, user_id) REFERENCES accounts (id, user_id),
    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id, user_id, type) REFERENCES categories (id, user_id, type)
);
CREATE INDEX ix_transactions_user_date ON transactions (user_id, date DESC, created_at DESC);
CREATE INDEX ix_transactions_account   ON transactions (account_id, date);
CREATE INDEX ix_transactions_category  ON transactions (category_id);

-- Transfers (between the user's own accounts) ----------------------------
CREATE TABLE transfers (
    id              UUID         PRIMARY KEY,
    user_id         UUID         NOT NULL REFERENCES users (id),
    from_account_id UUID         NOT NULL,
    to_account_id   UUID         NOT NULL,
    amount          BIGINT       NOT NULL CHECK (amount > 0), -- cents
    description     VARCHAR(255),
    date            DATE         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_transfers_from
        FOREIGN KEY (from_account_id, user_id) REFERENCES accounts (id, user_id),
    CONSTRAINT fk_transfers_to
        FOREIGN KEY (to_account_id, user_id) REFERENCES accounts (id, user_id),
    CONSTRAINT ck_transfers_different_accounts CHECK (from_account_id <> to_account_id)
);
CREATE INDEX ix_transfers_user_date ON transfers (user_id, date DESC, created_at DESC);
CREATE INDEX ix_transfers_from      ON transfers (from_account_id, date);
CREATE INDEX ix_transfers_to        ON transfers (to_account_id, date);
