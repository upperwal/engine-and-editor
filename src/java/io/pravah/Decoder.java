package io.pravah;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.pravah.air.quality.AirQuality;

public class Decoder {
	static public Message decode(String channel, ByteString data) throws InvalidProtocolBufferException {
		switch(channel) {
			case ChannelConst.AIR_QUALITY:
				return AirQuality.FeedMessage.parseFrom(data);
			default:
				return null;
		}
	}
}
