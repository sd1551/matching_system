document.getElementById("uploadForm").addEventListener("submit", function(event) {
    event.preventDefault();
    const file = document.getElementById("fileInput").files[0];

    fetch('/auth/check', {
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) throw new Error("Не авторизован");
        return fetch("/auth/upload", {
            method: "POST",
            body: new FormData(document.getElementById("uploadForm")),
            credentials: 'include'
        });
    })
    .then(response => response.text())
    .then(data => {
        alert("File uploaded: " + data);
        document.getElementById("message").innerText = "Файл успешно загружен!";
    })
    .catch(error => {
        console.error("Ошибка:", error);
        document.getElementById("message").innerText = "Ошибка: " + error.message;
    });
});