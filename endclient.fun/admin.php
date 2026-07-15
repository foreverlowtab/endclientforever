<?php
require_once __DIR__ . '/includes/functions.php';
$me = current_user();
if (!$me) redirect('login.php?next=admin.php');
$isOwner = is_owner($me);

$msg = null; $err = null;

if ($isOwner && $_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!check_csrf($_POST['csrf'] ?? '')) {
        $err = 'Сессия истекла. Обновите страницу.';
    } else {
        $action = $_POST['action'] ?? '';
        try {
            if ($action === 'set_role') {
                $uid = (int)($_POST['user_id'] ?? 0);
                $role = $_POST['role'] ?? 'user';
                if (!in_array($role, ['user', 'tester', 'owner'], true)) $role = 'user';
                db()->prepare('UPDATE users SET role = ? WHERE id = ?')->execute([$role, $uid]);
                $msg = 'Роль обновлена.';
            } elseif ($action === 'grant_sub') {
                $uid = (int)($_POST['user_id'] ?? 0);
                $days = (int)($_POST['days'] ?? 0);
                $expires = $days > 0 ? date('Y-m-d H:i:s', time() + $days * 86400) : null;
                db()->prepare('INSERT INTO subscriptions (user_id, plan, source, started_at, expires_at) VALUES (?, ?, ?, NOW(), ?)')
                    ->execute([$uid, SUB_PLAN, 'manual', $expires]);
                $msg = $days > 0 ? ('Подписка выдана на ' . $days . ' дн.') : 'Подписка выдана навсегда.';
            } elseif ($action === 'revoke_sub') {
                $uid = (int)($_POST['user_id'] ?? 0);
                db()->prepare('DELETE FROM subscriptions WHERE user_id = ?')->execute([$uid]);
                $msg = 'Подписки пользователя удалены.';
            } elseif ($action === 'delete_user') {
                $uid = (int)($_POST['user_id'] ?? 0);
                if ($uid === (int)$me['id']) {
                    $err = 'Нельзя удалить самого себя.';
                } else {
                    db()->prepare('DELETE FROM users WHERE id = ?')->execute([$uid]);
                    $msg = 'Пользователь удалён.';
                }
            } elseif ($action === 'create_promo') {
                $code = strtoupper(trim($_POST['code'] ?? ''));
                $days = (int)($_POST['duration_days'] ?? 0);
                $max  = (int)($_POST['max_uses'] ?? 0);
                $note = trim($_POST['note'] ?? '');
                if (!preg_match('/^[A-Z0-9_-]{3,64}$/', $code)) {
                    $err = 'Код промокода: 3–64 символа, латиница/цифры/дефис/подчёркивание.';
                } else {
                    try {
                        db()->prepare('INSERT INTO promo_codes (code, plan, duration_days, max_uses, is_active, note) VALUES (?, ?, ?, ?, 1, ?)')
                            ->execute([$code, SUB_PLAN, $days, $max, $note !== '' ? $note : null]);
                        $msg = 'Промокод создан: ' . $code;
                    } catch (PDOException $e) {
                        $err = $e->getCode() === '23000' ? 'Такой промокод уже существует.' : 'Ошибка создания промокода.';
                    }
                }
            } elseif ($action === 'toggle_promo') {
                $pid = (int)($_POST['promo_id'] ?? 0);
                db()->prepare('UPDATE promo_codes SET is_active = 1 - is_active WHERE id = ?')->execute([$pid]);
                $msg = 'Статус промокода изменён.';
            } elseif ($action === 'delete_promo') {
                $pid = (int)($_POST['promo_id'] ?? 0);
                db()->prepare('DELETE FROM promo_codes WHERE id = ?')->execute([$pid]);
                $msg = 'Промокод удалён.';
            }
        } catch (PDOException $ex) {
            $err = 'Ошибка операции. Проверьте данные и попробуйте снова.';
        }
    }
}

$page_title = 'Админ-панель — End Client Forever';
require __DIR__ . '/includes/header.php';

