<?php
/* ============================================================
   End Client Forever — общий бутстрап для клиентского API
   Все эндпоинты в /api/ подключают этот файл.
   Отдаёт JSON, проверяет токены, пишет лог действий клиента.
   ============================================================ */

require_once __DIR__ . '/../includes/functions.php';

header('Content-Type: application/json; charset=utf-8');
header('X-Content-Type-Options: nosniff');
header('Cache-Control: no-store');

/** Отдать JSON и завершить. */
function api_out(array $data, int $code = 200): void {
    http_response_code($code);
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

/** Ошибка в едином формате. */
function api_fail(string $message, int $code = 400): void {
    api_out(['ok' => false, 'message' => $message], $code);
}

/** Достаём параметр из POST / GET / JSON-тела. */
function api_param(string $key, $default = null) {
    if (isset($_POST[$key])) return $_POST[$key];
    if (isset($_GET[$key]))  return $_GET[$key];
    static $json = null;
    if ($json === null) {
        $raw  = file_get_contents('php://input');
        $json = $raw ? (json_decode($raw, true) ?: []) : [];
    }
    return $json[$key] ?? $default;
}

function client_ip(): ?string {
    return $_SERVER['REMOTE_ADDR'] ?? null;
}

function client_ua(): ?string {
    return isset($_SERVER['HTTP_USER_AGENT']) ? mb_substr($_SERVER['HTTP_USER_AGENT'], 0, 255) : null;
}

/** Сгенерировать токен сессии клиента. */
function make_token(): string {
    return bin2hex(random_bytes(32));
}

/**
 * Найти пользователя по токену. Обновляет last_seen.
 * Возвращает строку с полями users.* + token_id + token_uid, либо null.
 */
function token_user(string $token): ?array {
    if ($token === '') return null;
    $st = db()->prepare(
        'SELECT t.id AS token_id, t.user_id AS token_uid, u.*
         FROM client_tokens t
         JOIN users u ON u.id = t.user_id
         WHERE t.token = ? AND (t.expires_at IS NULL OR t.expires_at > NOW())
         LIMIT 1'
    );
    $st->execute([$token]);
    $row = $st->fetch();
    if (!$row) return null;
    try {
        db()->prepare('UPDATE client_tokens SET last_seen = NOW(), ip = ?, user_agent = ? WHERE id = ?')
            ->execute([client_ip(), client_ua(), $row['token_id']]);
    } catch (PDOException $e) { /* не критично */ }
    return $row;
}

/** Записать действие клиента в лог (видно в админке). */
function log_client_event(?int $userId, ?int $tokenId, string $type, string $detail): void {
    try {
        db()->prepare(
            'INSERT INTO client_events (user_id, token_id, event_type, detail, ip, created_at)
             VALUES (?, ?, ?, ?, ?, NOW())'
        )->execute([
            $userId,
            $tokenId,
            mb_substr($type, 0, 40),
            mb_substr($detail, 0, 255),
            client_ip(),
        ]);
    } catch (PDOException $e) { /* лог не должен ломать ответ */ }
}

/** Собрать данные профиля для клиента (личный кабинет). */
function client_profile_payload(array $u): array {
    $uid = (int) $u['id'];
    $sub = active_subscription($uid);
    $canDownload = ($sub !== null) || in_array($u['role'], ['tester', 'owner'], true);

    $expires = 'none';
    if ($sub) {
        $expires = empty($sub['expires_at']) ? 'forever' : human_date($sub['expires_at']);
    }

    return [
        'user' => [
            'username'     => $u['username'],
            'display_name' => ($u['display_name'] ?? '') !== '' ? $u['display_name'] : $u['username'],
            'role'         => $u['role'],
            'role_label'   => role_label($u['role']),
            'downloads'    => (int) ($u['downloads_count'] ?? 0),
            'member_since' => human_date($u['created_at'] ?? null),
            'avatar_color' => $u['avatar_color'] ?? '#e11d2a',
        ],
        'subscription' => [
            'active'  => $sub !== null,
            'plan'    => $sub ? $sub['plan'] : SUB_PLAN,
            'expires' => $expires,
        ],
        'download' => [
            'allowed' => $canDownload,
            'url'     => SITE_URL . '/api/download.php',
        ],
    ];
}
