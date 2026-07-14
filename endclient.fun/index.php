<?php
$page_title = 'End Client Forever — визуальный клиент для Minecraft 1.21.4';
require __DIR__ . '/includes/header.php';
$u = current_user();
?>
<header class="hero">
  <div class="container hero-grid">
    <div class="hero-copy reveal in">
      <span class="badge"><span class="dot"></span>Fabric · Minecraft 1.21.4 · v<?= e(CLIENT_VERSION) ?></span>
      <h1 class="hero-title">Играй красиво.<br>Играй <span class="accent">Forever</span>.</h1>
      <p class="hero-sub">Уникальный визуальный клиент с плавными анимациями и двумя фирменными темами. Собран для Fabric 1.21.4 — стабильно, чисто, эстетично.</p>
      <div class="hero-cta">
        <a href="<?= $u ? 'download.php' : 'register.php' ?>" class="btn btn-primary"><?= $u ? 'Скачать клиент ↓' : 'Зарегистрироваться' ?></a>
        <a href="<?= e(TELEGRAM_URL) ?>" target="_blank" rel="noopener" class="btn btn-ghost">Telegram-канал</a>
      </div>
      <div class="hero-meta">
        <span><b>1.21.4</b> · Fabric</span>
        <span><b>2</b> темы оформления</span>
        <span><b>240 FPS</b> анимации</span>
      </div>
    </div>
    <div class="hero-visual reveal in">
      <div class="hv-stage">
        <div class="hv-orb o1"></div>
        <div class="hv-orb o2"></div>
        <div class="clickgui">
          <div class="cg-head"><span class="d"></span><span class="d"></span><span class="d"></span><span class="cg-title">End Client — ClickGUI</span></div>
          <div class="cg-body">
            <div class="cg-cats"><span class="cg-cat active">Combat</span><span class="cg-cat">Render</span><span class="cg-cat">Player</span></div>
            <div class="cg-mod on"><span>KillAura</span><i class="sw"></i></div>
            <div class="cg-mod on"><span>Animations</span><i class="sw"></i></div>
            <div class="cg-mod"><span>ESP</span><i class="sw"></i></div>
            <div class="cg-mod"><span>Custom HUD</span><i class="sw"></i></div>
          </div>
        </div>
        <div class="hud-chip c1"><b>FPS</b> 240</div>
        <div class="hud-chip c2"><b>XYZ</b> 128 64 -512</div>
      </div>
    </div>
  </div>
</header>

<section class="section" id="features">
  <div class="container">
    <div class="section-head reveal">
      <div class="eyebrow">Возможности</div>
      <h2 class="section-title">Всё, что нужно для красивой игры</h2>
      <p class="section-sub">Продуманный интерфейс, тонкие анимации и модульная система — без визуального мусора.</p>
    </div>
    <div class="features">
      <div class="card reveal"><div class="ico">✦</div><h3>Плавные анимации</h3><p>Каждый элемент интерфейса анимирован с частотой до 240 FPS и мягкими easing-кривыми.</p></div>
      <div class="card reveal"><div class="ico">◆</div><h3>Кастомный HUD</h3><p>Настраиваемые модули: координаты, FPS, keystrokes, потенции и таргет-худ.</p></div>
      <div class="card reveal"><div class="ico">◑</div><h3>Две темы</h3><p>Переключайся между бело-красной темой и стилем Claude в один клик.</p></div>
      <div class="card reveal"><div class="ico">⚡</div><h3>Оптимизация</h3><p>Совместимость с Sodium и минимальная нагрузка на рендер — стабильный FPS.</p></div>
      <div class="card reveal"><div class="ico">⚙</div><h3>ClickGUI</h3><p>Удобное меню с поиском, категориями и живым предпросмотром настроек.</p></div>
      <div class="card reveal"><div class="ico">↻</div><h3>Авто-обновления</h3><p>Клиент сам проверяет новые версии и подтягивает свежие сборки.</p></div>
    </div>
  </div>
</section>

