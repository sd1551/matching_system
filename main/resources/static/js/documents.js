document.addEventListener('DOMContentLoaded', () => {
    let currentPage = 0;
    const pageSize = 10;
    let isAdmin = false;

    // Проверка аутентификации и прав
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
        document.getElementById('username').textContent = data.username;
        isAdmin = data.role === 'ADMIN';

        if (isAdmin) {
            document.getElementById('adminFilters').classList.add('admin-only');
            document.querySelectorAll('.admin-only').forEach(el => {
                el.style.display = 'block';
            });
            loadAllDocuments();
        } else {
            loadUserDocuments();
        }
    })
    .catch(error => console.error(error));

    // Кнопки
    document.getElementById('backBtn').addEventListener('click', () => {
        window.location.href = '/hub';
    });

    document.getElementById('logoutBtn').addEventListener('click', logout);

    // Пагинация
    document.getElementById('prevPage').addEventListener('click', () => {
        if (currentPage > 0) {
            currentPage--;
            updateDocuments();
        }
    });

    document.getElementById('nextPage').addEventListener('click', () => {
        currentPage++;
        updateDocuments();
    });

    // Фильтры
    document.getElementById('applyFilters').addEventListener('click', () => {
        currentPage = 0;
        updateDocuments();
    });

    function updateDocuments() {
        if (isAdmin) {
            loadAllDocuments();
        } else {
            loadUserDocuments();
        }
    }

    function loadUserDocuments() {
        fetch(`/documents/my?page=${currentPage}&size=${pageSize}`, {
            credentials: 'include'
        })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка загрузки документов');
            return response.json();
        })
        .then(documents => {
            renderDocuments(documents);
            updatePagination(documents.length);
        })
        .catch(error => {
            console.error('Error:', error);
            showError(error.message);
        });
    }

    function loadAllDocuments() {
        const username = document.getElementById('usernameFilter').value;
        const date = document.getElementById('dateFilter').value;

        let url = `/documents/all?page=${currentPage}&size=${pageSize}`;
        if (username) url += `&username=${encodeURIComponent(username)}`;
        if (date) url += `&date=${date}`;

        fetch(url, {
            credentials: 'include'
        })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка загрузки документов');
            return response.json();
        })
        .then(documents => {
            renderDocuments(documents, true);
            updatePagination(documents.length);
        })
        .catch(error => {
            console.error('Error:', error);
            showError(error.message);
        });
    }

    function renderDocuments(documents, showUser = false) {
        const container = document.getElementById('documents-list');
        container.innerHTML = '';

        if (documents.length === 0) {
            container.innerHTML = '<p class="no-documents">Документы не найдены</p>';
            return;
        }

        documents.forEach((doc, index) => {
            const docElement = document.createElement('div');
            docElement.className = 'document-card';
            docElement.style.animationDelay = `${index * 0.1}s`;

            let userInfo = '';
            if (showUser && doc.username) {
                userInfo = `<div class="document-user"><i class="fas fa-user"></i> ${doc.username}</div>`;
            }

            docElement.innerHTML = `
                <div class="document-name">${doc.fileName}</div>
                <div class="document-info">
                    ${userInfo}
                    <div class="document-date"><i class="fas fa-calendar"></i> ${new Date(doc.uploadDate).toLocaleString()}</div>
                </div>
                <div class="document-actions">
                    <button class="download-btn" onclick="downloadDocument('${doc.id}', '${doc.fileName}')">
                        <i class="fas fa-download"></i> Скачать
                    </button>
                    <button class="delete-btn" onclick="deleteDocument('${doc.id}')">
                        <i class="fas fa-trash"></i> Удалить
                    </button>
                </div>
            `;
            container.appendChild(docElement);
        });
    }

    function updatePagination(docsCount) {
        document.getElementById('pageInfo').textContent = `Страница ${currentPage + 1}`;
        document.getElementById('prevPage').disabled = currentPage === 0;
        document.getElementById('nextPage').disabled = docsCount < pageSize;
    }

    function showError(message) {
        const container = document.getElementById('documents-list');
        container.innerHTML = `<p class="error-message"><i class="fas fa-exclamation-triangle"></i> ${message}</p>`;
    }
});

// Глобальные функции
async function downloadDocument(documentId, filename) {
    try {
        const response = await fetch(`/documents/download?id=${documentId}`, {
            credentials: 'include'
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Ошибка скачивания файла');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);

        // Создаем невидимую ссылку для скачивания
        const a = document.createElement('a');
        a.href = url;
        a.download = filename || 'document';
        a.style.display = 'none';

        // Добавляем ссылку в DOM и эмулируем клик
        document.body.appendChild(a);
        a.click();

        // Очистка
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    } catch (error) {
        console.error('Ошибка скачивания:', error);
        alert(`Ошибка скачивания: ${error.message}`);
    }
}

function deleteDocument(documentId) {
    if (!confirm('Вы уверены, что хотите удалить этот документ?')) return;

    fetch(`/documents/${documentId}`, {
        method: 'DELETE',
        credentials: 'include'
    })
    .then(response => {
        if (response.status === 403) {
            throw new Error('У вас нет прав для удаления этого документа');
        }
        if (!response.ok) {
            throw new Error('Ошибка удаления документа');
        }
        window.location.reload(); // Перезагружаем страницу для обновления списка
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка: ' + error.message);
    });
}