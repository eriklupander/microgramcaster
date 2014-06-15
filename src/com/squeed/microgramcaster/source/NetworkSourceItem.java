package com.squeed.microgramcaster.source;


public class NetworkSourceItem {

	private String title;
	private String subtitle;
	private Object networkObject;

	private NetworkSourceType type;
	
	public NetworkSourceItem(String title, String subtitle, NetworkSourceType type, Object networkObject) {
		this.title = title;
		this.subtitle = subtitle;
		this.type = type;
		this.networkObject = networkObject;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public Object getNetworkObject() {
		return networkObject;
	}

	public void setNetworkObject(Object networkObject) {
		this.networkObject = networkObject;
	}

	public NetworkSourceType getType() {
		return type;
	}

	public void setType(NetworkSourceType type) {
		this.type = type;
	}

	

	// DOC:DETAILS

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		NetworkSourceItem that = (NetworkSourceItem) o;
		return networkObject.equals(that.networkObject);
	}

	@Override
	public int hashCode() {
		return networkObject.hashCode();
	}

	@Override
	public String toString() {
		String name = getTitle();
		// Display a little star while the device is being loaded (see
		// performance optimization earlier)
		return name;
	}
}
