<?php
/* POST /api/event.php  { token, type, detail }
   Клиент шлёт сюда действия пользователя (открыл ClickGUI, включил модуль,
   зашёл в кабинет и т.д.). Всё это видно владельцу в админ-панели. */
require_once __DIR__ . '/_common.php';

$token = (string) api_param('token', '');
$u = token_user($token);
if (!$u) {
    api_fail('Сессия недействительна.', 401);
}

$type   = trim((string) api_param('type', 'event'));
$detail = trim((string) api_param('detail', ''));
if ($type === '') $type = 'event';

log_client_event((int) $u['id'], (int) $u['token_id'], $type, $detail);
api_out(['ok' => true]);
