package com.unifina.signalpath;

import com.streamr.client.utils.StreamPartition;
import com.unifina.domain.data.Stream;

import java.util.Collection;

public abstract class ConfigurableModule extends AbstractSignalPathModule {
	abstract public Stream getStream();
	abstract public Collection<StreamPartition> getStreamPartitions();
}
