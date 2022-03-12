const validUrl = require('valid-url');

// Local Imports
const database = require('../database.js');
const utils = require('../Utils.js');

// Classes
const Result = require('../classes/Result.js');
const SearchSite = require('../classes/SearchSite.js');

function go(req, res) {
    let url = req.body.address;
    let catagorie = req.body.type;

    if (!validUrl.isWebUri(url)) {
        res.end('URL ERROR');
        return;
    }

    if (!utils.isValidCatagorie(catagorie)) {
        res.end('CATAGORIE ERROR');
        return;
    }

    // DN requirement that all urls end in a /
    let lastUrlChar = url.substr(url.length - 1);
    if (lastUrlChar != '/') {
        url += '/';
    }

    //url = database.connection.escape(url);

    let sql = "INSERT IGNORE INTO submit_Sites (url, type) VALUES ('" + url + "', '" + catagorie + "');";
    database.connection.query(sql, function (err, result, fields) {
        if (err) throw err;
        submitCallback(res);
    });
}

async function submitCallback(res) {
    res.end('OK');
}

module.exports = {
    go: function (req, res) {
        return go(req, res);
    },
};
