// Local Imports
const database = require('../database.js');
const utils = require('../Utils.js');

// Classes
const MollyTransmission = require('./MollyTransmission.js');
const MollyGift = require('./MollyGift.js');

function go(req, res) {
    let userId = req.query.me;
    if (userId == undefined) {
        res.send('Error talking to Molly');
        return;
    }

    let user = dn.molly.updateOrAddUser(userId);

    //Send Gift
    let sentGift = req.query.sg;
    if (sentGift != undefined) {
        let gift = new MollyGift(sentGift.toLowerCase());
        dn.molly.setUserGift(user, gift);
    }

    //Receive Gift
    let receiveGifts = false;
    let requestGift = req.query.rg;
    if (requestGift != undefined) {
        receiveGifts = true;
    }

    //Result Maker
    let dataBox = new MollyTransmission();
    dataBox.siteCount = dn.molly.getSiteCount();
    dataBox.siteOfDay = dn.molly.getSiteOfDay();
    dataBox.saying = dn.molly.getRandomSaying();

    if (receiveGifts) {
        dataBox.gift = dn.molly.getNextGift(user);
    }

    res.send(dataBox);
}

module.exports = {
    go: function (req, res) {
        return go(req, res);
    },
};
