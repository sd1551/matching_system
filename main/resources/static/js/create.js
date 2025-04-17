document.addEventListener('DOMContentLoaded', function() {
    const hardSkillInput = document.getElementById('hardSkillInput');
    const softSkillInput = document.getElementById('softSkillInput');
    const addHardSkillBtn = document.getElementById('addHardSkillBtn');
    const addSoftSkillBtn = document.getElementById('addSoftSkillBtn');
    const hardSkillsList = document.getElementById('hardSkillsList');
    const softSkillsList = document.getElementById('softSkillsList');
    const hardSkillsSuggestions = document.getElementById('hardSkillsSuggestions');
    const softSkillsSuggestions = document.getElementById('softSkillsSuggestions');
    const vacancyForm = document.getElementById('vacancyForm');
    const backBtn = document.getElementById('backBtn');

    const hardSkills = new Set();
    const softSkills = new Set();

    // Навигация
    backBtn.addEventListener('click', () => {
        window.location.href = '/hub';
    });

    // Поиск hard skills
    hardSkillInput.addEventListener('input', debounce(async (e) => {
        const query = e.target.value.trim();
        if (query.length < 1) {
            hardSkillsSuggestions.style.display = 'none';
            return;
        }

        try {
            const response = await fetch(`/api/vacancies/hard-skills?query=${encodeURIComponent(query)}`, {
                credentials: 'include'
            });

            if (!response.ok) throw new Error('Ошибка загрузки навыков');

            const skills = await response.json();
            updateSuggestions(hardSkillsSuggestions, skills, addHardSkill);
        } catch (error) {
            console.error('Ошибка получения hard skills:', error);
            hardSkillsSuggestions.style.display = 'none';
        }
    }, 300));

    // Поиск soft skills
    softSkillInput.addEventListener('input', debounce(async (e) => {
        const query = e.target.value.trim();
        if (query.length < 1) {
            softSkillsSuggestions.style.display = 'none';
            return;
        }

        try {
            const response = await fetch(`/api/vacancies/soft-skills?query=${encodeURIComponent(query)}`, {
                credentials: 'include'
            });

            if (!response.ok) throw new Error('Ошибка загрузки навыков');

            const skills = await response.json();
            updateSuggestions(softSkillsSuggestions, skills, addSoftSkill);
        } catch (error) {
            console.error('Ошибка получения soft skills::', error);
            softSkillsSuggestions.style.display = 'none';
        }
    }, 300));

    // Добавление hard skill
    addHardSkillBtn.addEventListener('click', () => {
        const skill = hardSkillInput.value.trim();
        if (skill && !hardSkills.has(skill)) {
            addHardSkill(skill);
            hardSkillInput.value = '';
        }
    });

    // Добавление soft skill
    addSoftSkillBtn.addEventListener('click', () => {
        const skill = softSkillInput.value.trim();
        if (skill && !softSkills.has(skill)) {
            addSoftSkill(skill);
            softSkillInput.value = '';
        }
    });

    // Создание вакансии
    vacancyForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const data = {
            title: document.getElementById('title').value.trim(),
            description: document.getElementById('description').value.trim(),
            hardSkills: Array.from(hardSkills),
            softSkills: Array.from(softSkills)
        };

        console.log("Отправка:", data);

        try {
            const response = await fetch('/api/vacancies', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
                credentials: 'include'
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Ошибка сервера');
            }

            const result = await response.json();
            console.log("Response data:", result);

            alert('Вакансия создана! ID: ' + result.id);
            window.location.href = '/hub';
        } catch (error) {
            console.error('Ошибка:', error);
            alert('Ошибка: ' + error.message);
        }
    });

    // Функции для работы с навыками
    function addHardSkill(skill) {
        if (!hardSkills.has(skill)) {
            hardSkills.add(skill);
            renderSkills();

            // Добавление нового навыка в систему
            fetch(`/api/vacancies/hard-skills?query=${encodeURIComponent(skill)}`, {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(skills => {
                if (!skills.includes(skill)) {
                    return fetch(`/api/vacancies/hard-skills`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({ skill }),
                        credentials: 'include'
                    });
                }
            })
            .catch(console.error);
        }
    }

    function addSoftSkill(skill) {
        if (!softSkills.has(skill)) {
            softSkills.add(skill);
            renderSkills();

            // Добавление нового навыка в систему
            fetch(`/api/vacancies/soft-skills?query=${encodeURIComponent(skill)}`, {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(skills => {
                if (!skills.includes(skill)) {
                    return fetch(`/api/vacancies/soft-skills`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({ skill }),
                        credentials: 'include'
                    });
                }
            })
            .catch(console.error);
        }
    }

    function removeHardSkill(skill) {
        hardSkills.delete(skill);
        renderSkills();
    }

    function removeSoftSkill(skill) {
        softSkills.delete(skill);
        renderSkills();
    }

    function renderSkills() {
        hardSkillsList.innerHTML = '';
        softSkillsList.innerHTML = '';

        hardSkills.forEach(skill => {
            const tag = createSkillTag(skill, removeHardSkill);
            hardSkillsList.appendChild(tag);
        });

        softSkills.forEach(skill => {
            const tag = createSkillTag(skill, removeSoftSkill);
            softSkillsList.appendChild(tag);
        });
    }

    function updateSuggestions(container, skills, addFunction) {
        container.innerHTML = '';

        if (skills.length === 0) {
            container.style.display = 'none';
            return;
        }

        skills.forEach(skill => {
            const item = document.createElement('div');
            item.className = 'suggestion-item';
            item.textContent = skill;
            item.addEventListener('click', () => {
                addFunction(skill);
                container.style.display = 'none';
            });
            container.appendChild(item);
        });

        container.style.display = 'block';
    }

    function createSkillTag(skill, removeCallback) {
        const tag = document.createElement('div');
        tag.className = 'skill-tag';

        const text = document.createElement('span');
        text.textContent = skill;

        const removeBtn = document.createElement('button');
        removeBtn.innerHTML = '&times;';
        removeBtn.addEventListener('click', () => removeCallback(skill));

        tag.appendChild(text);
        tag.appendChild(removeBtn);

        return tag;
    }

    // Утилиты
    function debounce(func, wait) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    }
});