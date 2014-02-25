package com.squeed.microgramcaster;

/**
 * Factory for building commands to send to the player, includes constant values for defined
 * commands and parameters.
 */
public class PlayerCmdFactory {

	public static final String CMD_PLAY_URL = "PLAY_URL";
	public static final String CMD_PLAY = "PLAY";
	public static final String CMD_PAUSE = "PAUSE";
	public static final String CMD_REQUEST_POSITION = "GET_CURRENT_POSITION";
	public static final String CMD_SEEK_POSITION = "SEEK_POSITION";
	
	public static final String PARAM_URL = "url";
	public static final String PARAM_POSITION_SECONDS = "positionSeconds";

	public static PlayerCmd buildPlayUrlCommand(String mediaItemURL) {
		PlayerCmd cmd = new PlayerCmd(CMD_PLAY_URL);
		cmd.addParameter(PARAM_URL, mediaItemURL);
		return cmd;
	}
	
	public static PlayerCmd buildPlayCommand() {
		PlayerCmd cmd = new PlayerCmd(CMD_PLAY);
		return cmd;
	}
	
	public static PlayerCmd buildPauseCommand() {
		PlayerCmd cmd = new PlayerCmd(CMD_PAUSE);
		return cmd;
	}
	
	public static PlayerCmd buildRequestPositionCommand() {
		PlayerCmd cmd = new PlayerCmd(CMD_REQUEST_POSITION);
		return cmd;
	}
	
	public static PlayerCmd buildSeekPositionCommand(int positionSeconds) {
		PlayerCmd cmd = new PlayerCmd(CMD_SEEK_POSITION);
		cmd.addParameter(PARAM_POSITION_SECONDS, ""+positionSeconds);
		return cmd;
	}
	
}
