CREATE TABLE event (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       max_participants INT NOT NULL,
                       start_at TIMESTAMP NOT NULL,
                       end_at TIMESTAMP NOT NULL,
                       status VARCHAR(50) NOT NULL,
                       created_at DATETIME NOT NULL,
);

CREATE TABLE event_participant (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   member_id BIGINT NOT NULL,
                                   event_id BIGINT NOT NULL,
                                   created_at TIMESTAMP NOT NULL,
                                   CONSTRAINT fk_event_participant_event
                                       FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE
);

INSERT INTO event (name, max_participants, start_at, end_at, status, created_at, updated_at)
VALUES (
           '선착순 10000명 이벤트',
           10000,
           '2025-07-01 00:00:00',
           '2025-07-10 23:59:59',
           'OPEN',
           NOW(),
       );
