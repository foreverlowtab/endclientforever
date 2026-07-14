<?php
require_once __DIR__ . '/includes/functions.php';
require_login();
$u = current_user();
$msg = null; $err = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'promo') {
    if (!check_csrf($_POST['csrf'] ?? '')) {
        $err = 'Сессия истекла. Обновите страницу.';
    } else {
        [$okp, $m] = redeem_promo($_POST['code'] ?? '', (int)$u['id']);
        if ($okp) { $msg = $m; } else { $err = $m; }
    }
}

$sub = active_subscription((int)$u['id']);
$page_title = 'Подписка — End Client Forever';
require __DIR__ . '/includes/header.php';
?>
<div class="dash">
  <div class="container" style="max-width:760px">
    <div class="dash-head"><h1>Подписка</h1><a href="dashboard.php" class="logout">← В кабинет</a></div>

    <?php if ($msg): ?><div class="alert alert-ok"><?= e($msg) ?></div><?php endif; ?>
    <?php if ($err): ?><div class="alert alert-err"><?= e($err) ?></div><?php endif; ?>

    <div class="panel">
      <h3>◆ Текущий статус</h3>
      <?php if ($sub): ?>
        <div class="stat-row"><span class="k">План</span><span class="v"><span class="sub-badge">◆ <?= e($sub['plan']) ?></span></span></div>
        <div class="stat-row"><span class="k">Источник</span><span class="v"><?= $sub['source'] === 'promo' ? 'Промокод' : ($sub['source'] === 'manual' ? 'Выдана вручную' : 'Покупка') ?></span></div>
        <div class="stat-row"><span class="k">Действует</span><span class="v"><?= $sub['expires_at'] ? ('до ' . e(human_datetime($sub['expires_at']))) : 'навсегда' ?></span></div>
      <?php else: ?>
        <p style="color:var(--muted)">Активной подписки нет.</p>
      <?php endif; ?>
    </div>

    <div class="pricing" style="margin:22px 0">
      <div class="price-card">
        <div class="pc-badge">Навсегда</div>
        <h3 class="pc-name">Forever Lite</h3>
        <div class="pc-price"><span class="cur">₽</span><?= e(SUB_PRICE_RUB) ?><span class="per">/ навсегда</span></div>
        <div class="pc-price-alt">≈ <?= e(SUB_PRICE_USD) ?> $ · разовый платёж</div>
        <ul class="plan-features">
          <li>Полный доступ к клиенту</li>
          <li>Обе темы: Red и Claude</li>
          <li>Кастомный HUD и ClickGUI</li>
          <li>Приоритетные авто-обновления</li>
          <li>Значок подписчика в профиле</li>
        </ul>
        <a href="<?= e(TELEGRAM_URL) ?>" target="_blank" rel="noopener" class="btn btn-primary btn-block">Оплатить через Telegram</a>
        <p class="pc-note">Оплата пока проводится вручную через Telegram. После оплаты владелец активирует Forever Lite на твоём аккаунте.</p>
      </div>
    </div>

    <div class="panel">
      <h3>🎁 Активировать промокод</h3>
      <form method="post" action="subscription.php">
        <input type="hidden" name="csrf" value="<?= e(csrf_token()) ?>">
        <input type="hidden" name="action" value="promo">
        <div class="promo-row">
          <input type="text" name="code" placeholder="Например: FIRSTLIGHT" autocomplete="off" required>
          <button type="submit" class="btn btn-primary">Активировать</button>
        </div>
      </form>
    </div>
  </div>
</div>
<?php require __DIR__ . '/includes/footer.php'; ?>
