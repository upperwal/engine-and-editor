package com.unifina.feed;

import com.streamr.client.exceptions.EncryptedContentNotParsableException;
import com.streamr.client.protocol.message_layer.MessageRef;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.utils.StreamPartition;
import com.unifina.utils.Globals;
import in.soket.Data;
import in.soket.StreamCatcher;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class RealtimePravahMessageSource extends StreamPravahMessageSource {

	private static final Logger log = Logger.getLogger(RealtimePravahMessageSource.class);

	public RealtimePravahMessageSource(Globals globals, StreamMessageConsumer consumer, Collection<StreamPartition> streamPartitions) {
		super(globals, consumer, streamPartitions);

		List geo = new ArrayList<String>();
		geo.add("/in/ncr");

		pravahRPC.subscribe("/AirQuality", geo, new StreamCatcherLogic(consumer));
	}

	@Override
	public void close() {

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

			log.debug("StreamCatcherLogic: Got message");
			log.debug("Message got: " + data.getTopic());

			this.streamMessageConsumer.accept(new StreamMessage(
					111,
					StreamMessage.ContentType.GROUP_KEY_RESPONSE_SIMPLE,
					StreamMessage.EncryptionType.NONE,
					m
			) {

				@Override
				public String getStreamId() {
					return "";
				}

				@Override
				public int getStreamPartition() {
					return 0;
				}

				@Override
				public long getTimestamp() {
					return 0;
				}

				@Override
				public long getSequenceNumber() {
					return 0;
				}

				@Override
				public String getPublisherId() {
					return null;
				}

				@Override
				public String getMsgChainId() {
					return null;
				}

				@Override
				public MessageRef getPreviousMessageRef() {
					return null;
				}

				@Override
				public SignatureType getSignatureType() {
					return null;
				}

				@Override
				public String getSignature() {
					return null;
				}

				@Override
				public void setSignatureType(SignatureType signatureType) {

				}

				@Override
				public void setSignature(String s) {

				}
			});
		}

		@Override
		public void onError(Throwable throwable) {

		}

		@Override
		public void onCompleted() {

		}
	}
}
