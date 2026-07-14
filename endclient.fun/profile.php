<?php
require_once __DIR__ . '/includes/functions.php';

$username = trim($_GET['u'] ?? '');
$profile = null;

if ($username !== '' && preg_match('/^[A-Za-z0-9_]{1,32}$/', $username)) {
    $sql = 'SELECT u.id, u.username, u.display_name, u.bio, u.avatar_color, u.role, u.downloads_count, u.created_at,
                   (SELECT MAX(downloaded_at) FROM downloads d WHERE d.user_id = u.id) AS last_download
            FROM users u
            WHERE u.username = :username AND u.is_public = 1
            LIMIT 1';
    $st = db()->prepare($sql);
    $st->execute([':username' => $username]);
    $profile = $st->fetch() ?: null;
}

$sub = $profile ? active_subscription((int)$profile['id']) : null;

$page_title = $profile
    ? (($profile['display_name'] ?: $profile['username']) . ' — профиль End Client Forever')
    : 'Профиль не найден';
require __DIR__ . '/includes/header.php';
?>
<div class="profile-wrap">
<?php if (!$profile): ?>
  <div class="profile-card" style="padding:48px 30px;text-align:center">
    <h1 style="font-family:var(--font-display);font-size:26px;margin-bottom:10px">Профиль не найден</h1>
    <p style="color:var(--muted);margin-bottom:22px">Такого пользователя нет или его профиль скрыт.</p>
    <a href="index.php" class="btn btn-primary">На главную</a>
  </div>
<?php else: ?>
  <div class="profile-card">
    <div class="profile-cover"></div>
    <div class="profile-body">
      <div class="avatar" style="background:<?= e($profile['avatar_color']) ?>"><?= e(initials($profile['display_name'] ?: $profile['username'])) ?></div>
      <div class="profile-name">
        <?= e($profile['display_name'] ?: $profile['username']) ?>
        <?php if ($profile['role'] !== 'user'): ?><span class="role-badge <?= e($profile['role']) ?>"><?= e(role_label($profile['role'])) ?></span><?php endif; ?>
        <?php if ($sub): ?><span class="sub-badge">◆ <?= e($sub['plan']) ?></span><?php endif; ?>
      </div>
      <div class="profile-handle">@<?= e($profile['username']) ?></div>
      <?php if (!empty($profile['bio'])): ?><p class="profile-bio"><?= nl2br(e($profile['bio'])) ?></p><?php endif; ?>
      <div class="profile-stats">
        <div class="pstat"><div class="n"><?= (int)$profile['downloads_count'] ?></div><div class="l">Скачиваний</div></div>
        <div class="pstat"><div class="n"><?= e(human_date($profile['created_at'])) ?></div><div class="l">В сообществе с</div></div>
        <div class="pstat"><div class="n"><?= $profile['last_download'] ? e(human_date($profile['last_download'])) : '—' ?></div><div class="l">Последнее скач.</div></div>
      </div>
    </div>
  </div>
<?php endif; ?>
</div>
<?php require __DIR__ . '/includes/footer.php'; ?>
