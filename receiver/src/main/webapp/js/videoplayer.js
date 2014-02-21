$("video").bind("ended", function() {
   microgramcaster.displayText('Playback finished');
});
$("video").bind("play", function() {
	var file = $('video').attr('src');
	var playedObject = document.getElementById('video');
	var curr = playedObject.currentTime;
	var dur = playedObject.duration;
	moment().format('HH:mm:ss')
    microgramcaster.displayText('Playing \'' + file + '\' ' + humanizeDuration(curr) + ' of ' + humanizeDuration(dur));
});

$("video").bind("loadstart", function() {
	microgramcaster.displayText('Starting to load ' + $('video').attr('src'));
});

$("video").bind("progress", function() {
	microgramcaster.displayText('Buffering ' + $('video').attr('src') + "...");
});

function humanizeDuration(input, units ) { 
  // units is a string with possible values of y, M, w, d, h, m, s, ms
  var duration = moment().startOf('day').add('s', input),
    format = "";

  if(duration.hour() > 0){ format += "HH:"; }

  //if(duration.minute() > 0){ format += "mm:"; }

  format += "mm:ss";

  return duration.format(format);
}

function playVideo(url) {
    $('#video').attr('src',url);
}