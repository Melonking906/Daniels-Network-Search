let mysql = require('mysql');

let connection = mysql.createPool({
    host: 'mysql.database.com',
    user: 'XXX',
    password: 'XXX',
    database: 'XXX',
});

function connect() {
    connection.connect(function (err) {
        if (err) {
            connect();
            return console.error('error: ' + err.message);
        }

        console.log('Connected to the MySQL server.');
    });
}

function disconnect() {
    connection.end(function (err) {
        if (err) {
            return console.log('error:' + err.message);
        }
        console.log('Closed the database connection.');
    });
}

module.exports = {
    connection: connection,
    connect: function () {
        return connect();
    },
    disconnect: function () {
        return disconnect();
    },
};
