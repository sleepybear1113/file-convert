function parseTimeToStr(t) {
    if (t == null || isNaN(t) || t.length === 0) {
        return "";
    }

    let date = new Date(t);
    return `${date.getFullYear()}-${time2(date.getMonth())}-${time2(date.getDay())} ${time2(date.getHours())}:${time2(date.getMinutes())}:${time2(date.getSeconds())}`;
}

function time2(t) {
    if (t < 10) {
        return "0" + t;
    }
    return t;
}