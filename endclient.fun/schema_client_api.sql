-- ============================================================
--  End Client Forever — миграция для клиентского API
--  (токены сессий + лог действий в клиенте)
--
--  ЗАПУСК: phpMyAdmin → выбери базу endmi0987_endclientbd → вкладка "Импорт"
--  → загрузи этот файл. Безопасно запускать повторно (IF NOT EXISTS).
-- ============================================================

SET NAMES utf8mb4;

-- Токены сессий клиента (выдаются при входе через клиент)
CREATE TABLE IF NOT EXISTS `client_tokens` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    INT UNSIGNED NOT NULL,
  `token`      CHAR(64) NOT NULL,
  `ip`         VARCHAR(45)  DEFAULT NULL,
  `user_agent` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_seen`  DATETIME DEFAULT NULL,
  `expires_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_client_token` (`token`),
  KEY `idx_tok_user` (`user_id`),
  CONSTRAINT `fk_tok_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Лог действий пользователя в клиенте (видно в админке)
CREATE TABLE IF NOT EXISTS `client_events` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    INT UNSIGNED DEFAULT NULL,
  `token_id`   BIGINT UNSIGNED DEFAULT NULL,
  `event_type` VARCHAR(40) NOT NULL,
  `detail`     VARCHAR(255) DEFAULT NULL,
  `ip`         VARCHAR(45)  DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ev_user` (`user_id`),
  KEY `idx_ev_type` (`event_type`),
  KEY `idx_ev_time` (`created_at`),
  CONSTRAINT `fk_ev_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