if (!$isOwner):
?>
<div class="deny">
  <h1>Доступ запрещён</h1>
  <p>Админ-панель доступна только владельцу (owner).</p>
  <a href="dashboard.php" class="btn btn-primary">В личный кабинет</a>
</div>
<?php
    require __DIR__ . '/includes/footer.php';
    exit;
endif;

$totUsers = (int) db()->query('SELECT COUNT(*) AS c FROM users')->fetch()['c'];
$totDowns = (int) db()->query('SELECT COUNT(*) AS c FROM downloads')->fetch()['c'];
$totSubs  = (int) db()->query('SELECT COUNT(DISTINCT user_id) AS c FROM subscriptions WHERE expires_at IS NULL OR expires_at > NOW()')->fetch()['c'];
$totPromo = (int) db()->query('SELECT COUNT(*) AS c FROM promo_codes')->fetch()['c'];
$totEvents = (int) db()->query('SELECT COUNT(*) AS c FROM client_events')->fetch()['c'];
$online = (int) db()->query('SELECT COUNT(*) AS c FROM client_tokens WHERE last_seen > (NOW() - INTERVAL 10 MINUTE)')->fetch()['c'];

$users = db()->query(
    'SELECT u.*,
       (SELECT COUNT(*) FROM subscriptions s WHERE s.user_id = u.id AND (s.expires_at IS NULL OR s.expires_at > NOW())) AS active_subs
     FROM users u ORDER BY u.created_at DESC LIMIT 200'
)->fetchAll();

$promos = db()->query('SELECT * FROM promo_codes ORDER BY created_at DESC')->fetchAll();

$recent = db()->query(
    'SELECT d.*, u.username FROM downloads d LEFT JOIN users u ON u.id = d.user_id
     ORDER BY d.downloaded_at DESC LIMIT 25'
)->fetchAll();

$events = db()->query(
    'SELECT ev.*, u.username FROM client_events ev LEFT JOIN users u ON u.id = ev.user_id
     ORDER BY ev.created_at DESC LIMIT 200'
)->fetchAll();

$sessions = db()->query(
    'SELECT t.*, u.username FROM client_tokens t JOIN users u ON u.id = t.user_id
     WHERE t.expires_at IS NULL OR t.expires_at > NOW()
     ORDER BY t.last_seen DESC LIMIT 40'
)->fetchAll();

