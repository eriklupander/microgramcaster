package com.squeed.microgramcaster;

public class PlayerCmdFactory {

	public static final String CMD_PLAY_URL = "PLAY_URL";
	public static final String CMD_PLAY = "PLAY";
	public static final String CMD_PAUSE = "PAUSE";
	
	public static final String PARAM_URL = "url";

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
	
}
