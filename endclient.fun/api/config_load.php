<?php
/* POST /api/config_load.php  { token }
   Возвращает облачный конфиг пользователя (JSON-строка) или пусто. */
require_once __DIR__ . '/_common.php';

$token = (string) api_param('token', '');
$u = token_user($token);
if (!$u) {
    api_fail('Сессия недействительна. Войдите заново.', 401);
}

$uid = (int) $u['id'];
$st = db()->prepare('SELECT data, updated_at FROM client_configs WHERE user_id = ? LIMIT 1');
$st->execute([$uid]);
$row = $st->fetch();

log_client_event($uid, (int) $u['token_id'], 'config_load', 'Загрузка конфига из облака');
api_out([
    'ok'         => true,
    'data'       => $row ? $row['data'] : '',
    'updated_at' => $row ? $row['updated_at'] : null,
]);
