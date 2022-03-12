// Local Imports
let database = require('../database.js');
let utils = require('../Utils.js');

// Classes
let Result = require('../classes/Result.js');
let SearchSite = require('../classes/SearchSite.js');

function go(req, res) {
    // Preprocessing before query
    let catagorie = req.query.q;

    if (!utils.isValidCatagorie(catagorie)) {
        let result = new Result();
        result.resultType = 'type-search';
        result.type = catagorie;
        result.sites = [];
        res.send(result);
        return;
    }

    // MYSQL Connection
    let searchQuery = 'SELECT sID FROM data_Sites WHERE categorie = "' + catagorie + '" AND pageCount > 1 ORDER BY rand() LIMIT 250;';
    database.connection.query(searchQuery, (err, result) => {
        if (err) throw err;
        searchCallback(res, catagorie, result);
    });
}

async function searchCallback(res, catagorie, data) {
    // Generate Data
    let sites = [];
    for (let i = 0; i < data.length; i++) {
        let site = new SearchSite(data[i].sID);
        // Make sure site data has loaded from the database
        while (true) {
            if (site.hasLoaded) {
                break;
            }
            await new Promise((resolve) => setTimeout(resolve, 3));
        }
        if (site.isValid()) {
            sites.push(site);
        }
    }

    // Create Result
    let result = new Result();
    result.resultType = 'type-search';
    result.type = catagorie;
    result.sites = sites;

    res.send(result);
}

module.exports = {
    go: function (req, res) {
        return go(req, res);
    },
};
