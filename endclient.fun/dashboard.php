<?php
require_once __DIR__ . '/includes/functions.php';
require_login();
$u = current_user();
$ok = false;
$errors = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!check_csrf($_POST['csrf'] ?? '')) {
        $errors[] = 'Сессия истекла. Обновите страницу.';
    }
    $display = trim($_POST['display_name'] ?? '');
    $bio     = trim($_POST['bio'] ?? '');
    $color   = trim($_POST['avatar_color'] ?? '#e11d2a');
    $public  = isset($_POST['is_public']) ? 1 : 0;

    if (mb_strlen($display) > 64) $errors[] = 'Имя профиля слишком длинное.';
    if (mb_strlen($bio) > 280) $errors[] = 'Описание до 280 символов.';
    if (!preg_match('/^#[0-9A-Fa-f]{6}$/', $color)) $color = '#e11d2a';

    if (!$errors) {
        $st = db()->prepare('UPDATE users SET display_name = ?, bio = ?, avatar_color = ?, is_public = ? WHERE id = ?');
        $st->execute([$display !== '' ? $display : $u['username'], $bio !== '' ? $bio : null, $color, $public, $u['id']]);
        $ok = true;
        $st = db()->prepare('SELECT * FROM users WHERE id = ?');
        $st->execute([$u['id']]);
        $u = $st->fetch();
    }
}

$st = db()->prepare('SELECT MAX(downloaded_at) AS last_dl FROM downloads WHERE user_id = ?');
$st->execute([$u['id']]);
$lastDl = $st->fetch()['last_dl'] ?? null;

$sub = active_subscription((int)$u['id']);

$page_title = 'Личный кабинет — End Client Forever';
require __DIR__ . '/includes/header.php';
?>
<div class="dash">
  <div class="container">
    <div class="dash-head">
      <h1>Привет, <span class="u"><?= e($u['display_name'] ?: $u['username']) ?></span> 👋</h1>
      <a href="logout.php" class="logout">Выйти</a>
    </div>

    <?php if ($ok): ?><div class="alert alert-ok">Профиль сохранён.</div><?php endif; ?>
    <?php if ($errors): ?><div class="alert alert-err"><ul><?php foreach ($errors as $er) echo '<li>' . e($er) . '</li>'; ?></ul></div><?php endif; ?>

    <div class="dash-grid">
      <div>
        <div class="panel">
          <h3>◆ Моя лицензия</h3>
          <div class="stat-row"><span class="k">Пользователь</span><span class="v"><?= e($u['username']) ?></span></div>
          <div class="stat-row"><span class="k">Email</span><span class="v"><?= e($u['email']) ?></span></div>
          <div class="stat-row"><span class="k">Роль</span><span class="v"><span class="role-badge <?= e($u['role']) ?>"><?= e(role_label($u['role'])) ?></span></span></div>
          <div class="stat-row"><span class="k">Версия клиента</span><span class="v"><?= e(CLIENT_VERSION) ?> · MC <?= e(CLIENT_MC) ?></span></div>
          <div class="stat-row"><span class="k">Всего скачиваний</span><span class="v"><?= (int)$u['downloads_count'] ?></span></div>
          <div class="stat-row"><span class="k">Последнее скачивание</span><span class="v"><?= $lastDl ? e(human_date($lastDl)) : '—' ?></span></div>
          <div class="stat-row"><span class="k">Регистрация</span><span class="v"><?= e(human_date($u['created_at'])) ?></span></div>
        </div>

        <div class="panel">
          <h3>⚙ Настройки профиля</h3>
          <form method="post" action="dashboard.php">
            <input type="hidden" name="csrf" value="<?= e(csrf_token()) ?>">
            <div class="field"><label for="display_name">Отображаемое имя</label><input type="text" id="display_name" name="display_name" maxlength="64" value="<?= e($u['display_name']) ?>"></div>
            <div class="field"><label for="bio">О себе</label><textarea id="bio" name="bio" maxlength="280" placeholder="Несколько слов о себе..."><?= e($u['bio']) ?></textarea></div>
            <div class="field"><label for="avatar_color">Цвет аватара</label><input type="color" id="avatar_color" name="avatar_color" value="<?= e($u['avatar_color']) ?>" style="height:46px;padding:6px"></div>
            <div class="field"><label style="display:flex;align-items:center;gap:10px;font-weight:500"><input type="checkbox" name="is_public" value="1" <?= $u['is_public'] ? 'checked' : '' ?> style="width:auto"> Показывать мой публичный профиль</label></div>
            <button type="submit" class="btn btn-primary btn-block">Сохранить</button>
          </form>
        </div>
      </div>

      <div>
        <div class="panel">
          <div class="big-download">
            <small>Готово к загрузке</small>
            <div class="v">End Client Forever</div>
            <small>v<?= e(CLIENT_VERSION) ?> · Fabric <?= e(CLIENT_MC) ?></small>
            <a href="download.php" class="btn btn-block">Скачать .jar ↓</a>
          </div>
        </div>
        <div class="panel">
          <h3>★ Подписка</h3>
          <?php if ($sub): ?>
            <div class="stat-row"><span class="k">План</span><span class="v"><span class="sub-badge">◆ <?= e($sub['plan']) ?></span></span></div>
            <div class="stat-row"><span class="k">Действует</span><span class="v"><?= $sub['expires_at'] ? ('до ' . e(human_datetime($sub['expires_at']))) : 'навсегда' ?></span></div>
            <a href="subscription.php" class="btn btn-ghost btn-block" style="margin-top:14px">Управление подпиской</a>
          <?php else: ?>
            <p style="color:var(--muted);font-size:14.5px;margin-bottom:14px">Активной подписки пока нет. Активируй промокод или оформи Forever Lite.</p>
            <a href="subscription.php" class="btn btn-primary btn-block">Forever Lite / промокод</a>
          <?php endif; ?>
        </div>
        <?php if (is_owner($u)): ?>
        <div class="panel">
          <h3>🛡 Владельцу</h3>
          <p style="color:var(--muted);font-size:14.5px;margin-bottom:14px">У тебя есть доступ к админ-панели.</p>
          <a href="admin.php" class="btn btn-primary btn-block">Открыть админ-панель</a>
        </div>
        <?php endif; ?>
        <div class="panel">
          <h3>🔗 Мой публичный профиль</h3>
          <div class="stat-row"><span class="k">Видимость</span><span class="v"><?= $u['is_public'] ? 'Открыт' : 'Скрыт' ?></span></div>
          <a href="profile.php?u=<?= e(urlencode($u['username'])) ?>" class="btn btn-ghost btn-block" style="margin-top:14px">Открыть профиль</a>
        </div>
      </div>
    </div>
  </div>
</div>
<?php require __DIR__ . '/includes/footer.php'; ?>
