<?php
/* GET /api/download.php?token=...
   Отдаёт .jar клиента только по действующему токену и при наличии
   активной подписки (или роли tester/owner). Пишет лог скачивания. */
require_once __DIR__ . '/../includes/functions.php';

function dl_error(int $code, string $text): void {
    http_response_code($code);
    header('Content-Type: text/plain; charset=utf-8');
    echo $text;
    exit;
}

$token = (string) ($_GET['token'] ?? $_POST['token'] ?? '');
if ($token === '') {
    dl_error(401, 'Не передан токен. Войдите в клиент заново.');
}

$st = db()->prepare(
    'SELECT t.id AS token_id, u.*
     FROM client_tokens t
     JOIN users u ON u.id = t.user_id
     WHERE t.token = ? AND (t.expires_at IS NULL OR t.expires_at > NOW())
     LIMIT 1'
);
$st->execute([$token]);
$u = $st->fetch();
if (!$u) {
    dl_error(401, 'Недействительная сессия. Войдите в клиент заново.');
}

$hasSub  = active_subscription((int) $u['id']) !== null;
$isStaff = in_array($u['role'], ['tester', 'owner'], true);
if (!$hasSub && !$isStaff) {
    dl_error(403, 'Для скачивания нужна активная подписка «' . SUB_PLAN . '».');
}

$file = CLIENT_FILE;
if (!is_file($file)) {
    dl_error(404, 'Файл клиента ещё не загружен на сервер.');
}

try {
    $pdo = db();
    $pdo->beginTransaction();
    $pdo->prepare('INSERT INTO downloads (user_id, client_version, ip, user_agent) VALUES (?, ?, ?, ?)')
        ->execute([
            $u['id'],
            CLIENT_VERSION,
            $_SERVER['REMOTE_ADDR'] ?? null,
            mb_substr($_SERVER['HTTP_USER_AGENT'] ?? '', 0, 255),
        ]);
    $pdo->prepare('UPDATE users SET downloads_count = downloads_count + 1 WHERE id = ?')->execute([$u['id']]);
    $pdo->prepare(
        'INSERT INTO client_events (user_id, token_id, event_type, detail, ip, created_at)
         VALUES (?, ?, ?, ?, ?, NOW())'
    )->execute([$u['id'], $u['token_id'], 'download', 'Скачивание клиента v' . CLIENT_VERSION, $_SERVER['REMOTE_ADDR'] ?? null]);
    $pdo->commit();
} catch (PDOException $e) {
    if (db()->inTransaction()) db()->rollBack();
}

while (ob_get_level() > 0) { ob_end_clean(); }
header('Content-Type: application/java-archive');
header('Content-Disposition: attachment; filename="' . CLIENT_FILE_NAME . '"');
header('Content-Length: ' . filesize($file));
header('X-Content-Type-Options: nosniff');
readfile($file);
exit;
