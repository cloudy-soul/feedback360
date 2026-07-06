CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL CHECK (role IN ('EMPLOYEE','MANAGER','ADMIN')),
    department      VARCHAR(100),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE module_formation (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id),
    title                   VARCHAR(255) NOT NULL,
    description             VARCHAR(500),
    category                VARCHAR(150),
    completion_date         TIMESTAMP NOT NULL,
    source                  VARCHAR(50) NOT NULL DEFAULT 'TalentUp',
    talentup_module_id      BIGINT NOT NULL,
    talentup_parcours_id    BIGINT,
    talentup_parcours_name  VARCHAR(150),
    talentup_population_id  BIGINT,
    talentup_population_name VARCHAR(150),
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_module_user_talentup UNIQUE (user_id, talentup_module_id)
);

CREATE TABLE question_feedback (
    id              BIGSERIAL PRIMARY KEY,
    label           VARCHAR(500) NOT NULL,
    type            VARCHAR(20)  NOT NULL CHECK (type IN ('RATING','TEXT','CHOICE')),
    required        BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order   INT          NOT NULL DEFAULT 0,
    active          BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE feedback (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    module_id       BIGINT NOT NULL REFERENCES module_formation(id),
    rating          SMALLINT CHECK (rating BETWEEN 0 AND 10),
    status          VARCHAR(20) NOT NULL DEFAULT 'NOT_SUBMITTED'
                        CHECK (status IN ('NOT_SUBMITTED','IN_PROGRESS','SUBMITTED')),
    comment         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    submitted_at    TIMESTAMP,
    CONSTRAINT uq_feedback_user_module UNIQUE (user_id, module_id)
);

CREATE TABLE response_feedback (
    id              BIGSERIAL PRIMARY KEY,
    feedback_id     BIGINT NOT NULL REFERENCES feedback(id) ON DELETE CASCADE,
    question_id     BIGINT NOT NULL REFERENCES question_feedback(id),
    response_value  TEXT NOT NULL,
    CONSTRAINT uq_response_feedback_question UNIQUE (feedback_id, question_id)
);

CREATE TABLE notification (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    module_id       BIGINT NOT NULL REFERENCES module_formation(id),
    sent_date       TIMESTAMP NOT NULL DEFAULT now(),
    status          VARCHAR(20) NOT NULL DEFAULT 'SENT'
                        CHECK (status IN ('SENT','READ','RESPONDED','FAILED')),
    type            VARCHAR(20) NOT NULL DEFAULT 'INITIAL'
                        CHECK (type IN ('INITIAL','REMINDER')),
    reminder_count  INT NOT NULL DEFAULT 0
);

CREATE TABLE integration_log (
    id              BIGSERIAL PRIMARY KEY,
    type            VARCHAR(40) NOT NULL,
    status          VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS','FAILED')),
    message         TEXT,
    related_module_id BIGINT REFERENCES module_formation(id),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE system_setting (
    setting_key     VARCHAR(100) PRIMARY KEY,
    setting_value   VARCHAR(1000) NOT NULL
);

CREATE INDEX idx_feedback_status ON feedback(status);
CREATE INDEX idx_notification_status_type ON notification(status, type);
CREATE INDEX idx_module_completion_date ON module_formation(completion_date);