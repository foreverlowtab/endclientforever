<?php
require_once __DIR__ . '/includes/functions.php';
if (current_user()) redirect('dashboard.php');

$errors = [];
$login = '';
$next = $_GET['next'] ?? ($_POST['next'] ?? 'dashboard.php');
// защита от открытого редиректа: только локальные пути
if (!preg_match('/^[A-Za-z0-9_\-.\/?=&]+$/', (string)$next) || strpos((string)$next, '//') !== false) {
    $next = 'dashboard.php';
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!check_csrf($_POST['csrf'] ?? '')) {
        $errors[] = 'Сессия истекла. Обновите страницу.';
    }
    $login = trim($_POST['login'] ?? '');
    $pass  = $_POST['password'] ?? '';

    if ($login === '' || $pass === '') {
        $errors[] = 'Заполните все поля.';
    }
    if (!$errors) {
        $st = db()->prepare('SELECT * FROM users WHERE username = :l1 OR email = :l2 LIMIT 1');
        $st->execute([':l1' => $login, ':l2' => $login]);
        $user = $st->fetch();
        if ($user && password_verify($pass, $user['password_hash'])) {
            session_regenerate_id(true);
            $_SESSION['user_id'] = (int) $user['id'];
            db()->prepare('UPDATE users SET last_login = NOW() WHERE id = ?')->execute([$user['id']]);
            redirect($next);
        } else {
            $errors[] = 'Неверный логин или пароль.';
        }
    }
}

$page_title = 'Вход — End Client Forever';
require __DIR__ . '/includes/header.php';
?>
<div class="auth-wrap">
  <div class="auth-bg a1"></div>
  <div class="auth-bg a2"></div>
  <form class="auth-card" method="post" action="login.php" autocomplete="on">
    <h1>С возвращением</h1>
    <p class="sub">Войди, чтобы скачать клиент и управлять профилем.</p>
    <?php if ($errors): ?>
      <div class="alert alert-err"><ul><?php foreach ($errors as $er) echo '<li>' . e($er) . '</li>'; ?></ul></div>
    <?php endif; ?>
    <input type="hidden" name="csrf" value="<?= e(csrf_token()) ?>">
    <input type="hidden" name="next" value="<?= e($next) ?>">
    <div class="field"><label for="login">Имя пользователя или email</label><input type="text" id="login" name="login" value="<?= e($login) ?>" required></div>
    <div class="field"><label for="password">Пароль</label><input type="password" id="password" name="password" required></div>
    <button type="submit" class="btn btn-primary btn-block">Войти в кабинет</button>
    <p class="auth-alt">Нет аккаунта? <a href="register.php">Зарегистрироваться</a></p>
  </form>
</div>
<?php require __DIR__ . '/includes/footer.php'; ?>
