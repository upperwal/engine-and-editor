package com.unifina.feed;

import com.google.protobuf.Any;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.streamr.client.exceptions.EncryptedContentNotParsableException;
import com.streamr.client.protocol.message_layer.MessageRef;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.protocol.message_layer.StreamMessageV28;
import com.streamr.client.utils.StreamPartition;
import com.unifina.utils.Globals;
import in.soket.Data;
import in.soket.StreamCatcher;
import io.pravah.Decoder;
import org.apache.log4j.Logger;
import com.google.protobuf.Message;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class RealtimePravahMessageSource extends StreamPravahMessageSource {

	private static final Logger log = Logger.getLogger(RealtimePravahMessageSource.class);

	private List geoArray;
	private String channel;


	public RealtimePravahMessageSource(Globals globals, StreamMessageConsumer consumer, Collection<StreamPartition> streamPartitions) {
		super(globals, consumer, streamPartitions);

		for(StreamPartition sp : streamPartitions) {
			String streamId = sp.getStreamId();
			int idx = streamId.indexOf("/", 1);
			channel = streamId.substring(0, idx);
			String geo = streamId.substring(idx);

			geoArray = new ArrayList<String>();
			geoArray.add(geo);

			pravahRPC.subscribe(channel, geoArray, new StreamCatcherLogic(consumer));
		}


	}

	@Override
	public void close() {
		pravahRPC.unsubscribe(channel, geoArray);
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
				JsonFormat f = new JsonFormat();
				Message protom = Decoder.decode("/AirQuality", data.getRaw());
				String rawJSON = f.printToString(protom);
				m.put("raw", rawJSON);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}

			// Hack
			m.put("start", "");
			m.put("groupKey", "");

			log.info("StreamCatcherLogic: Got message");
			log.info("Message got: " + data.getTopic());

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

			log.info("After Msg formed");

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
