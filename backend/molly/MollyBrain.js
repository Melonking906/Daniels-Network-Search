const utils = require('../Utils.js');
const database = require('../database.js');

const MollyUser = require('./MollyUser.js');

let molly = {};

molly.users = [];
molly.gifts = new Map();
molly.siteCount = 0;
molly.siteOfDay = { title: 'Melonking.Net', url: 'https://melonking.net' };
molly.lastDailyChange = new Date();

molly.sayings = [];
molly.sayings.push('Do not panic, Im here to assist you...');
molly.sayings.push('Do you dream of cactus days on cactus hills by cactus waves?');
molly.sayings.push('This is the only search engine with PANACHE!');
molly.sayings.push('This search was founded in 2017!');
molly.sayings.push('Did you know that Melonking INVENTED the internet?');
molly.sayings.push('Watch out for falling pixels!');
molly.sayings.push('Only about 55 BILLION times cooler than google!?');

molly.sql = {};
molly.sql.siteCount = 'SELECT count(sID) AS count FROM data_Sites;';

molly.sql.randomSite = 'SELECT data_Sites.url AS url, title FROM data_Sites';
molly.sql.randomSite += ' JOIN data_Pages ON data_Sites.sID = data_Pages.sID';
molly.sql.randomSite += ' WHERE data_Pages.url = data_Sites.url';
molly.sql.randomSite += ' AND title != ""';
molly.sql.randomSite += ' AND title != data_Pages.url';
molly.sql.randomSite += ' AND data_Sites.pageCount >= 3';
molly.sql.randomSite += ' ORDER BY rand() LIMIT 1;';

// Start the loops
mollyLoop();
molly.loop = setInterval(mollyLoop, 1000 * 30);
mollyDailyLoop();
molly.dailyLoop = setInterval(mollyLoop, 1000 * 60 * 60 * 24);

//+++ Functions +++

function getUsers() {
    return molly.users;
}

function getSiteCount() {
    return molly.siteCount;
}

function getSiteOfDay() {
    return molly.siteOfDay;
}

function mollyLoop() {
    clearOldUsers(new Date());

    //Get the latest site count.
    database.connection.query(molly.sql.siteCount, function (err, result, fields) {
        if (err) throw err;
        molly.siteCount = result[0].count;
    });
}

function mollyDailyLoop() {
    getNewSiteOfDay();
    lastDailyChange = new Date();
}

function getUser(ip) {
    for (user of molly.users) {
        if (user.ip == ip) {
            return user;
        }
    }

    return undefined;
}

function updateOrAddUser(ip) {
    for (user of molly.users) {
        if (user.ip == ip) {
            user.setLastActiveNow();
            return user;
        }
    }

    let newUser = new MollyUser(ip);
    newUser.setLastActiveNow();
    molly.users.push(newUser);
    return newUser;
}

function getUser(ip) {
    for (user of molly.users) {
        if (user.ip == ip) {
            return user;
        }
    }
    return undefined;
}

function updateOrAddUser(ip) {
    for (user of molly.users) {
        if (user.ip == ip) {
            user.setLastActiveNow();
            return user;
        }
    }

    let newUser = new MollyUser(ip);
    newUser.setLastActiveNow();
    molly.users.push(newUser);
    return newUser;
}

function addUser(user) {
    for (existingUser of molly.users) {
        if (existingUser.ip == user.ip) {
            return false;
        }
    }
    molly.users.push(user);
    return true;
}

function removeUser(ip) {
    for (let i = molly.users.length - 1; i >= 0; i--) {
        let user = molly.users[i];

        if (user.ip == ip) {
            molly.users.splice(i, 1);
            return true;
        }
    }
    return false;
}

function clearOldUsers(now) {
    for (let i = molly.users.length - 1; i >= 0; i--) {
        if (now.getTime() - molly.users[i].lastActive.getTime() >= 15 * 60 * 1000) {
            molly.users.splice(i, 1);
        }
    }
}

function setUserGift(user, gift) {
    molly.gifts.set(user, gift);
}

function getNextGift(requestUser) {
    if (molly.gifts.length < 1) {
        return undefined;
    }

    for (user of molly.gifts.keys()) {
        if (user.ip != requestUser.ip) {
            let gift = molly.gifts.get(user);
            //Remove gift after its selected.
            molly.gifts.delete(user);
            return gift;
        }
    }

    return undefined; //No molly.gifts found
}

function getRandomSaying() {
    let random = utils.getRandomInt(0, molly.sayings.length - 1);
    return molly.sayings[random];
}

function getNewSiteOfDay() {
    database.connection.query(molly.sql.randomSite, function (err, result, fields) {
        if (err) throw err;
        molly.siteOfDay = result[0];
    });
}

module.exports = {
    updateOrAddUser: function (id) {
        return updateOrAddUser(id);
    },
    getUsers: function () {
        return getUsers();
    },
    getSiteCount: function () {
        return getSiteCount();
    },
    getSiteOfDay: function () {
        return getSiteOfDay();
    },
    getRandomSaying: function () {
        return getRandomSaying();
    },
    getNextGift: function (requestUser) {
        return getNextGift(requestUser);
    },
    setUserGift: function (user, gift) {
        return setUserGift(user, gift);
    },
};
