<?php
/* POST /api/config_save.php  { token, data }
   Сохраняет облачный конфиг клиента (JSON-строка) — один на пользователя. */
require_once __DIR__ . '/_common.php';

$token = (string) api_param('token', '');
$u = token_user($token);
if (!$u) {
    api_fail('Сессия недействительна. Войдите заново.', 401);
}

$data = (string) api_param('data', '');
if ($data === '') {
    api_fail('Пустой конфиг.', 422);
}
if (strlen($data) > 200000) {
    api_fail('Конфиг слишком большой.', 413);
}
json_decode($data);
if (json_last_error() !== JSON_ERROR_NONE) {
    api_fail('Некорректный JSON.', 422);
}

$uid = (int) $u['id'];
db()->prepare(
    'INSERT INTO client_configs (user_id, data, updated_at) VALUES (?, ?, NOW())
     ON DUPLICATE KEY UPDATE data = VALUES(data), updated_at = NOW()'
)->execute([$uid, $data]);

log_client_event($uid, (int) $u['token_id'], 'config_save', 'Конфиг сохранён в облако (' . strlen($data) . ' б)');
api_out(['ok' => true, 'message' => 'Конфиг сохранён в облако.']);
