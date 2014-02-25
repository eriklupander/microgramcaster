var microgramcaster = new function() {

	cast.receiver.logger.setLevelValue(0);
	window.castReceiverManager = cast.receiver.CastReceiverManager.getInstance();
	console.log('Starting Receiver Manager');

	// handler for the 'ready' event
	castReceiverManager.onReady = function (event) {
		console.log('Received Ready event: ' + JSON.stringify(event.data));
		window.castReceiverManager.setApplicationState("Application status is ready...");
	};

	// handler for 'senderconnected' event
	castReceiverManager.onSenderConnected = function (event) {
		console.log('Received Sender Connected event: ' + event.data);
		console.log(window.castReceiverManager.getSender(event.data).userAgent);
	};

	// handler for 'senderdisconnected' event
	castReceiverManager.onSenderDisconnected = function (event) {
		console.log('Received Sender Disconnected event: ' + event.data);
		if (window.castReceiverManager.getSenders().length == 0) {
			window.close();
		}
	};

	// handler for 'systemvolumechanged' event
	castReceiverManager.onSystemVolumeChanged = function (event) {
		console.log('Received System Volume Changed event: ' + event.data['level'] + ' ' +
				event.data['muted']);
	};

	// create a CastMessageBus to handle messages for a custom namespace
	window.messageBus =
			window.castReceiverManager.getCastMessageBus(
					'urn:x-cast:com.squeed.microgramcaster');

	// handler for the CastMessageBus message event
	window.messageBus.onMessage = function (event) {
		console.log('Message [' + event.senderId + ']: ' + event.data);
		// display the message from the sender
        //microgramcaster.displayText(event.data);
        var command = cmd.parse(event.data);
        switch(command.id) {
            case "PLAY_URL":
                videoplayer.playUrl(command.params.url);
                break;
            case "PLAY":
                videoplayer.play();
                break;
            case "PAUSE":
                videoplayer.pause();
                break;
            case "ADD_TO_PLAYLIST":
                videoplayer.addToPlaylist(command.params.url);
                break;
			case "SEEK_POSITION":
				videoplayer.seek(command.params.positionSeconds);
				break;
			case "GET_CURRENT_POSITION":
				var positionSeconds = videoplayer.getCurrentPosition();
				var rsp = {
					"currentPosition": positionSeconds;
				};
				window.messageBus.send(event.senderId, JSON.stringify(rsp));
				break;
            default:
                microgramcaster.displayText("Unknown or unparsable command: " + event.data);
                break;
        }
        //playVideo(event.data);

        // Echo everything back to all attached senders
		window.messageBus.send(event.senderId, event.data);
	}

	// initialize the CastReceiverManager with an application status message
	window.castReceiverManager.start({statusText: "Application is starting"});
	console.log('Receiver Manager started');
   

    // utility function to display the text message in the input field
    this.displayText = function(text) {
        console.log(text);
        $('#message').clearQueue();
        $('#message').html(text);
		$('#message').css('opacity','1.0');
		$('#message').animate({'opacity':0.0}, 4000);
		
        window.castReceiverManager.setApplicationState(text);
    };
	
};