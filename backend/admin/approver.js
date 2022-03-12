// Local Imports
let database = require('../database.js');
let utils = require('../Utils.js');

// Classes
let Result = require('../classes/Result.js');
let SearchSite = require('../classes/SearchSite.js');

// Vars
let ap = {};
rs.RESULTS_TO_LOAD = 12;

function go(req, res) {
    // MYSQL Connection
    let searchQuery = 'SELECT data_Sites.url AS url, title, categorie AS "type" FROM data_Sites';
    searchQuery += ' JOIN data_Pages ON data_Sites.sID = data_Pages.sID';
    searchQuery += ' WHERE data_Pages.url = data_Sites.url';
    searchQuery += ' AND data_Sites.pageCount > 1';
    searchQuery += ' ORDER BY lastIndexComplete DESC LIMIT ' + rs.RECENT_SITES_TO_SHOW + ';';

    database.connection.query(searchQuery, function (err, result, fields) {
        if (err) throw err;
        searchCallback(res, result);
    });
}

async function searchCallback(res, data) {
    // Create Result
    let result = new Result();
    result.resultType = 'recent-updates';
    result.sites = data;

    res.send(result);
}

module.exports = {
    go: function (req, res) {
        return go(req, res);
    },
};
