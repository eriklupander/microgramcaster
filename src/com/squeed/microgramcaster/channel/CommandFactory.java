package com.squeed.microgramcaster.channel;


/**
 * Factory for building commands to send to the player, includes constant values for defined
 * commands and parameters.
 */
public class CommandFactory {

	public static Command buildPlayUrlCommand(String mediaItemURL) {
		Command cmd = new Command(CommandDef.CMD_PLAY_URL.name());
		cmd.addParameter(ChannelDef.PARAM_URL, mediaItemURL);
		return cmd;
	}
	
	public static Command buildPlayCommand() {
		Command cmd = new Command(CommandDef.CMD_PLAY.name());
		return cmd;
	}
	
	public static Command buildPauseCommand() {
		Command cmd = new Command(CommandDef.CMD_PAUSE.name());
		return cmd;
	}
	
	public static Command buildRequestPositionCommand() {
		Command cmd = new Command(CommandDef.CMD_REQUEST_POSITION.name());
		return cmd;
	}
	
	public static Command buildSeekPositionCommand(int positionSeconds) {
		Command cmd = new Command(CommandDef.CMD_SEEK_POSITION.name());
		cmd.addParameter(ChannelDef.PARAM_POSITION_SECONDS, ""+positionSeconds);
		return cmd;
	}
	
}
