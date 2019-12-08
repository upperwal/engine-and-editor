package com.unifina.datasource;

import com.streamr.client.utils.StreamPartition;
import com.unifina.feed.RealtimePravahMessageSource;
import com.unifina.feed.StreamMessageSource;
import com.unifina.utils.Globals;

import java.util.Collection;

public class RealtimePravahDataSource extends RealtimeDataSource {
	public RealtimePravahDataSource(Globals globals) {
		super(globals);
	}

	@Override
	protected StreamMessageSource createStreamMessageSource(Collection<StreamPartition> streamPartitions, StreamMessageSource.StreamMessageConsumer consumer) {
		return new RealtimePravahMessageSource(globals, consumer, streamPartitions);
	}
}
