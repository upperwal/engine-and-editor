package com.unifina.signalpath.kafka;

import grails.converters.JSON;

import java.security.AccessControlException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.service.KafkaService;
import com.unifina.service.UnifinaSecurityService;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.MapInput;
import com.unifina.signalpath.NotificationMessage;
import com.unifina.signalpath.Parameter;
import com.unifina.signalpath.StreamParameter;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.TimeSeriesInput;

public class SendToStream extends AbstractSignalPathModule {

	protected StreamParameter streamParameter = new StreamParameter(this,"stream");
	protected JSONObject streamConfig = null;
	
	protected KafkaService kafkaService = null;
	protected UnifinaSecurityService unifinaSecurityService = null;
	
	protected boolean historicalWarningShown = false;
	private Stream authenticatedStream = null;
	
	@Override
	public void init() {
		kafkaService = (KafkaService) globals.getGrailsApplication().getMainContext().getBean("kafkaService");
		unifinaSecurityService = (UnifinaSecurityService) globals.getGrailsApplication().getMainContext().getBean("unifinaSecurityService");
		
		addInput(streamParameter);
		

		streamParameter.setUpdateOnChange(true);
		
		// TODO: don't rely on static ids
		Feed feedFilter = new Feed();
		feedFilter.setId(7L);
		streamParameter.setFeedFilter(feedFilter);
	}

	@Override
	public void sendOutput() {
		if (globals.isRealtime()) {
			Map msg = new LinkedHashMap<>();
			for (Input i : drivingInputs) {
				msg.put(i.getName(), i.getValue());
			}
			kafkaService.sendMessage(authenticatedStream, "", msg);
		}
		else if (!historicalWarningShown && globals.getUiChannel()!=null) {
			globals.getUiChannel().push(new NotificationMessage(this.getName()+": Not sending to Stream '"+streamParameter.getValue()+"' in historical playback mode."), parentSignalPath.getUiChannelId());
			historicalWarningShown = true;
		}
	}

	@Override
	public void clearState() {

	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		Stream stream = streamParameter.getValue();
		
		if (stream==null)
			return;
		
		// Check access to this Stream
		if (unifinaSecurityService.canAccess(stream))
			authenticatedStream = stream;
		else throw new AccessControlException(this.getName()+": Access denied to Stream "+stream.getName());
		
		// TODO: don't rely on static ids
		if (stream.getFeed().getId()!=7) {
			throw new IllegalArgumentException("Can not send to this feed type!");
		}
		
		if (stream.getStreamConfig()==null)
			throw new IllegalStateException("Stream "+stream.getName()+" is not properly configured!");
		
		streamConfig = (JSONObject) JSON.parse(stream.getStreamConfig());

		JSONArray fields = streamConfig.getJSONArray("fields");
		
		for (Object o : fields) {
			JSONObject j = (JSONObject) o;
			String type = j.getString("type");
			String name = j.getString("name");
			
			// TODO: add other types
			if (type.equalsIgnoreCase("number") || type.equalsIgnoreCase("boolean")) {
				TimeSeriesInput input = new TimeSeriesInput(this,name);
				input.canHaveInitialValue = false;
				addInput(input);
			}
			else if (type.equalsIgnoreCase("string")) {
				StringInput input = new StringInput(this, name);
				addInput(input);
			}
			else if (type.equalsIgnoreCase("map")) {
				addInput(new MapInput(this,name));
			}
			else if (type.equalsIgnoreCase("list")) {
				addInput(new ListInput(this,name));
			}
		}
		
		for (Input input : getInputs()) {
			if (!(input instanceof Parameter)) {
				input.canToggleDrivingInput = false;
				input.canBeFeedback = false;
				input.requiresConnection = false;
			}
		}
		
		if (streamConfig.containsKey("name"))
			this.setName(streamConfig.get("name").toString());
	}
	
}