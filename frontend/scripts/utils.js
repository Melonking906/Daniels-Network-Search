//+++ General DN Utils +++

let SERVER_URL = 'https://search.melonking.net/';

//Sounds
let sound_bell = new Audio('../audio/bell.mp3');
let sound_trumpet = new Audio('../audio/trumpet.mp3');
let sound_storm = new Audio('../audio/storm.mp3');

//Gets URL parameters
function getParameterByName(name, url) {
    if (!url) {
        url = window.location.href;
    }
    name = name.replace(/[\[\]]/g, '\\$&');
    let regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

function addParameter(url, param) {
    if (url.includes('?')) {
        return url + '&' + param;
    } else {
        return url + '?' + param;
    }
}

function getFileType(filename) {
    return filename.substring(filename.lastIndexOf('.') + 1, filename.length) || filename;
}
