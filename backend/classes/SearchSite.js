let SearchPage = require('./SearchPage.js');
let database = require('../database.js');

module.exports = class SearchSite {
    constructor(id) {
        this.id = id;
        this.pages = [];
        this.url = undefined;
        this.type = undefined;
        this.title = undefined;
        this.lastIndex = undefined;
        this.totalPageCount = undefined;
        this.textColour = undefined;
        this.backgroundImage = undefined;
        this.backgroundColour = undefined;
        this.hasLoaded = false;
        this.siteMatch = 0;
        this.reloadSiteInfo(this);
    }

    isValid() {
        return this.title != undefined;
    }

    //addPage(id, url, title, match) {
    //    this.pages.push(new SearchPage(id, url, title, match));
    //}

    //sortPages() {
    //    this.pages.sort(Comparator.comparingInt(getMatch).reversed());
    //}

    getSiteMatch() {
        let matchSum = 0;
        for (let p = 0; p < this.pages.length; p++) {
            matchSum += this.pages[p].pageMatch;
        }
        this.siteMatch = matchSum;
        return matchSum;
    }

    reloadSiteInfo(target) {
        let sqlQuery = '';
        sqlQuery += 'SELECT data_Sites.url AS url, data_Sites.categorie AS categorie, data_Sites.lastIndexComplete AS lastIndex, data_Pages.title AS title, data_Sites.pageCount';
        sqlQuery += ', data_SiteCSS.textColour AS textColour, data_SiteCSS.backgroundColour AS backgroundColour, data_SiteCSS.backgroundImage AS backgroundImage ';
        sqlQuery += 'FROM data_Sites JOIN data_Pages ON data_Sites.sID = data_Pages.sID ';
        sqlQuery += 'LEFT JOIN data_SiteCSS ON data_SiteCSS.sID = data_Sites.sID ';
        sqlQuery += 'WHERE data_Sites.sID = ' + target.id + ' ';
        sqlQuery += 'AND data_Sites.url = data_Pages.url;';

        database.connection.query(sqlQuery, (err, rows) => {
            if (rows == undefined || rows[0] == undefined) {
                target.hasLoaded = true;
                return;
            }
            let siteInfo = rows[0];
            target.url = siteInfo.url;
            target.type = siteInfo.categorie;
            target.title = siteInfo.title;
            target.lastIndex = siteInfo.lastIndex;
            target.totalPageCount = siteInfo.pageCount;
            target.backgroundImage = siteInfo.backgroundImage;
            target.backgroundColour = siteInfo.backgroundColour;
            target.textColour = siteInfo.textColour;
            target.hasLoaded = true;
        });
    }
};
