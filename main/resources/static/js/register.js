document.getElementById('registerForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorMessage = document.getElementById('error-message');

    fetch('/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw new Error(err.error || 'Регистрация провалена'); });
        }
        return response.json();
    })
    .then(() => {
        window.location.href = '/hub';
    })
    .catch(error => {
        errorMessage.textContent = error.message;
        errorMessage.style.display = 'block';
    });
});