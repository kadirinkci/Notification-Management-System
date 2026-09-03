CREATE TABLE notification_delivery_attempt (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    attempt_number INTEGER NOT NULL,
    outcome VARCHAR(30) NOT NULL,
    failure_reason TEXT,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_delivery_attempt_notification
        FOREIGN KEY (notification_id)
        REFERENCES notification(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_delivery_attempt_number
        UNIQUE (notification_id, attempt_number),

    CONSTRAINT chk_delivery_attempt_number
        CHECK (attempt_number > 0),

    CONSTRAINT chk_delivery_attempt_outcome
        CHECK (
            outcome IN (
                'SUCCESS',
                'TRANSIENT_FAILURE',
                'PERMANENT_FAILURE'
            )
        )
);
