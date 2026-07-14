<?php
require_once __DIR__ . '/functions.php';
$__u = current_user();
$__title = $page_title ?? SITE_NAME;
?><!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><?= e($__title) ?></title>
<meta name="description" content="End Client Forever — уникальный визуальный клиент для Minecraft Fabric 1.21.4.">
<meta name="robots" content="noindex">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="assets/styles.css">
<script>(function(){var t=localStorage.getItem('ecf-theme');if(t==='claude')document.documentElement.setAttribute('data-theme','claude');})();</script>
</head>
<body>
<nav class="nav">
  <div class="container nav-inner">
    <a class="brand" href="index.php">
      <svg class="mark" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg"><rect width="32" height="32" rx="8" fill="currentColor" style="color:var(--accent)"/><path d="M16 6 L25 16 L16 26 L7 16 Z" stroke="#fff" stroke-width="2" fill="none"/><path d="M16 11 L21 16 L16 21 L11 16 Z" fill="#fff"/></svg>
      End Client<span style="opacity:.5">&nbsp;Forever</span>
    </a>
    <div class="nav-links">
      <a href="index.php#features">Возможности</a>
      <a href="index.php#pricing">Подписка</a>
      <a href="index.php#themes">Темы</a>
      <a href="<?= e(TELEGRAM_URL) ?>" target="_blank" rel="noopener">Telegram</a>
    </div>
    <div class="nav-actions">
      <div class="theme-toggle">
        <button type="button" data-theme="red" class="active">Red</button>
        <button type="button" data-theme="claude">Claude</button>
      </div>
      <?php if ($__u): ?>
        <?php if (is_owner($__u)): ?><a href="admin.php" class="btn btn-ghost">Админка</a><?php endif; ?>
        <a href="dashboard.php" class="btn btn-primary">Кабинет</a>
      <?php else: ?>
        <a href="login.php" class="btn btn-ghost">Войти</a>
        <a href="register.php" class="btn btn-primary">Регистрация</a>
      <?php endif; ?>
    </div>
  </div>
</nav>
