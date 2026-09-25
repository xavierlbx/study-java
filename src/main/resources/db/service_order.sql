CREATE TABLE service_order (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  protocol          VARCHAR(20)  NOT NULL UNIQUE,
  customer_name     VARCHAR(120) NOT NULL,
  customer_document VARCHAR(14)  NOT NULL,
  type              VARCHAR(20)  NOT NULL,   -- INSTALLATION | REPAIR | RELOCATION
  status            VARCHAR(20)  NOT NULL,   -- OPEN | SCHEDULED | IN_PROGRESS | DONE | CANCELED
  scheduled_date    DATE         NULL,
  notes             VARCHAR(500) NULL,
  created_at        DATETIME     NOT NULL,
  updated_at        DATETIME     NOT NULL,
  deleted_at        DATETIME     NULL
);
