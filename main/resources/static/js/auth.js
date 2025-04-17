function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

function checkAuth() {
    fetch('/auth/check', {
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) {
            if (['/auth/login', '/auth/register', '/'].includes(window.location.pathname)) {
                window.location.href = '/hub';
            }
        } else {
            if (!['/auth/login', '/auth/register'].includes(window.location.pathname)) {
                window.location.href = '/auth/login';
            }
        }
    })
    .catch(() => {
        if (!['/auth/login', '/auth/register'].includes(window.location.pathname)) {
            window.location.href = '/auth/login';
        }
    });
}

function logout() {
    fetch('/auth/logout', {
        method: 'POST',
        credentials: 'include'
    }).then(() => {
        window.location.href = '/auth/login';
    });
}

// Проверяем аутентификацию при загрузке каждой страницы
document.addEventListener('DOMContentLoaded', checkAuth);