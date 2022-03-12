let moment = require('moment');

const validCategories = ['art', 'comics', 'education', 'games', 'hobbies', 'music', 'other', 'personal', 'spooky', 'technology', 'tokyo', 'writing'];

function isValidCatagorie(catagorie) {
    return validCategories.includes(catagorie);
}

function filterNonAscii(text) {
    return text.replace(/[^a-zA-Z0-9]/g, '');
}

function getRandomInt(min, max) {
    min = Math.ceil(min);
    max = Math.floor(max);
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function dateToSQL(momentDate) {
    return momentDate.format('YYYY-MM-DD HH:mm:ss');
}

function dateFromSQL(sqlDateString) {
    return moment(sqlDateString, 'YYYY-MM-DD HH:mm:ss').toDate();
}

module.exports = {
    getRandomInt: function (min, max) {
        return getRandomInt(min, max);
    },
    dateToSQL: function (momentDate) {
        return dateToSQL(momentDate);
    },
    dateFromSQL: function (sqlDateString) {
        return dateFromSQL(sqlDateString);
    },
    filterNonAscii: function (text) {
        return filterNonAscii(text);
    },
    isValidCatagorie: function (catagorie) {
        return isValidCatagorie(catagorie);
    },
};