<section class="section" id="pricing">
  <div class="container">
    <div class="section-head reveal">
      <div class="eyebrow">Подписка</div>
      <h2 class="section-title">Forever Lite — один раз и навсегда</h2>
      <p class="section-sub">Разовая оплата открывает премиум-возможности клиента навсегда. Никаких ежемесячных платежей.</p>
    </div>
    <div class="pricing reveal">
      <div class="price-card">
        <div class="pc-badge">Навсегда</div>
        <h3 class="pc-name">Forever Lite</h3>
        <div class="pc-price"><span class="cur">₽</span><?= e(SUB_PRICE_RUB) ?><span class="per">/ навсегда</span></div>
        <div class="pc-price-alt">≈ <?= e(SUB_PRICE_USD) ?> $ · разовый платёж</div>
        <ul class="plan-features">
          <li>Полный доступ к клиенту End Client Forever</li>
          <li>Обе темы: Red и Claude</li>
          <li>Кастомный HUD и ClickGUI</li>
          <li>Приоритетные авто-обновления</li>
          <li>Значок подписчика в профиле</li>
        </ul>
        <a href="<?= $u ? 'subscription.php' : 'register.php' ?>" class="btn btn-primary btn-block"><?= $u ? 'Оформить Forever Lite' : 'Начать' ?></a>
        <p class="pc-note">Есть промокод? Активируй его в <a href="subscription.php">разделе подписки</a>.</p>
      </div>
    </div>
  </div>
</section>

<section class="section" id="themes">
  <div class="container">
    <div class="section-head reveal">
      <div class="eyebrow">Оформление</div>
      <h2 class="section-title">Две темы. Один клиент.</h2>
      <p class="section-sub">Нажми переключатель Red / Claude в шапке — весь сайт меняет стиль вживую.</p>
    </div>
    <div class="themes">
      <div class="theme-card reveal">
        <div class="theme-preview tp-red">
          <div class="mock-window"><div class="mock-bar"><i></i><i></i><i></i></div><div class="mock-body"><div class="mock-line" style="width:70%"></div><div class="mock-line" style="width:90%"></div><div class="mock-line" style="width:50%"></div><span class="mock-pill" style="background:#e11d2a">RED THEME</span></div></div>
        </div>
        <div class="theme-info"><h3>Бело-красная</h3><p>Чистый белый фон, сочный красный акцент и энергичные анимации.</p><div class="swatches"><span class="swatch" style="background:#fff"></span><span class="swatch" style="background:#f6f7f9"></span><span class="swatch" style="background:#e11d2a"></span><span class="swatch" style="background:#ff414d"></span><span class="swatch" style="background:#14161c"></span></div></div>
      </div>
      <div class="theme-card reveal">
        <div class="theme-preview tp-claude">
          <div class="mock-window" style="background:#FAF9F5"><div class="mock-bar" style="background:#F0EEE6"><i></i><i></i><i></i></div><div class="mock-body"><div class="mock-line" style="width:70%;background:#e6e1d4"></div><div class="mock-line" style="width:90%;background:#e6e1d4"></div><div class="mock-line" style="width:50%;background:#e6e1d4"></div><span class="mock-pill" style="background:#CC785C">CLAUDE THEME</span></div></div>
        </div>
        <div class="theme-info"><h3>Claude / Anthropic</h3><p>Тёплый молочный фон, глиняно-оранжевый акцент и серифные заголовки.</p><div class="swatches"><span class="swatch" style="background:#F0EEE6"></span><span class="swatch" style="background:#FAF9F5"></span><span class="swatch" style="background:#CC785C"></span><span class="swatch" style="background:#D97757"></span><span class="swatch" style="background:#29261E"></span></div></div>
      </div>
    </div>
  </div>
</section>

<section class="section" id="download">
  <div class="container">
    <div class="cta reveal">
      <h2>Скачай End Client Forever</h2>
      <p><?= $u ? 'Ты вошёл — можно скачивать клиент прямо сейчас.' : 'Скачивание доступно только авторизованным пользователям. Зарегистрируйся — это бесплатно.' ?></p>
      <div class="hero-cta">
        <?php if ($u): ?>
          <a href="download.php" class="btn btn-primary">Скачать .jar ↓</a>
          <a href="dashboard.php" class="btn btn-ghost">Личный кабинет</a>
        <?php else: ?>
          <a href="register.php" class="btn btn-primary">Зарегистрироваться</a>
          <a href="login.php" class="btn btn-ghost">У меня есть аккаунт</a>
        <?php endif; ?>
      </div>
    </div>
  </div>
</section>

<?php require __DIR__ . '/includes/footer.php'; ?>
