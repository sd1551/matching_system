document.addEventListener('DOMContentLoaded', function() {
    // Элементы интерфейса
    const menuBtn = document.getElementById('menuBtn');
    const sidebar = document.getElementById('sidebar');
    const vacanciesList = document.getElementById('vacanciesList');
    const createVacancyBtn = document.getElementById('createVacancyBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const attachDocumentModal = document.getElementById('attachDocumentModal');
    const closeModal = document.querySelector('.close');
    const availableDocumentsList = document.getElementById('availableDocumentsList');
    const searchInput = document.getElementById('searchInput');
    const filterSelect = document.getElementById('filterSelect');
    const usernameSpan = document.getElementById('username');

    let currentVacancyId = null;
    let currentUserId = null;
    let userRole = 'USER';
    let allVacancies = [];

    // Инициализация
    initMenuToggle();
    checkAuthAndLoadVacancies();

    // Навигация
    if (createVacancyBtn) {
        createVacancyBtn.addEventListener('click', () => {
            window.location.href = '/vacancies/create';
        });
    }

    logoutBtn.addEventListener('click', logout);

    // Поиск и фильтрация
    if (searchInput) searchInput.addEventListener('input', filterVacancies);
    if (filterSelect) filterSelect.addEventListener('change', filterVacancies);

    // Модальное окно
    closeModal.addEventListener('click', () => {
        attachDocumentModal.style.display = 'none';
    });

    window.addEventListener('click', (event) => {
        if (event.target === attachDocumentModal) {
            attachDocumentModal.style.display = 'none';
        }
    });

    // Функции
    function initMenuToggle() {
        menuBtn.addEventListener('click', () => {
            sidebar.classList.toggle('active');
            menuBtn.classList.toggle('active');
        });
    }

    function checkAuthAndLoadVacancies() {
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
            userRole = data.role;
            currentUserId = data.username;
            usernameSpan.textContent = currentUserId;

            // Показываем элементы для админа
            if (userRole === 'ADMIN') {
                document.querySelectorAll('.admin-only').forEach(el => {
                    el.style.display = 'flex';
                });
            }

            loadVacancies();
        })
        .catch(error => {
            console.error('Провалена авторизация:', error);
            window.location.href = '/auth/login';
        });
    }

    function loadVacancies() {
        fetch('/api/vacancies', {
            credentials: 'include'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка загрузки вакансий');
            }
            return response.json();
        })
        .then(vacancies => {
            allVacancies = vacancies;
            renderVacancies(vacancies);
        })
        .catch(error => {
            console.error('Ошибка загрузки вакансий:', error);
            vacanciesList.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>${error.message}</p>
                </div>
            `;
        });
    }

    function filterVacancies() {
        const searchTerm = searchInput.value.toLowerCase();
        const filterValue = filterSelect.value;

        let filtered = [...allVacancies];

        // Фильтрация по поиску
        if (searchTerm) {
            filtered = filtered.filter(v =>
                v.title.toLowerCase().includes(searchTerm) ||
                v.description.toLowerCase().includes(searchTerm) ||
                (v.hardSkills && v.hardSkills.some(skill => skill.toLowerCase().includes(searchTerm))) ||
                (v.softSkills && v.softSkills.some(skill => skill.toLowerCase().includes(searchTerm)))
            );
        }

        // Дополнительная фильтрация
        if (filterValue === 'my') {
            filtered = filtered.filter(v => v.createdBy === currentUserId);
        } else if (filterValue === 'recent') {
            const twoWeeksAgo = new Date();
            twoWeeksAgo.setDate(twoWeeksAgo.getDate() - 14);
            filtered = filtered.filter(v => new Date(v.createdAt) > twoWeeksAgo);
        }

        renderVacancies(filtered);
    }

    function renderVacancies(vacancies) {
        if (vacancies.length === 0) {
            vacanciesList.innerHTML = `
                <div class="no-vacancies">
                    <i class="fas fa-briefcase"></i>
                    <p>Нет доступных вакансий</p>
                </div>
            `;
            return;
        }

        vacanciesList.innerHTML = '';
        vacancies.forEach((vacancy, index) => {
            const isOwner = vacancy.createdBy === currentUserId;
            const canDelete = userRole === 'ADMIN' || isOwner;
            const canAttach = true; // Разрешаем всем аутентифицированным пользователям прикреплять документы

            const vacancyCard = document.createElement('div');
            vacancyCard.className = 'vacancy-card';
            vacancyCard.style.animationDelay = `${index * 0.1}s`;
            vacancyCard.innerHTML = `
                <div class="vacancy-header">
                    <div class="vacancy-title">${vacancy.title}</div>
                    <div class="vacancy-meta">
                        <span class="vacancy-date">${formatDate(vacancy.createdAt)}</span>
                    </div>
                </div>
                <div class="vacancy-author">Опубликовано: ${vacancy.createdBy}</div>
                <div class="vacancy-description">${vacancy.description || 'Описание отсутствует'}</div>

                <div class="skills-container">
                    <h4>Требуемые навыки:</h4>
                    ${vacancy.hardSkills ? vacancy.hardSkills.map(skill => `<span class="skill-tag">${skill}</span>`).join('') : ''}
                </div>

                <div class="skills-container">
                    <h4>Личные качества:</h4>
                    ${vacancy.softSkills ? vacancy.softSkills.map(skill => `<span class="skill-tag">${skill}</span>`).join('') : ''}
                </div>

                <div class="documents-container">
                    <h4 class="documents-title">Прикрепленные документы:</h4>
                    ${vacancy.documents && vacancy.documents.length > 0
                        ? vacancy.documents.map(doc => `
                            <div class="document-item">
                                <span class="document-name" data-id="${doc.id}">
                                    <i class="fas fa-file-${getFileIcon(doc.fileName)}"></i>
                                    ${doc.fileName}
                                </span>
                                ${isOwner ? `
                                <button class="document-action detach-btn" data-vacancy="${vacancy.id}" data-document="${doc.id}">
                                    <i class="fas fa-unlink"></i>
                                </button>` : ''}
                            </div>`).join('')
                        : '<p class="no-documents">Нет прикрепленных документов</p>'}
                </div>

                <div class="vacancy-actions">
                    ${canAttach ? `
                    <button class="attach-btn" data-vacancy="${vacancy.id}">
                        <i class="fas fa-paperclip"></i> Прикрепить документ
                    </button>` : ''}

                    ${canDelete ? `
                    <button class="delete-btn" data-vacancy="${vacancy.id}">
                        <i class="fas fa-trash"></i> Удалить
                    </button>` : ''}
                </div>
            `;

            vacanciesList.appendChild(vacancyCard);
        });

        setupEventListeners();
    }

    function setupEventListeners() {
        // Прикрепление документа
        document.querySelectorAll('.attach-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                currentVacancyId = e.target.getAttribute('data-vacancy') ||
                                  e.target.closest('.attach-btn').getAttribute('data-vacancy');
                showAttachDocumentModal(currentVacancyId);
            });
        });

        // Удаление вакансии
        document.querySelectorAll('.delete-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const vacancyId = e.target.getAttribute('data-vacancy') ||
                                 e.target.closest('.delete-btn').getAttribute('data-vacancy');
                deleteVacancy(vacancyId);
            });
        });

        // Открепление документа
        document.querySelectorAll('.detach-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const vacancyId = e.target.getAttribute('data-vacancy');
                const documentId = e.target.getAttribute('data-document');
                detachDocument(vacancyId, documentId);
            });
        });

        // Скачивание документа
        document.querySelectorAll('.document-name').forEach(doc => {
            doc.addEventListener('click', (e) => {
                const documentId = e.target.getAttribute('data-id') ||
                                 e.target.closest('.document-name').getAttribute('data-id');
                downloadDocument(documentId);
            });
        });
    }

    function showAttachDocumentModal(vacancyId) {
        fetch('/documents/my?page=0&size=100', {
            credentials: 'include'
        })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка загрузки документов');
            return response.json();
        })
        .then(documents => {
            availableDocumentsList.innerHTML = '';

            if (documents.length === 0) {
                availableDocumentsList.innerHTML = `
                    <div class="no-documents">
                        <i class="fas fa-folder-open"></i>
                        <p>У вас нет документов для прикрепления</p>
                        <a href="/auth/upload" class="btn-primary">Загрузить документ</a>
                    </div>
                `;
            } else {
                // Фильтруем документы, которые уже прикреплены к этой вакансии
                const currentVacancy = allVacancies.find(v => v.id === vacancyId);
                const attachedDocIds = currentVacancy?.documents?.map(d => d.id) || [];

                documents.forEach(doc => {
                    if (!attachedDocIds.includes(doc.id)) {
                        const docItem = document.createElement('div');
                        docItem.className = 'document-select-item';
                        docItem.innerHTML = `
                            <span>
                                <i class="fas fa-file-${getFileIcon(doc.fileName)}"></i>
                                ${doc.fileName}
                            </span>
                            <button class="attach-doc-btn" data-document="${doc.id}">
                                <i class="fas fa-paperclip"></i> Прикрепить
                            </button>
                        `;
                        availableDocumentsList.appendChild(docItem);
                    }
                });

                if (availableDocumentsList.children.length === 0) {
                    availableDocumentsList.innerHTML = `
                        <div class="no-documents">
                            <i class="fas fa-check-circle"></i>
                            <p>Все ваши документы уже прикреплены к этой вакансии</p>
                        </div>
                    `;
                }
            }

            attachDocumentModal.style.display = 'block';

            // Добавляем обработчики для новых кнопок
            document.querySelectorAll('.attach-doc-btn').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const documentId = e.target.getAttribute('data-document') ||
                                     e.target.closest('.attach-doc-btn').getAttribute('data-document');
                    attachDocument(vacancyId, documentId);
                });
            });
        })
        .catch(error => {
            console.error('Ошибка загрузки документов:', error);
            availableDocumentsList.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Ошибка загрузки документов</p>
                </div>
            `;
            attachDocumentModal.style.display = 'block';
        });
    }

    function attachDocument(vacancyId, documentId) {
        fetch(`/api/vacancies/${vacancyId}/documents/${documentId}`, {
            method: 'POST',
            credentials: 'include'
        })
        .then(response => {
            if (response.ok) {
                showNotification('Документ успешно прикреплен', 'success');
                attachDocumentModal.style.display = 'none';
                loadVacancies();
            } else {
                throw new Error('Ошибка прикрепления документа');
            }
        })
        .catch(error => {
            console.error('Ошибка прикрепления документа:', error);
            showNotification(error.message, 'error');
        });
    }

    function detachDocument(vacancyId, documentId) {
        if (!confirm('Вы уверены, что хотите открепить этот документ?')) return;

        fetch(`/api/vacancies/${vacancyId}/documents/${documentId}`, {
            method: 'DELETE',
            credentials: 'include'
        })
        .then(response => {
            if (response.ok) {
                showNotification('Документ успешно откреплен', 'success');
                loadVacancies();
            } else {
                throw new Error('Ошибка открепления документа');
            }
        })
        .catch(error => {
            console.error('Ошибка открепления документа:', error);
            showNotification(error.message, 'error');
        });
    }

    function deleteVacancy(vacancyId) {
        if (!confirm('Вы уверены, что хотите удалить эту вакансию?')) return;

        fetch(`/api/vacancies/${vacancyId}`, {
            method: 'DELETE',
            credentials: 'include'
        })
        .then(response => {
            if (response.ok) {
                showNotification('Вакансия успешно удалена', 'success');
                loadVacancies();
            } else if (response.status === 403) {
                throw new Error('Недостаточно прав для удаления этой вакансии');
            } else {
                throw new Error('Ошибка удаления вакансии');
            }
        })
        .catch(error => {
            console.error('Ошибка удаления вакансии:', error);
            showNotification(error.message, 'error');
        });
    }

    function downloadDocument(documentId) {
        window.open(`/documents/download?id=${documentId}`, '_blank');
    }

    function logout() {
        fetch('/auth/logout', {
            method: 'POST',
            credentials: 'include'
        })
        .then(() => {
            window.location.href = '/auth/login';
        })
        .catch(error => {
            console.error('Выход из системы не выполнен:', error);
        });
    }

    // Вспомогательные функции
    function formatDate(dateString) {
        const options = { day: '2-digit', month: '2-digit', year: 'numeric' };
        return new Date(dateString).toLocaleDateString('ru-RU', options);
    }

    function getFileIcon(filename) {
        const ext = filename.split('.').pop().toLowerCase();
        const icons = {
            pdf: 'pdf',
            doc: 'word',
            docx: 'word',
            xls: 'excel',
            xlsx: 'excel',
            ppt: 'powerpoint',
            pptx: 'powerpoint',
            txt: 'alt',
            jpg: 'image',
            jpeg: 'image',
            png: 'image',
            gif: 'image'
        };
        return icons[ext] || 'alt';
    }

    function showNotification(message, type) {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
            <span>${message}</span>
        `;

        document.body.appendChild(notification);

        setTimeout(() => {
            notification.classList.add('show');
        }, 10);

        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => {
                notification.remove();
            }, 300);
        }, 3000);
    }
});