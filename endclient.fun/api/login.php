<?php
/* POST /api/login.php  { username, password }
   Проверяет логин/пароль, выдаёт токен сессии и профиль для клиента. */
require_once __DIR__ . '/_common.php';

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
    api_fail('Требуется POST-запрос.', 405);
}

$login = trim((string) api_param('username', ''));
$pass  = (string) api_param('password', '');

if ($login === '' || $pass === '') {
    api_fail('Введите имя пользователя и пароль.', 422);
}

$st = db()->prepare('SELECT * FROM users WHERE username = ? OR email = ? LIMIT 1');
$st->execute([$login, $login]);
$u = $st->fetch();

if (!$u || !password_verify($pass, $u['password_hash'])) {
    log_client_event($u ? (int) $u['id'] : null, null, 'login_failed', 'Неверные данные для входа: ' . $login);
    api_fail('Неверный логин или пароль.', 401);
}

$token   = make_token();
$expires = date('Y-m-d H:i:s', time() + 30 * 86400); // сессия живёт 30 дней

db()->prepare(
    'INSERT INTO client_tokens (user_id, token, ip, user_agent, created_at, last_seen, expires_at)
     VALUES (?, ?, ?, ?, NOW(), NOW(), ?)'
)->execute([$u['id'], $token, client_ip(), client_ua(), $expires]);
$tokenId = (int) db()->lastInsertId();

db()->prepare('UPDATE users SET last_login = NOW() WHERE id = ?')->execute([$u['id']]);
log_client_event((int) $u['id'], $tokenId, 'login', 'Вход через клиент');

$payload = client_profile_payload($u);
$payload['ok']       = true;
$payload['message']  = 'Добро пожаловать, ' . $u['username'] . '!';
$payload['token']    = $token;
$payload['download']['url'] = SITE_URL . '/api/download.php?token=' . urlencode($token);

api_out($payload);
