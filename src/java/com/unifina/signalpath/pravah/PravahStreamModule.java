package com.unifina.signalpath.pravah;

import com.streamr.client.utils.StreamPartition;
import com.unifina.domain.data.Stream;
import com.unifina.signalpath.ConfigurableModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class PravahStreamModule extends ConfigurableModule {

	StringInput channelName = new StringInput(this, "channel");
	StringOutput topicOutput = new StringOutput(this, "topic");
	StringOutput rawOutput = new StringOutput(this, "raw");

	@Override
	public void init() {
		super.init();

		addInput(channelName);
		addOutput(topicOutput);
		addOutput(rawOutput);
	}

	@Override
	public Collection<StreamPartition> getStreamPartitions() {
		Collection<StreamPartition> p = new ArrayList<>();
		p.add(new StreamPartition(channelName.getValue(), 0));

		return p;
	}

	@Override
	public Stream getStream() {
		Stream s = new Stream();
		s.setId(channelName.getValue());
		return s;
	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}
}
