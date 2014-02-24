var videoplayer = new function() {

	var currentPositionRef = null;
	var currentPositionCount = 0;

    $("video").bind("pause", function() {
		if(currentPositionRef != null) window.clearTimeout(currentPositionRef);
        var playedObject = document.getElementById('video');
        var curr = playedObject.currentTime;
        var dur = playedObject.duration;
        moment().format('HH:mm:ss')
        microgramcaster.displayText(humanizeDuration(curr) + ' of ' + humanizeDuration(dur));

    });
    $("video").bind("ended", function() {
		if(currentPositionRef != null) window.clearTimeout(currentPositionRef);
		microgramcaster.displayText('Playback finished');
        setTimeout(function() {$('#splash').css('display','block')}, 5000);
    });
    $("video").bind("play", function() {
        $('#splash').css('display', 'none');
        var file = $('video').attr('src');
        var playedObject = document.getElementById('video');
        var curr = playedObject.currentTime;
        var dur = playedObject.duration;
        moment().format('HH:mm:ss')
        microgramcaster.displayText('Playing \'' + file + '\' <span id="currentPosition">' + humanizeDuration(curr) + '</span> of ' + humanizeDuration(dur));
		currentPositionCount = 0;
		currentPositionRef = setInterval(function() {
			var playedObject = document.getElementById('video');
			var curr = playedObject.currentTime;
			$('#currentPosition').text(humanizeDuration(curr));
			currentPositionCount++;
			if(currentPositionCount > 4) {
				window.clearTimeout(currentPositionRef);
			}
		}, 1000);
    });

    $("video").bind("loadstart", function() {
        microgramcaster.displayText('Starting to load ' + $('video').attr('src'));
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

    var playVideo = function(url) {
        $('#splash').css('display', 'none');
        $('#video').attr('src',url);
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
        $('#video').attr('src',url);
    };

    this.addToPlaylist = function(url) {
        $('#video').append('<source src="'+url+'" type="video/mp4"/>');
    };

};