$csrf = csrf_token();
?>
<div class="admin">
  <style>
  .section-head-row{display:flex;align-items:center;justify-content:space-between;gap:14px;flex-wrap:wrap;margin-bottom:16px}
  .section-head-row h2{margin:0}
  .section-tools{display:flex;align-items:center;gap:10px;flex-wrap:wrap}
  .tbl-search{background:var(--surface-2);border:1px solid var(--border);border-radius:10px;padding:8px 13px 8px 34px;font:inherit;font-size:14px;color:var(--text);min-width:230px;outline:none;transition:border-color .2s,box-shadow .2s;background-image:url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" stroke="%23888" stroke-width="2"><circle cx="7" cy="7" r="5"/><path d="M11 11l4 4"/></svg>');background-repeat:no-repeat;background-position:11px 50%}
  .tbl-search::placeholder{color:var(--muted)}
  .tbl-search:focus{border-color:var(--accent);box-shadow:0 0 0 3px var(--accent-soft)}
  .tbl-count{font-size:13px;color:var(--muted);white-space:nowrap;font-variant-numeric:tabular-nums}
  .tbl-more-wrap{margin-top:12px}
  </style>
  <div class="container">
    <div class="admin-head">
      <h1>🛡 Админ-панель</h1>
      <a href="dashboard.php" class="logout">← В кабинет</a>
    </div>
    <?php if ($msg): ?><div class="alert alert-ok"><?= e($msg) ?></div><?php endif; ?>
    <?php if ($err): ?><div class="alert alert-err"><?= e($err) ?></div><?php endif; ?>

    <div class="admin-stats">
      <div class="stat-card"><div class="n"><?= $totUsers ?></div><div class="l">Пользователей</div></div>
      <div class="stat-card"><div class="n"><?= $totSubs ?></div><div class="l">Активных подписок</div></div>
      <div class="stat-card"><div class="n"><?= $totDowns ?></div><div class="l">Скачиваний</div></div>
      <div class="stat-card"><div class="n"><?= $totPromo ?></div><div class="l">Промокодов</div></div>
      <div class="stat-card"><div class="n"><?= $online ?></div><div class="l">Онлайн в клиенте</div></div>
      <div class="stat-card"><div class="n"><?= $totEvents ?></div><div class="l">Действий в клиенте</div></div>
    </div>

    <div class="admin-section">
      <div class="section-head-row">
        <h2>👥 Пользователи</h2>
        <div class="section-tools">
          <input type="search" class="tbl-search" id="users-search" placeholder="Поиск: имя или email…">
          <span class="tbl-count" id="users-count"></span>
        </div>
      </div>
      <div class="table-wrap">
      <table class="admin-table" id="users-table">
        <thead><tr><th>ID</th><th>Пользователь</th><th>Email</th><th>Роль</th><th>Подписка</th><th>Действия</th></tr></thead>
        <tbody>
        <?php foreach ($users as $usr): ?>
          <tr class="data-row">
            <td><?= (int)$usr['id'] ?></td>
            <td><a href="profile.php?u=<?= e(urlencode($usr['username'])) ?>" style="color:var(--accent);font-weight:600"><?= e($usr['username']) ?></a></td>
            <td><?= e($usr['email']) ?></td>
            <td>
              <form method="post" class="inline">
                <input type="hidden" name="csrf" value="<?= e($csrf) ?>">
                <input type="hidden" name="action" value="set_role">
                <input type="hidden" name="user_id" value="<?= (int)$usr['id'] ?>">
                <select name="role" onchange="this.form.submit()">
                  <?php foreach (['user', 'tester', 'owner'] as $r): ?>
                    <option value="<?= $r ?>" <?= $usr['role'] === $r ? 'selected' : '' ?>><?= e(role_label($r)) ?></option>
                  <?php endforeach; ?>
                </select>
              </form>
            </td>
            <td><?= $usr['active_subs'] > 0 ? '<span class="chip on">Активна</span>' : '<span class="chip off">Нет</span>' ?></td>
            <td>
              <div class="inline">
                <form method="post" class="inline">
                  <input type="hidden" name="csrf" value="<?= e($csrf) ?>">
                  <input type="hidden" name="action" value="grant_sub">
                  <input type="hidden" name="user_id" value="<?= (int)$usr['id'] ?>">
                  <input type="number" name="days" value="0" min="0" title="0 = навсегда" style="width:70px">
                  <button class="btn btn-ghost btn-sm">Выдать</button>
                </form>
                <form method="post" class="inline" onsubmit="return confirm('Снять подписки пользователя?')">
                  <input type="hidden" name="csrf" value="<?= e($csrf) ?>">
                  <input type="hidden" name="action" value="revoke_sub">
                  <input type="hidden" name="user_id" value="<?= (int)$usr['id'] ?>">
                  <button class="btn btn-ghost btn-sm">Снять</button>
                </form>
                <?php if ((int)$usr['id'] !== (int)$me['id']): ?>
                <form method="post" class="inline" onsubmit="return confirm('Удалить пользователя без возврата?')">
                  <input type="hidden" name="csrf" value="<?= e($csrf) ?>">
                  <input type="hidden" name="action" value="delete_user">
                  <input type="hidden" name="user_id" value="<?= (int)$usr['id'] ?>">
                  <button class="btn btn-danger btn-sm">Удалить</button>
                </form>
                <?php endif; ?>
              </div>
            </td>
          </tr>
        <?php endforeach; ?>
        <tr class="no-match" style="display:none"><td colspan="6" style="color:var(--muted)">Ничего не найдено.</td></tr>
        </tbody>
      </table>
      </div>
      <div class="tbl-more-wrap"><button type="button" class="btn btn-ghost btn-block" id="users-toggle" style="display:none"></button></div>
    </div>

    <div class="admin-grid">
      <div class="admin-section">
        <h2>🎁 Создать промокод</h2>
        <form method="post">
          <input type="hidden" name="csrf" value="<?= e($csrf) ?>">
          <input type="hidden" name="action" value="create_promo">
          <div class="field"><label>Код</label><input type="text" name="code" placeholder="SUMMER2026" required style="text-transform:uppercase"></div>
          <div class="field"><label>Дней доступа (0 = навсегда)</label><input type="number" name="duration_days" value="0" min="0"></div>
          <div class="field"><label>Лимит активаций (0 = без лимита)</label><input type="number" name="max_uses" value="0" min="0"></div>
          <div class="field"><label>Заметка (необязательно)</label><input type="text" name="note" placeholder="Для чего этот код"></div>
          <button class="btn btn-primary btn-block">Создать промокод</button>
        </form>
      </div>

      <div class="admin-section">
        <h2>🏷 Промокоды</h2>
        <div class="table-wrap">
        <table class="admin-table">
          <thead><tr><th>Код</th><th>Дней</th><th>Исп.</th><th>Статус</th><th></th></tr></thead>
          <tbody>
          <?php foreach ($promos as $p): ?>
            <tr>
              <td><b><?= e($p['code']) ?></b></td>
              <td><?= (int)$p['duration_days'] === 0 ? '∞' : (int)$p['duration_days'] ?></td>
              <td><?= (int)$p['used_count'] ?><?= (int)$p['max_uses'] > 0 ? ('/' . (int)$p['max_uses']) : '' ?></td>
              <td><?= (int)$p['is_active'] === 1 ? '<span class="chip on">вкл</span>' : '<span class="chip off">выкл</span>' ?></td>
              <td>
                <div class="inline">
                  <form method="post" class="inline">
                    <input type="hidden" name="csrf" value="<?= e($csrf) ?>">
                    <input type="hidden" name="action" value="toggle_promo">
                    <input type="hidden" name="promo_id" value="<?= (int)$p['id'] ?>">
                    <button class="btn btn-ghost btn-sm"><?= (int)$p['is_active'] === 1 ? 'Выкл' : 'Вкл' ?></button>
                  </form>
                  <form method="post" class="inline" onsubmit="return confirm('Удалить промокод?')">
                    <input type="hidden" name="csrf" value="<?= e($csrf) ?>">
                    <input type="hidden" name="action" value="delete_promo">
                    <input type="hidden" name="promo_id" value="<?= (int)$p['id'] ?>">
                    <button class="btn btn-danger btn-sm">✕</button>
                  </form>
                </div>
              </td>
            </tr>
          <?php endforeach; ?>
          <?php if (!$promos): ?><tr><td colspan="5" style="color:var(--muted)">Пока нет промокодов.</td></tr><?php endif; ?>
          </tbody>
        </table>
        </div>
      </div>
    </div>

    <div class="admin-section">
      <h2>🟢 Активные сессии клиента</h2>
      <div class="table-wrap">
      <table class="admin-table">
        <thead><tr><th>Пользователь</th><th>Активность</th><th>IP</th><th>Вход</th></tr></thead>
        <tbody>
        <?php foreach ($sessions as $s):
            $seen = $s['last_seen'] ? strtotime($s['last_seen']) : 0;
            $isOnline = $seen && $seen > (time() - 600);
        ?>
          <tr>
            <td><a href="profile.php?u=<?= e(urlencode($s['username'])) ?>" style="color:var(--accent);font-weight:600"><?= e($s['username']) ?></a></td>
            <td><?= $isOnline ? '<span class="chip on">онлайн</span>' : e(human_datetime($s['last_seen'])) ?></td>
            <td><?= e($s['ip'] ?? '—') ?></td>
            <td><?= e(human_datetime($s['created_at'])) ?></td>
          </tr>
        <?php endforeach; ?>
        <?php if (!$sessions): ?><tr><td colspan="4" style="color:var(--muted)">Пока никто не входил через клиент.</td></tr><?php endif; ?>
        </tbody>
      </table>
      </div>
    </div>

    <div class="admin-section">
      <div class="section-head-row">
        <h2>🎮 Действия пользователей в клиенте</h2>
        <div class="section-tools">
          <input type="search" class="tbl-search" id="events-search" placeholder="Поиск: пользователь, действие, IP…">
          <span class="tbl-count" id="events-count"></span>
        </div>
      </div>
      <div class="table-wrap">
      <table class="admin-table" id="events-table">
        <thead><tr><th>Пользователь</th><th>Действие</th><th>Детали</th><th>IP</th><th>Когда</th></tr></thead>
        <tbody>
        <?php foreach ($events as $ev): ?>
          <tr class="data-row">
            <td><?= e($ev['username'] ?? '—') ?></td>
            <td><span class="chip on"><?= e(client_event_label($ev['event_type'])) ?></span></td>
            <td style="color:var(--muted)"><?= e($ev['detail'] ?? '—') ?></td>
            <td><?= e($ev['ip'] ?? '—') ?></td>
            <td><?= e(human_datetime($ev['created_at'])) ?></td>
          </tr>
        <?php endforeach; ?>
        <?php if (!$events): ?><tr><td colspan="5" style="color:var(--muted)">Пока нет действий из клиента.</td></tr><?php endif; ?>
        <tr class="no-match" style="display:none"><td colspan="5" style="color:var(--muted)">Ничего не найдено.</td></tr>
        </tbody>
      </table>
      </div>
      <div class="tbl-more-wrap"><button type="button" class="btn btn-ghost btn-block" id="events-toggle" style="display:none"></button></div>
    </div>

    <div class="admin-section">
      <h2>⬇ Последние скачивания</h2>
      <div class="table-wrap">
      <table class="admin-table">
        <thead><tr><th>Пользователь</th><th>Версия</th><th>IP</th><th>Когда</th></tr></thead>
        <tbody>
        <?php foreach ($recent as $d): ?>
          <tr>
            <td><?= e($d['username'] ?? '—') ?></td>
            <td><?= e($d['client_version']) ?></td>
            <td><?= e($d['ip'] ?? '—') ?></td>
            <td><?= e(human_datetime($d['downloaded_at'])) ?></td>
          </tr>
        <?php endforeach; ?>
        <?php if (!$recent): ?><tr><td colspan="4" style="color:var(--muted)">Пока нет скачиваний.</td></tr><?php endif; ?>
        </tbody>
      </table>
      </div>
    </div>
  </div>
</div>
<script>
(function(){
  function initTable(o){
    var table=document.getElementById(o.table);
    if(!table)return;
    var search=document.getElementById(o.search),toggle=document.getElementById(o.toggle),count=document.getElementById(o.count);
    var rows=Array.prototype.slice.call(table.querySelectorAll('tbody tr.data-row'));
    var noMatch=table.querySelector('tbody tr.no-match');
    var LIMIT=o.limit||8,expanded=false;
    function render(){
      var q=((search&&search.value)||'').trim().toLowerCase();
      var shown=0,matched=0;
      rows.forEach(function(tr){
        var hit=q===''||tr.textContent.toLowerCase().indexOf(q)>-1;
        if(!hit){tr.style.display='none';return;}
        matched++;
        if(q!==''||expanded||shown<LIMIT){tr.style.display='';shown++;}else{tr.style.display='none';}
      });
      if(noMatch)noMatch.style.display=(matched===0&&rows.length>0)?'':'none';
      if(count)count.textContent=q!==''?(matched+' / '+rows.length):(''+rows.length);
      if(toggle){
        if(q!==''||rows.length<=LIMIT){toggle.style.display='none';}
        else{toggle.style.display='';toggle.textContent=expanded?'Свернуть ▴':('Показать все ('+rows.length+') ▾');}
      }
    }
    if(search)search.addEventListener('input',render);
    if(toggle)toggle.addEventListener('click',function(){expanded=!expanded;render();});
    render();
  }
  initTable({table:'events-table',search:'events-search',toggle:'events-toggle',count:'events-count',limit:8});
  initTable({table:'users-table',search:'users-search',toggle:'users-toggle',count:'users-count',limit:12});
})();
</script>
<?php require __DIR__ . '/includes/footer.php'; ?>
