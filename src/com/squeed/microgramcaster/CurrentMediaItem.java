package com.squeed.microgramcaster;

import com.squeed.microgramcaster.channel.Command;

/**
 * Combines MediaItem information with an Adapter index. Bit of a kludge...
 * 
 * @author Erik
 *
 */
public class CurrentMediaItem {

	private String name;
	private Integer position;
	private Command playCommand;
	private Long duration;
	
	public CurrentMediaItem(String name, Long duration, int position, Command cmd) {
		this.name = name;
		this.duration = duration;
		this.position = position;
		playCommand = cmd;
		
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getPosition() {
		return position;
	}
	public void setPosition(Integer position) {
		this.position = position;
	}
	public Command getPlayCommand() {
		return playCommand;
	}
	public void setPlayCommand(Command playCommand) {
		this.playCommand = playCommand;
	}
	public Long getDuration() {
		return duration;
	}
	public void setDuration(Long duration) {
		this.duration = duration;
	}
	
	
}
