package com.unifina.signalpath.pravah;

import com.streamr.client.utils.StreamPartition;
import com.unifina.domain.data.Stream;
import com.unifina.signalpath.ConfigurableModule;
import com.unifina.signalpath.StreamParameter;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class PravahStreamModule extends ConfigurableModule {

	StreamParameter streamParameter = new StreamParameter(this, "stream");
	StringInput geospaceInput = new StringInput(this, "geospace");
	StringOutput topicOutput = new StringOutput(this, "topic");
	StringOutput rawOutput = new StringOutput(this, "raw");

	@Override
	public void init() {
		super.init();

		addInput(streamParameter);
		addInput(geospaceInput);
		addOutput(topicOutput);
		addOutput(rawOutput);
	}

	@Override
	public Collection<StreamPartition> getStreamPartitions() {
		Collection<StreamPartition> p = new ArrayList<>();

		String topic = new String(
				streamParameter.getValue().getChannel() +
						geospaceInput.getValue() == "null" ? "" : geospaceInput.getValue());
		p.add(new StreamPartition(topic, 0));

		return p;
	}

	@Override
	public Stream getStream() {
		return streamParameter.getValue();
	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}
}
