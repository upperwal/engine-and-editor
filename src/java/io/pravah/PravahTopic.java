package io.pravah;

import java.util.ArrayList;
import java.util.List;

public class PravahTopic {
	private String channel;
	private List<String> geospaces;

	public PravahTopic() {
		geospaces = new ArrayList<>();
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public List<String> getGeospaces() {
		return geospaces;
	}

	public void addGeospace(String g) {
		this.geospaces.add(g);
	}

	public static PravahTopic getChannelAndTopic(String topic) {
		PravahTopic t = new PravahTopic();

		int idx = topic.indexOf("/", 1);
		t.setChannel(topic.substring(0, idx));
		t.addGeospace(topic.substring(idx));

		return t;
	}
}
