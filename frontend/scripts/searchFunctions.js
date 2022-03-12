//+++ Factories - Generate blocks of HTML +++

function outputSites(sites, orderedList) {
    let output = '';

    let siteLength = sites.length;
    for (let i = 0; i < siteLength; i++) {
        if (orderedList) {
            output += blockFactory(sites[i], i + 1);
        } else {
            output += blockFactory(sites[i], '*');
        }
    }

    return output;
}

function outputExtraBlock(site) {
    let indexDate = moment(site.lastIndex).tz('Europe/Amsterdam', true).fromNow();
    let displayPageCount = 'dunno...';
    if (site.totalPageCount < 9999) {
        displayPageCount = site.totalPageCount;
    }

    let output = '';

    output += '<div class="block-extra">';
    output += '<ul>';
    output += '<li>Indexed ' + indexDate + '!</li>';
    output += '<li>Pages: ' + displayPageCount + '</li>';
    output += '<li><a href="mailto:webmaster@melonking.net?Subject=Reporting-Site#' + site.id + '-Subject:"target="_top">Report!</a></li>';
    output += '</ul>';
    output += '</div>';

    return output;
}

function outputPages(site) {
    let output = '';

    if (site.pages != null) {
        let pagesLength = site.pages.length;
        if (pagesLength > 0) {
            for (let i = 0; i < pagesLength; i++) {
                if (site.pages[i].url == site.url) {
                    continue;
                }

                let displayUrl = site.pages[i].url.replace(site.url, '');
                let displayTitle = site.pages[i].title.trim();
                displayTitle = displayTitle.replaceAll('<', '&lt;');
                displayTitle = displayTitle.replaceAll('>', '&gt;');
                if (displayTitle == '') {
                    displayTitle = site.title;
                }

                output += '<li><a href="' + site.pages[i].url + '" target="_blank"><h1>' + displayTitle + '</h1><h2> - ' + displayUrl + '</h2></a></li>';
            }
        }
    }

    return output;
}

function outputSearchTerms(terms) {
    let termsText = '';
    let termsLength = terms.length;

    for (let i = 0; i < termsLength; i++) {
        if (i + 1 == termsLength) {
            termsText += '<strong>' + terms[i] + '</strong>';
        } else {
            termsText += '<strong>' + terms[i] + '</strong> + ';
        }
    }

    return termsText;
}

function setSearchTop(text) {
    return '<div id="search-top">' + text + '</div>';
}

function setSearchEnd(text) {
    return '<div id="search-end">' + text + '</div>';
}

function blockFactory(site, count) {
    let displayTitle = site.title.trim();
    displayTitle = displayTitle.replaceAll('<', '&lt;');
    displayTitle = displayTitle.replaceAll('>', '&gt;');
    if (displayTitle == '') {
        displayTitle = site.url;
    }
    let block = '';

    // Custom Block Style
    processCustomStyle(site);
    block += '<style>';
    block += '#block-' + site.id + ' {';
    block += 'color:' + site.textColour + ';';
    block += 'background-color:' + site.backgroundColour + ';';
    block += "background-image: url('" + site.backgroundImage + "');";
    block += '}';
    block += '#block-' + site.id + ' h1, #block-' + site.id + ' h2 {';
    block += 'color:' + site.textColour + ';';
    block += '}';
    block += '</style>';

    // Main Block HTML
    block += '<div class="block" id="block-' + site.id + '">';
    block += '<div class="block-icons">';
    block += '<h1>' + count + '</h1>';
    block += '<a href="?t=' + site.type + '">';
    block += '<img src="images/type-icons/' + site.type + '.gif" title="' + site.type + '" />';
    block += '</a>';
    block += '</div>';
    block += '<div class="block-site">';
    block += '<a href="' + site.url + '" target="_blank">';
    block += '<h1>' + displayTitle + '</h1>';
    block += '<h2>' + site.url + '</h2>';
    block += '</a>';
    block += '<ul>' + outputPages(site) + '</ul>';
    block += outputExtraBlock(site);
    block += '</div>';
    block += '</div>';

    return block;
}

// Helper function, checks custom styling and tries to fix obvious issues
function processCustomStyle(site) {
    //Check for sites with only a text colour edit
    if (site.textColour != '' && site.backgroundColour == '' && site.backgroundImage == '') {
        //Check for sites with white text and no background.
        if (site.textColour == '#ffffff' || site.textColour == '#fff' || site.textColour == 'white') {
            site.textColour = '';
        }
    }
    //For sites that have black on or white on colours due to missing overlays
    else if (site.textColour == '#ffffff' && site.backgroundColour == '#ffffff') {
        site.backgroundColour = '#000';
    } else if (site.textColour == '#000000' && site.backgroundColour == '#000000') {
        site.textColour = '#fff';
    }
    //Fix for sites with no text colour (the default search colour is black so...)
    else if (site.textColour == '' && site.backgroundColour == '#000000') {
        site.textColour = '#fff';
    }
}

