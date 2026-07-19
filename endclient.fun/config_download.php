<?php
/* GET /config_download.php  — скачать свой облачный конфиг файлом (требует входа). */
require_once __DIR__ . '/includes/functions.php';
require_login();
$u = current_user();
$st = db()->prepare('SELECT data FROM client_configs WHERE user_id = ? LIMIT 1');
$st->execute([$u['id']]);
$row = $st->fetch();
$data = $row ? $row['data'] : '{}';
header('Content-Type: application/json; charset=utf-8');
header('Content-Disposition: attachment; filename="endclientforever-config.json"');
header('Content-Length: ' . strlen($data));
echo $data;
