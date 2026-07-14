<?php
require_once __DIR__ . '/includes/functions.php';

// ГЕЙТ: скачать может только авторизованный пользователь
require_login();
$u = current_user();

$file = CLIENT_FILE;
if (!is_file($file)) {
    http_response_code(404);
    require __DIR__ . '/includes/functions.php';
    $page_title = 'Файл не найден';
    require __DIR__ . '/includes/header.php';
    echo '<div class="auth-wrap"><div class="auth-card"><h1>Файл клиента пока не загружен</h1><p class="sub">Загрузи сборку в папку /downloads на хостинге.</p><a href="dashboard.php" class="btn btn-primary btn-block">Назад в кабинет</a></div></div>';
    require __DIR__ . '/includes/footer.php';
    exit;
}

// Логируем скачивание + счётчик (транзакция)
try {
    $pdo = db();
    $pdo->beginTransaction();
    $st = $pdo->prepare('INSERT INTO downloads (user_id, client_version, ip, user_agent) VALUES (?, ?, ?, ?)');
    $st->execute([
        $u['id'],
        CLIENT_VERSION,
        $_SERVER['REMOTE_ADDR'] ?? null,
        mb_substr($_SERVER['HTTP_USER_AGENT'] ?? '', 0, 255),
    ]);
    $pdo->prepare('UPDATE users SET downloads_count = downloads_count + 1 WHERE id = ?')->execute([$u['id']]);
    $pdo->commit();
} catch (PDOException $ex) {
    if (db()->inTransaction()) db()->rollBack();
    // не блокируем скачивание из-за ошибки лога
}

// Отдаём файл
while (ob_get_level() > 0) { ob_end_clean(); }
header('Content-Type: application/java-archive');
header('Content-Disposition: attachment; filename="' . CLIENT_FILE_NAME . '"');
header('Content-Length: ' . filesize($file));
header('X-Content-Type-Options: nosniff');
readfile($file);
exit;
