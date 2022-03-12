// Local Imports
let database = require('../database.js');
let utils = require('../Utils.js');

function go(req, res) {
    // MYSQL Connection
    let sql = 'SELECT url FROM data_Sites WHERE pageCount > 2 ORDER BY rand() LIMIT 1;';
    database.connection.query(sql, (err, result) => {
        if (err) throw err;
        let html = "<html><head><title>Daniel's Network Random Redirect!</title>";
        html += '<style>body{background-color:yellow;}</style>';
        html += '</head><body>';
        html += 'Redirecting you to a random site!';
        html += '<script>window.location.replace("' + result[0].url + '");</script>';
        html += '</body></html>';
        res.send(html);
    });
}

module.exports = {
    go: function (req, res) {
        return go(req, res);
    },
};
