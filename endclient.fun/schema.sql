-- ============================================================
--  End Client Forever — схема базы данных (v2: подписки + промокоды + роли)
--  БД: endmi0987_endclientbd
--  Домен: https://endclient.fun
--  ИМПОРТ: phpMyAdmin → выбери базу → вкладка "Импорт" → загрузи этот файл.
-- ============================================================

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- ------------------------------------------------------------
--  Пользователи (роли: user / tester / owner)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username`        VARCHAR(32)  NOT NULL,
  `email`           VARCHAR(190) NOT NULL,
  `password_hash`   VARCHAR(255) NOT NULL,
  `display_name`    VARCHAR(64)  DEFAULT NULL,
  `bio`             VARCHAR(280) DEFAULT NULL,
  `avatar_color`    CHAR(7)      NOT NULL DEFAULT '#e11d2a',
  `role`            ENUM('user','tester','owner') NOT NULL DEFAULT 'user',
  `downloads_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `is_public`       TINYINT(1)   NOT NULL DEFAULT 1,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login`      DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_username` (`username`),
  UNIQUE KEY `uq_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
--  Лог скачиваний
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `downloads` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`        INT UNSIGNED NOT NULL,
  `client_version` VARCHAR(20) NOT NULL DEFAULT '0.6-test',
  `ip`             VARCHAR(45)  DEFAULT NULL,
  `user_agent`     VARCHAR(255) DEFAULT NULL,
  `downloaded_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  CONSTRAINT `fk_dl_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
--  Подписки (expires_at = NULL означает "навсегда")
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `subscriptions` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    INT UNSIGNED NOT NULL,
  `plan`       VARCHAR(40) NOT NULL DEFAULT 'Forever Lite',
  `source`     ENUM('purchase','promo','manual') NOT NULL DEFAULT 'manual',
  `started_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sub_user` (`user_id`),
  KEY `idx_sub_exp` (`expires_at`),
  CONSTRAINT `fk_sub_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
--  Промокоды (duration_days = 0 означает "навсегда"; max_uses = 0 — без лимита)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `promo_codes` (
  `id`            INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`          VARCHAR(64) NOT NULL,
  `plan`          VARCHAR(40) NOT NULL DEFAULT 'Forever Lite',
  `duration_days` INT NOT NULL DEFAULT 0,
  `max_uses`      INT NOT NULL DEFAULT 0,
  `used_count`    INT NOT NULL DEFAULT 0,
  `is_active`     TINYINT(1) NOT NULL DEFAULT 1,
  `note`          VARCHAR(190) DEFAULT NULL,
  `expires_at`    DATETIME DEFAULT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
--  Активации промокодов (защита от повторного использования)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `promo_redemptions` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code_id`         INT UNSIGNED NOT NULL,
  `user_id`         INT UNSIGNED NOT NULL,
  `subscription_id` BIGINT UNSIGNED DEFAULT NULL,
  `redeemed_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_code_user` (`code_id`, `user_id`),
  KEY `idx_red_user` (`user_id`),
  CONSTRAINT `fk_red_code` FOREIGN KEY (`code_id`) REFERENCES `promo_codes`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_red_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  НАЧАЛЬНЫЕ ДАННЫЕ
-- ============================================================

-- Первый промокод: FIRSTLIGHT — 2 дня бесплатного доступа Forever Lite (без лимита активаций)
INSERT INTO `promo_codes` (`code`, `plan`, `duration_days`, `max_uses`, `is_active`, `note`)
VALUES ('FIRSTLIGHT', 'Forever Lite', 2, 0, 1, 'Первый промокод — 2 дня бесплатного доступа')
ON DUPLICATE KEY UPDATE `code` = `code`;

-- ============================================================
--  МИГРАЦИЯ СО СТАРОЙ ВЕРСИИ (выполни ТОЛЬКО если таблица users уже
--  была создана со старыми ролями user/vip/admin). Убери "--" перед строками.
-- ============================================================
-- UPDATE `users` SET `role` = 'owner'  WHERE `role` = 'admin';
-- UPDATE `users` SET `role` = 'tester' WHERE `role` = 'vip';
-- ALTER TABLE `users` MODIFY `role` ENUM('user','tester','owner') NOT NULL DEFAULT 'user';

-- ============================================================
--  НАЗНАЧИТЬ ВЛАДЕЛЬЦА ВРУЧНУЮ (первый зарегистрировавшийся становится owner автоматически;
--  если нужно вручную — убери "--" и впиши свой логин)
-- ============================================================
-- UPDATE `users` SET `role` = 'owner' WHERE `username` = 'ForeverLowTab';

-- ============================================================
--  ПРИМЕРЫ ЗАПРОСОВ (ЗАКОММЕНТИРОВАНЫ; :placeholder — для PHP/PDO)
-- ============================================================
-- Активная подписка пользователя:
-- SELECT * FROM subscriptions
-- WHERE user_id = :uid AND (expires_at IS NULL OR expires_at > NOW())
-- ORDER BY (expires_at IS NULL) DESC, expires_at DESC LIMIT 1;
--
-- Сколько активных подписок всего:
-- SELECT COUNT(DISTINCT user_id) FROM subscriptions
-- WHERE expires_at IS NULL OR expires_at > NOW();
