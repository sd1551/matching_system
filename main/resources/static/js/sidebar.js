document.addEventListener('DOMContentLoaded', function() {
    // Инициализация меню
    const menuBtn = document.getElementById('menuBtn');
    const sidebar = document.getElementById('sidebar');

    if (menuBtn && sidebar) {
        menuBtn.addEventListener('click', function() {
            sidebar.classList.toggle('active');
            menuBtn.classList.toggle('active');
        });
    }

    // Подсветка активного пункта меню
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.sidebar-nav a');

    navLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });

    // Показ/скрытие админских пунктов
    fetch('/auth/check', {
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) throw new Error('Не авторизован');
        return response.json();
    })
    .then(data => {
        if (data.role === 'ADMIN') {
            document.querySelectorAll('.admin-only').forEach(el => {
                el.style.display = 'block';
            });
        }
    })
    .catch(error => console.error('Проверка не удалась:', error));
});