// Static Imports
const settings = require('./settings.json');

// Node Imports
const express = require('express');
const moment = require('moment');
const bodyParser = require('body-parser');

// Local Imports
const database = require('./database.js');

// Action Imports
const catagorySearch = require('./actions/catagorySearch.js');
const termSearch = require('./actions/termSearch.js');
const fileSearch = require('./actions/fileSearch.js');
const recentSearch = require('./actions/recentSearch.js');
const submitSite = require('./actions/submitSite.js');
const mollyAction = require('./molly/mollyAction.js');
const randomSite = require('./actions/randomSite.js');

// Main App
const app = express();
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(bodyParser.raw());

// Live Vars
global.dn = {};
dn.spellingFilter = {};
dn.molly = require('./molly/MollyBrain.js');

app.use('/', express.static('../search-frontend'));

app.get('/api/query', function (req, res) {
    termSearch.go(req, res);
});

app.get('/api/file', function (req, res) {
    fileSearch.go(req, res);
});

app.get('/api/type', function (req, res) {
    catagorySearch.go(req, res);
});

app.get('/api/recent-indexes', function (req, res) {
    recentSearch.go(req, res);
});

app.get('/api/molly', function (req, res) {
    mollyAction.go(req, res);
});

app.post('/api/submit', function (req, res) {
    submitSite.go(req, res);
});

app.get('/api/admin-subman', function (req, res) {});

app.get('/random', function (req, res) {
    randomSite.go(req, res);
});

const server = app.listen(settings.webPort, function () {
    //Start the server listening
    let host = server.address().address;
    let port = server.address().port;
    console.log('Web Server listening at http://%s:%s', host, port);

    //Setup the spelling filter
    let sqlSpellingFilter = 'SELECT fromWord, toWord FROM meta_SpellingFilter;';
    database.connection.query(sqlSpellingFilter, function (err, result, fields) {
        if (err) throw err;
        dn.spellingFilter.count = 0;
        dn.spellingFilter.fromWords = [];
        dn.spellingFilter.toWords = [];
        for (const row of Object.values(result)) {
            dn.spellingFilter.fromWords.push(row.fromWord);
            dn.spellingFilter.toWords.push(row.toWord);
            dn.spellingFilter.count++;
        }
        console.log('Spelling Filter has loaded!');
    });
});
