<?php
require_once __DIR__ . '/../config.php';

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

/** Экранирование вывода */
function e($s): string {
    return htmlspecialchars((string)$s, ENT_QUOTES, 'UTF-8');
}

/** CSRF-токен */
function csrf_token(): string {
    if (empty($_SESSION['csrf'])) {
        $_SESSION['csrf'] = bin2hex(random_bytes(32));
    }
    return $_SESSION['csrf'];
}
function check_csrf($t): bool {
    return !empty($_SESSION['csrf']) && is_string($t) && hash_equals($_SESSION['csrf'], $t);
}

/** Текущий пользователь (или null) */
function current_user(): ?array {
    if (empty($_SESSION['user_id'])) {
        return null;
    }
    static $cached = false;
    static $user = null;
    if ($cached === false) {
        $st = db()->prepare('SELECT * FROM users WHERE id = ?');
        $st->execute([$_SESSION['user_id']]);
        $user = $st->fetch() ?: null;
        $cached = true;
    }
    return $user;
}

/** Требовать авторизацию */
function require_login(): void {
    if (!current_user()) {
        redirect('login.php?next=' . urlencode($_SERVER['REQUEST_URI'] ?? 'dashboard.php'));
    }
}

function redirect(string $url): void {
    header('Location: ' . $url);
    exit;
}

/** Инициалы для аватарки */
function initials(string $name): string {
    $name = trim($name);
    if ($name === '') return '?';
    $parts = preg_split('/\\s+/', $name);
    $s = mb_substr($parts[0], 0, 1);
    if (count($parts) > 1) $s .= mb_substr($parts[1], 0, 1);
    return mb_strtoupper($s);
}

/** Красивая дата */
function human_date(?string $dt): string {
    if (!$dt) return '—';
    $ts = strtotime($dt);
    return $ts ? date('d.m.Y', $ts) : '—';
}

/** Дата и время */
function human_datetime(?string $dt): string {
    if (!$dt) return '—';
    $ts = strtotime($dt);
    return $ts ? date('d.m.Y H:i', $ts) : '—';
}

/** Роли */
function is_owner(?array $user): bool {
    return $user && ($user['role'] ?? '') === 'owner';
}
function is_staff(?array $user): bool {
    return $user && in_array($user['role'] ?? '', ['tester', 'owner'], true);
}
function role_label(string $role): string {
    $m = ['user' => 'Участник', 'tester' => 'Тестер', 'owner' => 'Владелец'];
    return $m[$role] ?? $role;
}

/** Понятное название типа события клиента (для админ-панели) */
function client_event_label(string $type): string {
    $m = [
        'login'         => 'Вход',
        'logout'        => 'Выход',
        'login_failed'  => 'Ошибка входа',
        'launch'        => 'Запуск клиента',
        'session'       => 'Проверка сессии',
        'open_clickgui' => 'Открыт ClickGUI',
        'open_account'  => 'Открыт кабинет',
        'open_menu'     => 'Главное меню',
        'toggle_module' => 'Модуль',
        'theme'         => 'Смена темы',
        'download'      => 'Скачивание',
        'join_world'    => 'Одиночная игра',
        'join_server'   => 'Сетевая игра',
    ];
    return $m[$type] ?? $type;
}

/** Активная подписка пользователя (или null) */
function active_subscription(int $userId): ?array {
    $st = db()->prepare(
        'SELECT * FROM subscriptions
         WHERE user_id = ? AND (expires_at IS NULL OR expires_at > NOW())
         ORDER BY (expires_at IS NULL) DESC, expires_at DESC
         LIMIT 1'
    );
    $st->execute([$userId]);
    return $st->fetch() ?: null;
}
function has_active_sub(int $userId): bool {
    return active_subscription($userId) !== null;
}

/** Активировать промокод. Возвращает [bool ok, string msg]. */
function redeem_promo(string $code, int $userId): array {
    $code = strtoupper(trim($code));
    if ($code === '') return [false, 'Введите промокод.'];

    $pdo = db();
    $st = $pdo->prepare('SELECT * FROM promo_codes WHERE code = ? LIMIT 1');
    $st->execute([$code]);
    $promo = $st->fetch();

    if (!$promo || (int)$promo['is_active'] !== 1) return [false, 'Промокод не найден или отключён.'];
    if ((int)$promo['max_uses'] > 0 && (int)$promo['used_count'] >= (int)$promo['max_uses'])
        return [false, 'У этого промокода закончились активации.'];
    if (!empty($promo['expires_at']) && strtotime($promo['expires_at']) < time())
        return [false, 'Срок действия промокода истёк.'];

    $st = $pdo->prepare('SELECT 1 FROM promo_redemptions WHERE code_id = ? AND user_id = ? LIMIT 1');
    $st->execute([$promo['id'], $userId]);
    if ($st->fetch()) return [false, 'Вы уже активировали этот промокод.'];

    $days = (int)$promo['duration_days'];
    $expires = $days > 0 ? date('Y-m-d H:i:s', time() + $days * 86400) : null;

    try {
        $pdo->beginTransaction();
        $ins = $pdo->prepare('INSERT INTO subscriptions (user_id, plan, source, started_at, expires_at) VALUES (?, ?, ?, NOW(), ?)');
        $ins->execute([$userId, $promo['plan'], 'promo', $expires]);
        $subId = (int)$pdo->lastInsertId();
        $pdo->prepare('INSERT INTO promo_redemptions (code_id, user_id, subscription_id) VALUES (?, ?, ?)')
            ->execute([$promo['id'], $userId, $subId]);
        $pdo->prepare('UPDATE promo_codes SET used_count = used_count + 1 WHERE id = ?')->execute([$promo['id']]);
        $pdo->commit();
    } catch (PDOException $ex) {
        if ($pdo->inTransaction()) $pdo->rollBack();
        return [false, 'Не удалось активировать промокод. Попробуйте позже.'];
    }

    $human = $days > 0 ? ('на ' . $days . ' дн.') : 'навсегда';
    return [true, 'Промокод активирован! Подписка «' . $promo['plan'] . '» ' . $human . '.'];
}
