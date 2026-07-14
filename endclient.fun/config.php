<?php
/* ============================================================
   End Client Forever — конфигурация
   Домен: https://endclient.fun
   БД: endmi0987_endclientbd
   ============================================================ */

/* ------------------------------------------------------------
   РЕЖИМ ОТЛАДКИ
   Пока настраиваешь сайт — оставь true, чтобы видеть
   реальную причину ошибки вместо пустого "HTTP ERROR 500".
   НА БОЕВОМ САЙТЕ ОБЯЗАТЕЛЬНО поставь false.
   ------------------------------------------------------------ */
define('DEBUG', true);

error_reporting(E_ALL);
ini_set('display_errors', DEBUG ? '1' : '0');
ini_set('log_errors', '1');

// --- Понятная страница ошибки вместо пустого 500 ---
set_exception_handler(function ($e) {
    http_response_code(500);
    error_log('[ECF] Uncaught: ' . $e->getMessage());
    if (defined('DEBUG') && DEBUG) {
        echo '<pre style="margin:24px;padding:20px;border-radius:12px;background:#1b1d23;color:#ff6b6b;'
           . 'font:13px/1.6 ui-monospace,Menlo,Consolas,monospace;white-space:pre-wrap;overflow:auto">'
           . htmlspecialchars((string) $e, ENT_QUOTES, 'UTF-8') . '</pre>';
    } else {
        echo '<!doctype html><meta charset="utf-8"><title>Ошибка</title>'
           . '<div style="max-width:560px;margin:90px auto;text-align:center;'
           . 'font:16px/1.6 system-ui,Segoe UI,Roboto,sans-serif;color:#29261e">'
           . '<h1 style="font-size:26px;margin-bottom:10px">Что-то пошло не так</h1>'
           . '<p style="color:#6b675e">Сервис временно недоступен. Попробуйте позже.</p></div>';
    }
    exit;
});

// --- Подключение к MySQL (замени USER/PASS на данные хостинга) ---
define('DB_HOST', 'localhost');
define('DB_NAME', 'endmi0987_endclientbd');
define('DB_USER', 'endmi0987_foreverlowtab');   // <-- твой пользователь БД
 define('DB_PASS', 'CfH-Ham-4N2-KHt');            // <-- твой пароль БД (ОБЯЗАТЕЛЬНО замени!)

// --- Общие настройки ---
define('SITE_URL', 'https://endclient.fun');
define('SITE_NAME', 'End Client Forever');
define('CLIENT_VERSION', '0.6-test');
define('CLIENT_MC', '1.21.4');
define('CLIENT_FILE', __DIR__ . '/downloads/EndClientForever-1.21.4.jar');
define('CLIENT_FILE_NAME', 'EndClientForever-' . CLIENT_MC . '.jar');
define('TELEGRAM_URL', 'https://t.me/endclient');

// --- Подписка ---
define('SUB_PLAN', 'Forever Lite');
define('SUB_PRICE_RUB', '150');
define('SUB_PRICE_USD', '1.5');

/**
 * Единое подключение PDO (синглтон).
 * При ошибке подключения даёт понятное сообщение, а не пустой 500.
 */
function db(): PDO {
    static $pdo = null;
    if ($pdo === null) {
        if (!extension_loaded('pdo_mysql')) {
            throw new RuntimeException('Расширение PHP pdo_mysql не установлено на хостинге.');
        }
        try {
            $dsn = 'mysql:host=' . DB_HOST . ';dbname=' . DB_NAME . ';charset=utf8mb4';
            $pdo = new PDO($dsn, DB_USER, DB_PASS, [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
            ]);
        } catch (PDOException $e) {
            error_log('[ECF] DB connect failed: ' . $e->getMessage());
            throw new RuntimeException(
                'Не удалось подключиться к базе данных. '
                . 'Проверьте DB_USER / DB_PASS / DB_NAME в config.php и что база создана. '
                . '(исходная ошибка: ' . $e->getMessage() . ')',
                (int) $e->getCode(),
                $e
            );
        }
    }
    return $pdo;
}
