var videoplayer = new function() {

	var currentPositionRef = null;
	var currentPositionCount = 0;

    $("video").bind("pause", function() {
		if(currentPositionRef != null) window.clearTimeout(currentPositionRef);
        var playerObject = document.getElementById('video');
        var curr = playerObject.currentTime;
        var dur = playerObject.duration;
        moment().format('HH:mm:ss')
        microgramcaster.displayText(humanizeDuration(curr) + ' of ' + humanizeDuration(dur));

    });

    $("video").bind("ended", function() {
		if(currentPositionRef != null) window.clearTimeout(currentPositionRef);
		microgramcaster.displayText('Playback finished');
        setTimeout(function() {$('#splash').css('display','block')}, 15000);
    });

    var lastSegment = function(url) {
        return url.substring(url.lastIndexOf("/")+1);
    };

    var renderPlayCallback = function() {
        $('#splash').css('display', 'none');
        var playerObject = document.getElementById('video');
        var curr = playerObject.currentTime;
        var dur = playerObject.duration;
        moment().format('HH:mm:ss')
        microgramcaster.displayText('Playing \'' + lastSegment(playerObject.currentSrc) + '\' <span id="currentPosition">' + humanizeDuration(curr) + '</span> of ' + humanizeDuration(dur));

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

    $("video").bind("play", function() {
        renderPlayCallback();
    });

    $("video").bind("loadstart", function() {
        microgramcaster.displayText('Starting to load ' + lastSegment(playerObject.currentSrc));
    });
	
	$("video").bind("seeking", function() {
        var playerObject = document.getElementById('video');
        var curr = playerObject.currentTime;
        var dur = playerObject.duration;

        microgramcaster.displayText('Seeking ' + humanizeDuration(curr) + ' of ' + humanizeDuration(dur) + '...');
    });

    $("video").bind("seeked", function() {
        var playerObject = document.getElementById('video');
        var curr = playerObject.currentTime;
        var dur = playerObject.duration;
        playerObject.play();

        microgramcaster.displayText(humanizeDuration(curr) + ' of ' + humanizeDuration(dur) + '...');
        microgramcaster.sendEvent(
            {
            "type":"event",
            "eventId":"EVENT_PLAYING",
            "currentPosition": curr
            }
        );
    });

    $("video").bind("progress", function() {
        // TODO perhaps add a tiny little loading spinner somewhere...
        //microgramcaster.displayText('Buffering ' + $('video').attr('src') + "...");
    });

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
             return "video/ogv";
         } else if(endsWith(url, ".3gp")) {
             return "video/3gp";
         } else if(endsWith(url, ".vp8")) {
             return "video/vp8";
         } else {
             return "video/mp4";
         }
    }

    var playVideo = function(url) {
        $('#splash').css('display', 'none');
        $('#video').empty();
        var srcElem = document.createElement("source");
        $(srcElem).attr('src',url);
        $(srcElem).attr('type',getTypeFromUrl(url));
        $('#video').append(srcElem);
    }


    this.play = function() {
        $('#splash').css('display', 'none');
        var player = document.getElementById('video');
        player.play();
    };

    this.pause = function() {
        var player = document.getElementById('video');
        player.pause();
    };

    this.playUrl = function(url) {
        $('#splash').css('display', 'none');
        $('#video').empty();
        var srcElem = document.createElement("source");
        $(srcElem).attr('src',url);
        $(srcElem).attr('type',getTypeFromUrl(url));
        $('#video').append(srcElem);
    };

    this.addToPlaylist = function(url) {
        $('#video').append('<source src="'+url+'" type="video/mp4"/>');
    };
	
	this.seek = function(positionSeconds) {
		var player = document.getElementById('video');
		player.currentTime = positionSeconds;
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