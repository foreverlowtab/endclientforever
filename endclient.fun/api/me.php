<?php
/* POST /api/me.php  { token }
   Возвращает свежий профиль и подписку для личного кабинета в клиенте.
   Используется для восстановления сессии при запуске и кнопки "Обновить". */
require_once __DIR__ . '/_common.php';

$token = (string) api_param('token', '');
$u = token_user($token);
if (!$u) {
    api_fail('Сессия недействительна. Войдите заново.', 401);
}

$payload = client_profile_payload($u);
$payload['ok']    = true;
$payload['token'] = $token;
$payload['download']['url'] = SITE_URL . '/api/download.php?token=' . urlencode($token);

api_out($payload);
