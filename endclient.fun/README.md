# End Client Forever — сайт + личный кабинет (PHP + MySQL)

Домен: https://endclient.fun
БД: endmi0987_endclientbd

## Установка
1. Залей все файлы в корень сайта (public_html).
2. Создай БД endmi0987_endclientbd и импортируй schema.sql (phpMyAdmin → Импорт).
3. В config.php впиши DB_USER и DB_PASS своего хостинга.
4. Положи реальный .jar в downloads/EndClientForever-1.21.4.jar
5. Открой https://endclient.fun — готово.

## Страницы
- index.php — лендинг
- register.php / login.php / logout.php — авторизация
- dashboard.php — личный кабинет (скачивание + настройки профиля)
- download.php — выдача .jar ТОЛЬКО авторизованным
- profile.php?u=USERNAME — публичный профиль
