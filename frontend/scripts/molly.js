//Molly Functions
function mollyMainRender(results) {
    if (results == null) {
        mollyLoadingRender();
        return;
    }

    let output = '';

    output += '<div class="center"><marquee behavior="alternate"><h2>+- <strong>CactusNav 2000</strong> -+</h2></marquee></div>';
    output += "<strong>Hello!</strong> <br/>I'm Molly the search cactus :S<br/>";
    // output += 'I am one of <strong>' + results.userCount + '</strong> active Molly units!<br/>';
    output += '<br/>' + results.saying + '<br/><br/>';
    output += 'We have <strong>' + results.siteCount + '</strong> indexed sites!<br/>';
    output += 'The site of the day is:<br/> <a href="' + results.siteOfDay.url + '" target="_blank">' + results.siteOfDay.title + ' (' + results.siteOfDay.url + ')</a>';
    output += '<br/><br/>Send a gift to a random visitor?<br/>';
    output += '<a onclick="mollySendGift( \'Flower\', \'flower.gif\' )"><img src="images/gifts/flower.gif" />Flower</a> - ';
    //output += '<a onclick="mollySendGift( \'Fish\', \'fish.gif\' )"><img src="images/gifts/fish.gif" />Fish</a> - ';
    //output += '<a onclick="mollySendGift( \'Melon\', \'melon.gif\' )"><img src="images/gifts/melon.gif" />Melon</a> - ';
    output += '<a onclick="mollySendGift( \'Storm\', \'storm.gif\' )"><img src="images/gifts/storm.gif" />Storm</a> ';

    molly_bubble.html(output);
}

function mollyLoadingRender() {
    molly_bubble.html('<div class="center"><img src="images/lightbulb.gif" /><h2>Thinking!</h2></div>');
}

function mollyFailRender() {
    molly_bubble.html('<div class="center">Molly is broken :(</div>');
}

function mollySendGift(name, image) {
    renderLock = true;
    giftToSend = name;

    molly_bubble.html('<div class="center"><img class="molly-dance" src="images/gifts/' + image + '" /><h2>You sent a ' + name + ' to someone!</h2>I hope they like it ;3</div>');
    setTimeout(mollyCloseBubble, 4000);
}

function mollyReceiveGift(name, image) {
    renderLock = true;
    sound_bell.play();

    molly_bubble.html('<div class="center"><img class="molly-dance" src="images/gifts/' + image + '" /><h2>You got a ' + name + ' from someone!</h2><a onclick="mollySendGift( \'' + name + "', '" + image + '\' )" >Send one back?</a></div>');
    mollyOpenBubble();

    if (name == 'Storm') {
        sound_storm.play();
        summonStorm();
        setTimeout(removeStorm, 25000);
    }
}

function mollyOpenBubble() {
    molly_bubble.fadeIn(50, 'linear');
    molly_image.addClass('molly-dance');
}

function mollyCloseBubble() {
    molly_bubble.fadeOut(300, 'linear');
    molly_image.removeClass('molly-dance');

    renderLock = false;
}

function summonStorm() {
    $('#overlay').fadeIn();
}

function removeStorm() {
    $('#overlay').fadeOut();
}

function mollyCall(requestGifts = true) {
    let servlet = 'api/molly';
    servlet = addParameter(servlet, 'me=' + userId); //Add a simple userid as we no longer use IP addresses for this.

    if (giftToSend != null) {
        servlet = addParameter(servlet, 'sg=' + giftToSend);
        giftToSend = null;
    }
    if (requestGifts) {
        servlet = addParameter(servlet, 'rg=1');
    }

    jQuery.get(SERVER_URL + servlet, function (data) {
        //Convert to JSON object
        let results = data;

        //Render calls
        if (results == null) {
            mollyFailRender();
        } else {
            //Success
            mollyJSON = results;
            if (results.gift != null) {
                mollyReceiveGift(results.gift.giftName, results.gift.giftImage);
            }
        }

        results = null;
    });
}

let mollyJSON = null;
let renderLock = false;
let giftToSend = null;
let userId = Math.floor(Math.random() * 99969 + 1);

let molly = $('#molly');
let molly_bubble = $('#molly_bubble');
let molly_image = $('#molly img');

//Document Ready Run Code
$(function () {
    //Events
    molly.hover(
        function () {
            mollyOpenBubble();
            if (!renderLock) {
                mollyMainRender(mollyJSON);
            }
        },
        function () {
            mollyCloseBubble();
        }
    );

    //Molly Data Loop Calls the server every 30 seconds
    clearInterval(mollyCall);
    mollyCall(false);
    setInterval(mollyCall, 30000);
});
