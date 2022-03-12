// Local Imports
const database = require('../database.js');
const utils = require('../Utils.js');

// Classes
const Result = require('../classes/Result.js');
const SearchSite = require('../classes/SearchSite.js');

// Vars
let rs = {};
rs.MAX_SEARCH_TERMS = 15;
rs.MAX_TERM_LENGTH = 27;
rs.MAX_RESULT_SITES = 250;

function go(req, res) {
    // Preprocess Query
    let rawTerms = req.query.q.split(' ');
    let terms = cleanTerms(rawTerms);
    if (terms.length < 1) {
        let result = new Result();
        result.resultType = 'term-search';
        return result;
    }

    //Process search query
    let equalsWhere = '';
    let likeWhere = '';
    for (let i = 0; i < terms.length; i++) {
        if (i == 0) {
            equalsWhere += "WHERE word_Dictionary.word = '" + terms[i] + "'";
            likeWhere += "WHERE url LIKE '%" + terms[i] + "%'";
        } else {
            equalsWhere += " AND word_Dictionary.word = '" + terms[i] + "'";
            likeWhere += " AND url LIKE '%" + terms[i] + "%'";
        }
    }

    let searchQuery = '';
    searchQuery += 'SELECT sID, pID, url, title, sum(miniScore) AS pageMatch ';
    searchQuery += 'FROM ( ';
    searchQuery += '    SELECT data_Pages.sID, data_Pages.pID, data_Pages.url, data_Pages.title, 1 AS miniScore ';
    searchQuery += '    FROM data_Pages ';
    searchQuery += '    JOIN index_WordOccurrences ON data_Pages.pID = index_WordOccurrences.pID ';
    searchQuery += '    JOIN word_Dictionary ON index_WordOccurrences.wID = word_Dictionary.wID ';
    searchQuery += '    ' + equalsWhere + ' ';
    searchQuery += '    UNION ALL ';
    searchQuery += '    SELECT data_Pages.sID, data_Pages.pID, data_Pages.url, data_Pages.title, 100 AS miniScore ';
    searchQuery += '    FROM data_Pages ';
    searchQuery += '    JOIN index_TitleOccurrences ON data_Pages.pID = index_TitleOccurrences.pID ';
    searchQuery += '    JOIN word_Dictionary ON index_TitleOccurrences.wID = word_Dictionary.wID ';
    searchQuery += '    ' + equalsWhere + ' ';
    searchQuery += '    UNION ALL ';
    searchQuery += '    SELECT data_Pages.sID, data_Pages.pID, data_Pages.url, data_Pages.title, 2 AS miniScore ';
    searchQuery += '    FROM data_Pages ';
    searchQuery += '    ' + likeWhere + ' ';
    searchQuery += ') RESULTS ';
    searchQuery += 'GROUP BY pID ORDER BY sID;'; //Results always arrive ordered by site.

    // MYSQL Connection
    database.connection.query(searchQuery, function (err, result, fields) {
        if (err) throw err;
        searchCallback(res, terms, result);
    });
}

async function searchCallback(res, terms, data) {
    let sites = [];
    const siteIDs = [...new Set(data.map((page) => page.sID))]; // Get all sites from the page batch

    for (let i = 0; i < siteIDs.length; i++) {
        let site = new SearchSite(siteIDs[i]);
        site.pages = data.filter((page) => {
            // Get all the pages related to the site
            return page.sID == siteIDs[i];
        });
        site.getSiteMatch();
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

    //Sort sites be page match result
    sites.sort((a, b) => parseFloat(b.siteMatch) - parseFloat(a.siteMatch));

    //Limit results sent
    sites.length = Math.min(sites.length, rs.MAX_RESULT_SITES);

    //Create Result
    let result = new Result();
    result.resultType = 'term-search';
    result.sites = sites;
    result.terms = terms;

    res.send(result);

    //Log the searched words and results for analytics
    database.connection.query("INSERT INTO log_Search (terms, results) VALUES ('" + terms + "', '" + siteIDs + "');");
}

function cleanTerms(rawTerms) {
    let terms = [];
    for (rawTerm in rawTerms) {
        let term = rawTerms[rawTerm];
        //Ensure someone cant spam the server with a long request
        if (terms.length >= rs.MAX_SEARCH_TERMS) {
            break;
        }

        //Stop a long string slowing down the system.
        if (term.length > rs.MAX_TERM_LENGTH) {
            continue;
        }

        term = utils.filterNonAscii(term);
        term = term.toLowerCase();
        //term = database.connection.escape(term);

        if (term == '') {
            continue;
        }

        //Spelling filter
        for (let i = 0; i < dn.spellingFilter.count; i++) {
            if (term == dn.spellingFilter.fromWords[i]) {
                term = dn.spellingFilter.toWords[i];
                break;
            }
        }

        if (terms.includes(term)) {
            continue;
        }

        terms.push(term);
    }

    return terms;
}

module.exports = {
    go: function (req, res) {
        return go(req, res);
    },
};
