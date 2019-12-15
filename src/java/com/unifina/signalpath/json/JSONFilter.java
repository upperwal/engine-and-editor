package com.unifina.signalpath.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.StringParameter;

public class JSONFilter extends AbstractSignalPathModule {

	StringParameter jsonParam = new StringParameter(this, "json", "{}");
	StringInput query = new StringInput(this, "query");

	StringOutput filteredOutput = new StringOutput(this, "result");

	@Override
	public void init() {
		super.init();

		jsonParam.setDrivingInput(true);
		jsonParam.setCanToggleDrivingInput(false);

		addInput(jsonParam);
		addInput(query);

		addOutput(filteredOutput);
	}

	@Override
	public void sendOutput() {
		Configuration conf = Configuration.defaultConfiguration();
		Object res = JsonPath.parse(jsonParam.getValue()).read(query.getValue());
		filteredOutput.send(res.toString());
	}

	@Override
	public void clearState() {

	}
}
