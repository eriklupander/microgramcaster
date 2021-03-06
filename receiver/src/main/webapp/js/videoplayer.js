var videoplayer = new function() {

    var showSplashHandler = null;
	var currentPositionRef = null;
	var currentPositionCount = 0;

    var currentlyPlayingTitle;

    var cancelSplash = function() {
        if(typeof showSplashHandler != 'undefined' && showSplashHandler != null) {
            window.clearInterval(showSplashHandler);
        }
    }

    var timeUpdateAfterStartCallback = function() {
        var playerObject = document.getElementById('video');
        var newcurr = Math.floor(playerObject.currentTime);
        var duration = playerObject.duration;
        if(newcurr != lastCurrentPosition) {
            microgramcaster.sendPlaying(newcurr, duration);
            $("video").unbind("timeupdate");
        }
    };

    $("video").bind("playing", function() {
        cancelSplash();
        var playerObject = document.getElementById('video');
        var curr = playerObject.currentTime;

        lastCurrentPosition = Math.floor(curr);

//        if(typeof currentlyPlayingTitle != 'undefined' && currentlyPlayingTitle != null) {
//            microgramcaster.displayText('Playing \'' + currentlyPlayingTitle + '\'');
//        } else {
//            microgramcaster.displayText('Playing \'' + lastSegment(playerObject.currentSrc));
//        }


        $('.overlay').css('opacity', 1.0);
        $('.overlay').stop().fadeOut(5000, function() {
            $('.overlay').css('display','none');
            $('.overlay').css('opacity', 0.0);
        });

        $('.overlay').css('display','block');
        $("video").bind("timeupdate", timeUpdateAfterStartCallback);
    });

    $("video").bind("pause", function() {
		if(currentPositionRef != null) window.clearTimeout(currentPositionRef);
        var playerObject = document.getElementById('video');
        var curr = playerObject.currentTime;
        var dur = playerObject.duration;
        moment().format('HH:mm:ss')
        microgramcaster.displayText(humanizeDuration(curr) + ' of ' + humanizeDuration(dur));

        $('.overlay').css('opacity', 1.0);
        $('.overlay').stop().fadeOut(5000, function() {
            $('.overlay').css('display','none');
            $('.overlay').css('opacity', 0.0);
        });

        $('.overlay').css('display','block');
        microgramcaster.sendPaused(curr, dur);
    });

    $("video").bind("ended", function() {
        if($('#video').hasClass('rotate90')) {
            $('#video').removeClass('rotate90');
        }
		if(currentPositionRef != null) window.clearTimeout(currentPositionRef);
		microgramcaster.displayText('Playback finished');
        microgramcaster.sendFinished();

        showSplashHandler = setTimeout(function() {$('#splash').css('display','block')}, 15000);
    });

    $("video").bind("play", function() {
        $('.overlay').css('display','block');
        $('.overlay').css('opacity', 1.0);
        renderPlayCallback();
    });

    $("video").bind("loadstart", function() {
        cancelSplash();
        var playerObject = document.getElementById('video');
        microgramcaster.displayText('Loading...');
        if(typeof currentlyPlayingTitle != 'undefined' && currentlyPlayingTitle != null) {
            // microgramcaster.displayText('Starting to load \'' + currentlyPlayingTitle + '\'');
        } else {
            //microgramcaster.displayText('Starting to load ' + lastSegment(playerObject.currentSrc));
        }

    });

    $("video").bind("seeking", function() {
        var playerObject = document.getElementById('video');
        var curr = playerObject.currentTime;
        var dur = playerObject.duration;

        microgramcaster.displayText('Seeking ' + humanizeDuration(curr) + ' of ' + humanizeDuration(dur) + '...');
    });

    $("video").bind("waiting", function() {
        microgramcaster.displayLoadingSpinner();
    });

    var lastCurrentPosition = -1;

    $("video").bind("seeked", function() {
        var playerObject = document.getElementById('video');
        var curr = playerObject.currentTime;
        lastCurrentPosition = Math.floor(curr);
        var dur = playerObject.duration;
        playerObject.play();
        $("video").bind("timeupdate", timeUpdateAfterStartCallback);
    });


    var lastSegment = function(url) {
        return url.substring(url.lastIndexOf("/")+1);
    };

    var renderPlayCallback = function() {
        cancelSplash();
        $('#splash').css('display', 'none');
        var playerObject = document.getElementById('video');
        var curr = playerObject.currentTime;
        var dur = playerObject.duration;
        moment().format('HH:mm:ss');
        microgramcaster.displayText('<span id="currentPosition">'+humanizeDuration(curr) + '</span> of ' + humanizeDuration(dur));
        if(typeof currentlyPlayingTitle != 'undefined' && currentlyPlayingTitle != null) {
           // microgramcaster.displayText('Playing \'' + currentlyPlayingTitle + '\' <span id="currentPosition">' + humanizeDuration(curr) + '</span> of ' + humanizeDuration(dur));
        } else {
          //  microgramcaster.displayText('Playing \'' + lastSegment(playerObject.currentSrc) + '\' <span id="currentPosition">' + humanizeDuration(curr) + '</span> of ' + humanizeDuration(dur));
        }


        currentPositionCount = 0;
        currentPositionRef = setInterval(function() {
            var playerObject = document.getElementById('video');
            var curr = playerObject.currentTime;
            $('#currentPosition').text(humanizeDuration(curr));
            currentPositionCount++;
            if(currentPositionCount > 4) {
                window.clearTimeout(currentPositionRef);
            }
        }, 1000);
    };


    var humanizeDuration = function(input, units ) {
      // units is a string with possible values of y, M, w, d, h, m, s, ms
      var duration = moment().startOf('day').add('s', input),
        format = "";

      if(duration.hour() > 0){ 
		format += "HH:"; 
	  }
      format += "mm:ss";

      return duration.format(format);
    }

    var endsWith = function(str, suffix) {
        return str.indexOf(suffix, str.length - suffix.length) !== -1;
    }

    var getTypeFromUrl = function(url) {
         if(endsWith(url, ".mp4")) {
             return "video/mp4";
         } else if(endsWith(url, ".ogv")) {
             return ""; // "video/ogv";
         } else if(endsWith(url, ".ogg")) {
             return "video/ogg";
         } else if(endsWith(url, ".3gp")) {
             return "video/3gp";
         } else if(endsWith(url, ".vp8")) {
             return "video/vp8";
         } else if(endsWith(url, ".mkv")) {
             return "";
         } else {
             return "video/mp4";
         }
    }

    this.play = function() {
        cancelSplash();
        $('#splash').css('display', 'none');
        var player = document.getElementById('video');
        player.play();

    };

    this.pause = function() {
        var player = document.getElementById('video');
        player.pause();
        $('.overlay').css('display','block');
    };

    /**
     * Called when a new video is to be played.
     *
     * @param url
     *      The actual URL to playback the video stream from.
     * @param title
     *      Human-readable title.
     */
    this.playUrl = function(url, title, producer, thumbnailUrl) {
        if(typeof title != 'undefined' && title != null) {
            currentlyPlayingTitle = title;
            $('.titleinfo_title').html(title);
        } else {
            $('.titleinfo_title').html(url);
            currentlyPlayingTitle = null;
        }

        // Update overlay
        if(typeof thumbnailUrl != 'undefined' && thumbnailUrl != null) {
            $('#thumbnail').attr('src',thumbnailUrl);
        } else {
            $('#thumbnail').attr('src','coverart.png');
        }
        if(typeof producer != 'undefined' && producer != null) {
            $('.titleinfo_author').html(producer);
        } else {
            $('.titleinfo_author').empty();
        }


        if($('#video').hasClass('rotate90')) {
            $('#video').removeClass('rotate90');
        }
        $('#splash').css('display', 'none');
        $('#video').empty();
        var srcElem = document.createElement("source");
        $(srcElem).attr('src',url);
        var contentType = getTypeFromUrl(url);
        if(contentType != "") {
            $(srcElem).attr('type', contentType);
        } else {
            $(srcElem).removeAttr('type');
        }

        $('#video').append(srcElem);
        this.play();
        $('.overlay').css('display','block');
    };

    this.addToPlaylist = function(url) {
        $('#video').append('<source src="'+url+'" type="video/mp4"/>');
    };
	
	this.seek = function(positionSeconds) {
        try {
            var player = document.getElementById('video');
            player.currentTime = positionSeconds;
        } catch(e) {
            microgramcaster.displayText('No clip seekable at this moment, please try again...');
        }
	};
	
	this.getCurrentPosition = function() {
		var player = document.getElementById('video');
		return player.currentTime;
	};

    this.toggleRotation = function() {
        var player = document.getElementById('video');
        player.pause();
        if($('#video').hasClass('rotate90')) {
            $('#video').removeClass('rotate90');
        } else {
            $('#video').addClass('rotate90');
        }
        player.play();
    }
};