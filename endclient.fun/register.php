<?php
require_once __DIR__ . '/includes/functions.php';
if (current_user()) redirect('dashboard.php');

$errors = [];
$old = ['username' => '', 'email' => ''];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!check_csrf($_POST['csrf'] ?? '')) {
        $errors[] = 'Сессия истекла. Обновите страницу и попробуйте снова.';
    }
    $username = trim($_POST['username'] ?? '');
    $email    = trim($_POST['email'] ?? '');
    $pass     = $_POST['password'] ?? '';
    $pass2    = $_POST['password2'] ?? '';
    $old['username'] = $username;
    $old['email'] = $email;

    if (!preg_match('/^[A-Za-z0-9_]{3,32}$/', $username)) {
        $errors[] = 'Логин: 3–32 символа, только латиница, цифры и _.';
    }
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $errors[] = 'Введите корректный email.';
    }
    if (mb_strlen($pass) < 6) {
        $errors[] = 'Пароль должен быть не короче 6 символов.';
    }
    if ($pass !== $pass2) {
        $errors[] = 'Пароли не совпадают.';
    }

    if (!$errors) {
        try {
            // Первый зарегистрировавшийся автоматически становится владельцем
            $count = (int) db()->query('SELECT COUNT(*) AS c FROM users')->fetch()['c'];
            $role = $count === 0 ? 'owner' : 'user';
            $colors = ['#e11d2a', '#CC785C', '#2783DE', '#46A171', '#8b5cf6', '#D97757'];
            $st = db()->prepare('INSERT INTO users (username, email, password_hash, display_name, avatar_color, role) VALUES (?, ?, ?, ?, ?, ?)');
            $st->execute([
                $username,
                $email,
                password_hash($pass, PASSWORD_DEFAULT),
                $username,
                $colors[array_rand($colors)],
                $role,
            ]);
            $newId = (int) db()->lastInsertId();
            session_regenerate_id(true);
            $_SESSION['user_id'] = $newId;
            redirect('dashboard.php');
        } catch (PDOException $ex) {
            if ($ex->getCode() === '23000') {
                $errors[] = 'Такой логин или email уже заняты.';
            } else {
                $errors[] = 'Ошибка сервера. Попробуйте позже.';
            }
        }
    }
}

$page_title = 'Регистрация — End Client Forever';
require __DIR__ . '/includes/header.php';
?>
<div class="auth-wrap">
  <div class="auth-bg a1"></div>
  <div class="auth-bg a2"></div>
  <form class="auth-card" method="post" action="register.php" autocomplete="on">
    <h1>Создать аккаунт</h1>
    <p class="sub">Регистрация бесплатна и открывает доступ к скачиванию клиента.</p>
    <?php if ($errors): ?>
      <div class="alert alert-err"><ul><?php foreach ($errors as $er) echo '<li>' . e($er) . '</li>'; ?></ul></div>
    <?php endif; ?>
    <input type="hidden" name="csrf" value="<?= e(csrf_token()) ?>">
    <div class="field"><label for="username">Имя пользователя</label><input type="text" id="username" name="username" value="<?= e($old['username']) ?>" placeholder="ForeverLowTab" required></div>
    <div class="field"><label for="email">Email</label><input type="email" id="email" name="email" value="<?= e($old['email']) ?>" placeholder="you@mail.com" required></div>
    <div class="field"><label for="password">Пароль</label><input type="password" id="password" name="password" placeholder="минимум 6 символов" required></div>
    <div class="field"><label for="password2">Повторите пароль</label><input type="password" id="password2" name="password2" required></div>
    <button type="submit" class="btn btn-primary btn-block">Зарегистрироваться</button>
    <p class="auth-alt">Уже есть аккаунт? <a href="login.php">Войти</a></p>
  </form>
</div>
<?php require __DIR__ . '/includes/footer.php'; ?>
