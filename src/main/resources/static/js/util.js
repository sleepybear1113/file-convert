function parseTimeToStr(t) {
    if (t == null || isNaN(t) || t.length === 0) {
        return "";
    }

    let date = new Date(t);
    return `${date.getFullYear()}-${time2(date.getMonth() + 1)}-${time2(date.getDate())} ${time2(date.getHours())}:${time2(date.getMinutes())}:${time2(date.getSeconds())}`;
}

function parseFileSize(size) {
    if (size == null || isNaN(size) || size.length === 0) {
        return "";
    }

    let unit = ["B", "KB", "MB", "GB", "TB"];
    let i = 0;
    while (size >= 1024 && i < unit.length) {
        size /= 1024;
        i++;
    }

    return size.toFixed(2) + unit[i];
}

function time2(t) {
    if (t < 10) {
        return "0" + t;
    }
    return t;
}

function showAlertWarning(message, strong) {
    showAlert(message, strong, "alert-warning");
}

function showAlertDanger(message, strong) {
    showAlert(message, strong, "alert-danger");
}

function showAlertSuccess(message, strong) {
    showAlert(message, strong, "alert-success");
}

function showAlert(message, strong, type) {
    message = message || "";
    strong = strong || "";
    let alert = document.getElementById("alert-div");
    let id = "alert-" + new Date().getTime();
    alert.innerHTML = "";
    let innerHTML = `
            <div id="${id}" class="alert ${type} alert-dismissible fade show fixed-top" role="alert">
                <strong id="alert-strong">${strong}</strong>
                <span id="alert-text">${message}</span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>`;

    setTimeout(() => {
        alert.innerHTML = innerHTML;
    }, 100);

    setTimeout(() => {
        let alert = document.getElementById(id);
        if (alert) {
            alert.remove();
        }
    }, 3000);
}