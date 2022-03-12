//Daniel's Net Loader 1.0 - We are going to change the world.

//++ Functions ++
function loadPlotDocument(path) {
    path += '?t=' + Date.now(); //Timestamp the get to bypass cache.

    plot.html('<div class="center"><br/><br/><br/><img src="images/search.gif" /><h2>Fetching your page!</h2></div>');

    jQuery.get(path, function (data) {
        if (data == null) {
            plot.html("<h1>404 - The page you're looking for could not be found :(</h1>");
        } else {
            plot.html(data);
        }
    });
}

function contentRender(url) {
    let renderHTML = '';
    let termsFileType = getFileType(url);
    if (termsFileType == 'gif' || termsFileType == 'jpeg' || termsFileType == 'png' || termsFileType == 'jpg') {
        renderHTML = '<div class="center"><h1>Image Render! (' + url + ')</h1><img src="' + url + '" /></div>';
    } else {
        renderHTML = '<div class="center"><h1>Going to address... (' + url + ')</h1></div>';
        window.open(url, '_blank');
    }

    plot.html(renderHTML);
}

function bitsLink(page) {
    loadPlotDocument('bits/' + page + '.htm');
    history.pushState('', '', '?z=' + page);
}

//+++ letiables +++
let plot = $('#plot-inner');
let searchText = $('#search-text');

//+++ Page Load Engage +++
$(function () {
    //Event Handelers
    $('#search').submit(function (e) {
        e.preventDefault();

        let terms = $('#search-text').val().trim();

        //Nothing searched
        if (terms == '' || terms == null) {
            nothingSearched();
            return;
        }

        //Link Render
        if (terms.includes('http')) {
            contentRender(terms);
            return;
        }

        // Handle a Search
        terms = terms.replace(/ /g, '+');
        if (terms.includes('.')) {
            renderServerSearch('api/file?q=', terms); //File Query
        } else {
            renderServerSearch('api/query?q=', terms); //Regular Query
        }
        history.pushState('', '', '?q=' + terms);
    });

    $('header table a').click(function () {
        let type = $(this).html();

        renderServerSearch('api/type?q=', type);
        history.pushState('', '', '?t=' + type);
    });

    $('#nav-top').click(function () {
        $('html, body').animate({ scrollTop: 0 }, 1000);
    });

    //Page rendering
    let query = getParameterByName('q');
    let type = getParameterByName('t');
    let page = getParameterByName('z');

    if (query != null) {
        searchText.val(query);
        if (query.includes('.')) {
            renderServerSearch('api/file?q=', query); //File Query
        } else {
            renderServerSearch('api/query?q=', query); //Regular Query
        }
    } else if (type != null) {
        renderServerSearch('api/type?q=', type);
    } else if (page != null) {
        bitsLink(page);
    } else {
        loadPlotDocument('/bits/home.htm');
    }
});
