<?php
/* POST /api/logout.php  { token }
   Завершает сессию клиента (удаляет токен). */
require_once __DIR__ . '/_common.php';

$token = (string) api_param('token', '');
$u = token_user($token);
if ($u) {
    log_client_event((int) $u['id'], (int) $u['token_id'], 'logout', 'Выход из клиента');
    db()->prepare('DELETE FROM client_tokens WHERE token = ?')->execute([$token]);
}

api_out(['ok' => true]);
