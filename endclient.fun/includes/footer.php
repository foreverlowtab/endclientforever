<footer>
  <div class="container">
    <div class="foot-grid">
      <div class="foot-brand">
        <a class="brand" href="index.php">
          <svg class="mark" viewBox="0 0 32 32" fill="none"><rect width="32" height="32" rx="8" fill="currentColor" style="color:var(--accent)"/><path d="M16 6 L25 16 L16 26 L7 16 Z" stroke="#fff" stroke-width="2" fill="none"/><path d="M16 11 L21 16 L16 21 L11 16 Z" fill="#fff"/></svg>
          End Client Forever
        </a>
        <p>Уникальный визуальный клиент для Minecraft Fabric 1.21.4. Автор: ForeverLowTab.</p>
      </div>
      <div class="foot-cols">
        <div class="foot-col">
          <h4>Продукт</h4>
          <a href="index.php#features">Возможности</a>
          <a href="index.php#pricing">Подписка</a>
          <a href="index.php#themes">Темы</a>
          <a href="dashboard.php">Личный кабинет</a>
        </div>
        <div class="foot-col">
          <h4>Сообщество</h4>
          <a href="<?= e(TELEGRAM_URL) ?>" target="_blank" rel="noopener">Telegram</a>
          <a href="<?= e(SITE_URL) ?>">endclient.fun</a>
        </div>
      </div>
    </div>
    <div class="foot-bottom">
      <span>&copy; <?= date('Y') ?> End Client Forever · ForeverLowTab</span>
      <span>Закрытый проект · все права защищены</span>
    </div>
  </div>
</footer>
<script src="assets/app.js"></script>
</body>
</html>
