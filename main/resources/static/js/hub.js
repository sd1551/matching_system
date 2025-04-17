document.addEventListener('DOMContentLoaded', function() {
    fetch('/auth/check', {
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            window.location.href = '/auth/login';
            throw new Error('Не авторизован');
        }
        return response.json();
    })
    .then(data => {
        console.log("User data:", data);
        document.getElementById('username').textContent = data.username;

        if (data.role === 'ADMIN') {
            document.querySelectorAll('.admin-only').forEach(el => {
                el.style.display = 'block';
            });
        }
    })
    .catch(error => {
        console.error('Проверка авторизации не удалась:', error);
        window.location.href = '/auth/login';
    });
    // Активация меню
            document.getElementById('menuBtn').addEventListener('click', function() {
                document.getElementById('sidebar').classList.toggle('active');
            });

    document.getElementById('logoutBtn').addEventListener('click', logout);

    document.getElementById('uploadBtn').addEventListener('click', () => {
        window.location.href = '/auth/upload';
    });
    document.getElementById('viewDocsBtn').addEventListener('click', () => {
        window.location.href = '/documents';
    });
    document.getElementById('createVacancyBtn').addEventListener('click', () => {
            window.location.href = '/vacancies/create';
    });
    document.getElementById('compareBtn').addEventListener('click', () => {
                window.location.href = '/compare';
        });

    document.getElementById('viewVacanciesBtn').addEventListener('click', () => {
            window.location.href = '/vacancies';
        });
});

// hub.js
function decodeJwtPayload(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const padding = '='.repeat((4 - (base64.length % 4)) % 4);
    const decoded = atob(base64 + padding);
    return JSON.parse(decoded);
}

function getUsernameFromToken(token) {
    try {
        const payload = decodeJwtPayload(token);
        return payload.sub;
    } catch (e) {
        console.error('Ошибка декодирования токена:', e);
        return 'Unknown';
    }
}

function getRoleFromToken(token) {
    try {
        const payload = decodeJwtPayload(token);
        return payload.role;
    } catch (e) {
        console.error('Ошибка декодирования роли:', e);
        return 'USER';
    }
}