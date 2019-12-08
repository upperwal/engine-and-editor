package com.unifina.feed;

import com.streamr.client.utils.StreamPartition;
import com.unifina.utils.Globals;
import in.soket.MeshRPC;

import java.io.Closeable;
import java.util.Collection;

public abstract class StreamPravahMessageSource implements Closeable {

	protected MeshRPC pravahRPC;

	/**
	 * Creates an instance of this StreamMessageSource. The constructor should not block.
	 * Messages can be reported to the consumer as soon as they are available.
	 *
	 * @param globals
	 * @param consumer
	 * @param streamPartitions The set of StreamPartitions to subscribe to.
	 */
	public StreamPravahMessageSource(Globals globals, StreamMessageSource.StreamMessageConsumer consumer, Collection<StreamPartition> streamPartitions) {
		//super(globals, consumer, streamPartitions);

		pravahRPC = new MeshRPC("rpc.pravah.io", 5555);
	}

	public abstract void close();
}
