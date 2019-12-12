package com.unifina.feed;

import com.streamr.client.exceptions.EncryptedContentNotParsableException;
import com.streamr.client.protocol.message_layer.MessageRef;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.protocol.message_layer.StreamMessageV28;
import com.streamr.client.utils.StreamPartition;
import com.unifina.utils.Globals;
import in.soket.Data;
import in.soket.StreamCatcher;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class RealtimePravahMessageSource extends StreamPravahMessageSource {

	private static final Logger log = Logger.getLogger(RealtimePravahMessageSource.class);

	private List geo;
	private final String channel = "/AirQuality";


	public RealtimePravahMessageSource(Globals globals, StreamMessageConsumer consumer, Collection<StreamPartition> streamPartitions) {
		super(globals, consumer, streamPartitions);

		geo = new ArrayList<String>();
		geo.add("/in/ncr");

		pravahRPC.subscribe(channel, geo, new StreamCatcherLogic(consumer));
	}

	@Override
	public void close() {
		pravahRPC.unsubscribe(channel, geo);
	}

	private static class StreamCatcherLogic implements StreamCatcher {

		StreamMessageConsumer streamMessageConsumer;

		public StreamCatcherLogic(StreamMessageConsumer c) {
			this.streamMessageConsumer = c;
		}

		@Override
		public void onNext(Data data) {
			Map m = new LinkedHashMap();
			m.put("topic", data.getTopic());
			try {
				m.put("raw", data.getRaw().toString("utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			// Hack
			m.put("start", "");
			m.put("groupKey", "");

			log.debug("StreamCatcherLogic: Got message");
			log.debug("Message got: " + data.getTopic());

			StreamMessageV28 msg = new StreamMessageV28(
					data.getTopic(),
					0,
					System.currentTimeMillis(),
					100000,
					0L,
					0L,
					StreamMessage.ContentType.GROUP_KEY_RESET_SIMPLE,
					m
			);

			log.debug("After Msg formed");

			this.streamMessageConsumer.accept(msg);
		}

		@Override
		public void onError(Throwable throwable) {

		}

		@Override
		public void onCompleted() {

		}
	}
}