//+++ Result Renders - letious kinds of results +++

// File search
function successFileRender(results) {
    let plotText = '';

    plotText += setSearchTop('You searched for the file <strong>' + results.terms + '</strong> and found <strong>' + results.sites.length + '</strong> results!');
    plotText += outputSites(results.sites, true);
    plotText += setSearchEnd("That's all, search again buckaroo!");

    plot.html(plotText);
}

function noFileResultsRender(results) {
    let plotText = '';

    plotText += setSearchTop('You searched for the file <strong>' + results.terms + '</strong> but no files where found!');
    plotText += '<h1>No Results!</h1>';
    plotText += '<p>Mabey try search something else for now... :)</p>';

    plot.html(plotText);
}

// Term Search
function successTermRender(results) {
    let plotText = '';

    plotText += setSearchTop('You searched ' + outputSearchTerms(results.terms) + '... and found <strong>' + results.sites.length + '</strong> results!');
    plotText += outputSites(results.sites, true);
    plotText += setSearchEnd("That's all, search again buckaroo!");

    plot.html(plotText);
}

function noTermResultsRender(results) {
    let plotText = '';

    plotText += setSearchTop('You searched ' + outputSearchTerms(results.terms) + '... but no results were found!');
    plotText += '<h1>No Results!</h1>';
    plotText += '<p>Mabey try search something else for now... :)</p>';

    plot.html(plotText);
}

// Type Search
function successTypeRender(results) {
    let plotText = '';
    let topText = '';

    topText += '<div class="center big-type-icon"><img src="images/type-icons/' + results.type + '.gif"/></div>';
    topText += 'Listing <strong>' + results.sites.length + '</strong> random <strong>' + results.type + '</strong> sites... !';
    plotText += setSearchTop(topText);

    plotText += outputSites(results.sites, false);
    plotText += setSearchEnd("That's all, whatcha gonna do buckaroo?");

    plot.html(plotText);
}

function noTypeResultsRender(results) {
    let plotText = '';

    plotText += setSearchTop('Listing <strong>' + results.type + '</strong> sites... but none were found!');
    plotText += '<h1>No Results!</h1>';
    plotText += '<p>Mabey try a different category for now... :)</p>';

    plot.html(plotText);
}

//Other Results
function nothingSearched() {
    let plotText = '';

    plotText += '<div id="search-top">You searched <strong>NOTHING!</strong>... and no results were found...</div>';
    plotText += '<h1>No Search!</h1>';
    plotText += "<p>Your search didn't have any terms that could be indexed, try more specific words!</p>";

    plot.html(plotText);
}

function loadingResultsRender() {
    let plotText = '';
    plotText += '<div class="center"><br/><br/><br/><img src="images/search.gif" /><h2>A search is in progress!</h2></div>';

    plot.html(plotText);
}

function failRender() {
    let plotText = '<div class="center"><br/><br/><br/><img src="images/stop.gif" /><h2>Network Error :(</h2><p>The index server seems to be offline, please come back later!</p></div>';

    plot.html(plotText);
}

//+++ General Management +++

function renderTermSearch(results) {
    if (results.terms == null) {
        nothingSearched();
    } else if (results.sites == null) {
        noTermResultsRender(results);
    } else {
        successTermRender(results);
    }
}

function renderFileSearch(results) {
    if (results.terms == null) {
        nothingSearched();
    } else if (results.sites == null) {
        noTermResultsRender(results);
    } else {
        successFileRender(results);
    }
}

function renderTypeSearch(results) {
    if (results.type == null) {
        nothingSearched();
    } else if (results.sites == null) {
        noTypeResultsRender(results);
    } else {
        successTypeRender(results);
    }
}

function renderServerSearch(servlet, value) {
    loadingResultsRender();

    jQuery.get(SERVER_URL + servlet + value, function (data) {
        //Convert to JSON object
        let results = data;

        //Render calls
        if (results == null) {
            failRender();
        } else if (results.resultType == 'type-search') {
            renderTypeSearch(results);
        } else if (results.resultType == 'file-search') {
            renderFileSearch(results);
        } else {
            renderTermSearch(results);
        }
    });
}
