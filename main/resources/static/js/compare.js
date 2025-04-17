document.addEventListener('DOMContentLoaded', function() {
    const selectionPhase = document.getElementById('selectionPhase');
    const resultsPhase = document.getElementById('resultsPhase');
    const vacanciesList = document.getElementById('vacanciesList');
    const comparisonResults = document.getElementById('comparisonResults');
    const loadingOverlay = document.getElementById('loadingOverlay');
    const backBtn = document.getElementById('backBtn');
    const sendAllBtn = document.getElementById('sendAllBtn');
    const saveNotesBtn = document.getElementById('saveNotesBtn');
    const adminNotes = document.getElementById('adminNotes');
    const vacancyTitle = document.getElementById('vacancyTitle');

    let currentComparisonId = null;

    // Загрузка вакансий с документами
    function loadVacancies() {
        fetch('/compare/vacancies', {
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            if (data.length === 0) {
                vacanciesList.innerHTML = '<p>Нет вакансий с прикрепленными резюме</p>';
                return;
            }

            vacanciesList.innerHTML = '';
            data.forEach((vacancy, index) => {
                const vacancyCard = document.createElement('div');
                vacancyCard.className = 'vacancy-card';
                vacancyCard.style.animationDelay = `${index * 0.1}s`;
                vacancyCard.innerHTML = `
                    <h3>${vacancy.vacancyTitle}</h3>
                    <div class="document-list">
                        ${vacancy.documents.map(doc => `
                            <div class="document-item">
                                <span>${doc.fileName}</span>
                                <small>${doc.userName}</small>
                            </div>
                        `).join('')}
                    </div>
                    <button class="btn-primary compare-btn" data-vacancy-id="${vacancy.vacancyId}">
                        <i class="fas fa-code-compare"></i> Сравнить
                    </button>
                `;
                vacanciesList.appendChild(vacancyCard);
            });

            // Обработчики для кнопок сравнения
            document.querySelectorAll('.compare-btn').forEach(btn => {
                btn.addEventListener('click', function() {
                    const vacancyId = this.getAttribute('data-vacancy-id');
                    startComparison(vacancyId);
                });
            });
        })
        .catch(error => {
            console.error('Ошибка загрузки вакансий:', error);
            vacanciesList.innerHTML = '<p class="error">Ошибка загрузки данных</p>';
        });
    }

    // Начать сравнение
    function startComparison(vacancyId) {
        loadingOverlay.style.display = 'flex';

        fetch(`/compare/start?vacancyId=${vacancyId}`, {
            method: 'POST',
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            currentComparisonId = data.id;
            showComparisonResults(data);
        })
        .catch(error => {
            console.error('Ошибка начала сравнения:', error);
            alert('Ошибка при запуске сравнения');
        })
        .finally(() => {
            loadingOverlay.style.display = 'none';
        });
    }

    // Показать результаты сравнения
    function showComparisonResults(data) {
        selectionPhase.style.display = 'none';
        resultsPhase.style.display = 'block';

        vacancyTitle.textContent = data.vacancyTitle;
        adminNotes.value = data.adminNotes || '';

        comparisonResults.innerHTML = '';
        data.comparisons.forEach((comparison, index) => {
            const resultCard = document.createElement('div');
            resultCard.className = 'result-card';
            resultCard.style.animationDelay = `${index * 0.1}s`;
            resultCard.innerHTML = `
                <h3>${comparison.documentName} (${comparison.userName})</h3>
                <div class="graph-container">
                    <img src="${comparison.graphImagePath}" alt="Граф анализа резюме">
                </div>
                <div class="comparison-text">
                    <h4>Анализ резюме:</h4>
                    <p>${comparison.comparisonText}</p>
                </div>
                <div class="recommendation">
                    <h4>Рекомендации для кандидата:</h4>
                    <textarea data-document-id="${comparison.documentId}"
                              ${comparison.isSent ? 'disabled' : ''}>${comparison.recommendation}</textarea>
                    <button class="btn-primary send-btn" data-document-id="${comparison.documentId}"
                            ${comparison.isSent ? 'disabled' : ''}>
                        <i class="fas fa-paper-plane"></i> Отправить
                    </button>
                </div>
            `;
            comparisonResults.appendChild(resultCard);
        });

        // Добавляем обработчики для кнопок отправки
        document.querySelectorAll('.send-btn').forEach(btn => {
            if (!btn.disabled) {
                btn.addEventListener('click', function() {
                    const documentId = this.getAttribute('data-document-id');
                    sendRecommendation(documentId);
                });
            }
        });
    }

    // Отправить рекомендацию для одного документа
    function sendRecommendation(documentId) {
        if (!currentComparisonId || !confirm('Отправить рекомендацию этому кандидату?')) return;

        const recommendation = document.querySelector(`.recommendation textarea[data-document-id="${documentId}"]`).value;

        fetch(`/compare/send-one?comparisonId=${currentComparisonId}&documentId=${documentId}`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ recommendation })
        })
        .then(response => {
            if (response.ok) {
                alert('Рекомендация отправлена');
                loadComparisonResults(currentComparisonId);
            } else {
                throw new Error('Ошибка отправки');
            }
        })
        .catch(error => {
            console.error('Ошибка отправки рекомендации:', error);
            alert('Ошибка отправки');
        });
    }

    // Вернуться к выбору вакансий
    backBtn.addEventListener('click', function() {
        selectionPhase.style.display = 'block';
        resultsPhase.style.display = 'none';
        currentComparisonId = null;
    });

    // Сохранить заметки администратора
    saveNotesBtn.addEventListener('click', function() {
        if (!currentComparisonId) return;

        fetch('/compare/update', {
            method: 'PUT',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                comparisonId: currentComparisonId,
                adminNotes: adminNotes.value
            })
        })
        .then(response => {
            if (response.ok) {
                alert('Заметки сохранены');
            } else {
                throw new Error('Ошибка сохранения');
            }
        })
        .catch(error => {
            console.error('Ошибка сохранения заметок:', error);
            alert('Ошибка сохранения');
        });
    });

    // Отправить все рекомендации
    sendAllBtn.addEventListener('click', function() {
        if (!currentComparisonId || !confirm('Отправить все рекомендации кандидатам?')) return;

        fetch(`/compare/send-all?comparisonId=${currentComparisonId}`, {
            method: 'POST',
            credentials: 'include'
        })
        .then(response => {
            if (response.ok) {
                alert('Все рекомендации отправлены');
                loadComparisonResults(currentComparisonId);
            } else {
                throw new Error('Ошибка отправки');
            }
        })
        .catch(error => {
            console.error('Ошибка отправки рекомендаций:', error);
            alert('Ошибка отправки');
        });
    });

    // Загрузить результаты сравнения
    function loadComparisonResults(comparisonId) {
        fetch(`/compare/result/${comparisonId}`, {
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            showComparisonResults(data);
        })
        .catch(error => {
            console.error('Ошибка загрузки:', error);
        });
    }

    // Инициализация
    loadVacancies();
});