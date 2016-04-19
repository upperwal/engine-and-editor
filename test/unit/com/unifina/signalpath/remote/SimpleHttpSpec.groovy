package com.unifina.signalpath.remote

import com.unifina.datasource.DataSource
import com.unifina.datasource.DataSourceEventQueue
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import groovy.json.JsonBuilder
import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.concurrent.FutureCallback
import org.apache.http.entity.StringEntity
import org.apache.http.nio.client.HttpAsyncClient
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import spock.lang.Specification

class SimpleHttpSpec extends Specification {
	SimpleHttp module

	/**
	 * Module input values for each iteration, and corresponding expected output values
	 * Maps  input/output name -> List of values
	 */
	Map<String, List> inputs, outputs

	/**
	 * Override "response" to provide the mock server implementation
	 * If closure, will be executed (argument is HttpUriRequest)
	 * If constant, will be returned
	 * If array, elements will be returned in sequence (closures executed, cyclically repeated if too short)
	 * If you want to return an array,
	 *   use closure that returns an array (see default below)
	 *   or array of arrays
	 */
	def response = { request -> [] }

	/**
	 * Init module for test
	 * @param inputs List of names, or number of inputs to be autogenerated (in1, in2, ...)
	 * @param outputs List of names, or number of outputs to be autogenerated (out1, out2, ...)
     */
	private void init(inputs=[], outputs=[], List headers=[], boolean isBlocking=false, List moreParams=[]) {
		if (inputs instanceof Integer) { inputs = (inputs < 1) ? [] : (1..inputs).collect { "in"+it } }
		if (outputs instanceof Integer) { outputs = (outputs < 1) ? [] : (1..outputs).collect { "out"+it } }

		// TestableSimpleHttp is SimpleHttp module wrapped so that we can inject our own mock HttpClient
		// Separate class is needed in same path as SimpleHttp.java; anonymous class won't work with de-serializer
		TestableSimpleHttp.httpClient = mockClient
		module = new TestableSimpleHttp()
		module.init()
		int i = 0, o = 0
		module.configure([
			options: [
				inputCount: [value: inputs.size()],
				outputCount: [value: outputs.size()],
				headerCount: [value: headers.size()],
				blockExecution: [value: isBlocking]
			],
			inputs: inputs.collect {[name: "in"+(++i), displayName: it]},
			outputs: outputs.collect {[name: "out"+(++o), displayName: it]},
			params: [
			    *headers,
				*moreParams
			]
		])
		isAsync = !isBlocking
	}

	private boolean test() {
		return new ModuleTestHelper.Builder(module, inputs, outputs)
			.overrideGlobals { mockGlobals }
			.onModuleInstanceChange { newInstance -> module = newInstance }
			.test()
	}

	/** Mocked event queue. Works manually in tests, please call module.receive(queuedEvent) */
	def mockGlobals = Stub(Globals) {
		getDataSource() >> Stub(DataSource) {
			getEventQueue() >> Stub(DataSourceEventQueue) {
				enqueue(_) >> { feedEvent ->
					transaction = feedEvent.content[0]
				}
			}
		}
	}

	// temporary storage for async transaction generated by AbstractHttpModule, passing from globals to mockClient
	AbstractHttpModule.HttpTransaction transaction
	boolean isAsync = true

	/** HttpClient that generates mock responses to HttpUriRequests according to this.response */
	def mockClient = Stub(HttpAsyncClient) {
		def responseI = [].iterator()
		execute(_, _) >> { HttpUriRequest request, FutureCallback<HttpResponse> future ->
			def mockHttpResponse = Stub(CloseableHttpResponse) {
				getEntity() >> {
					def ret = response
					// array => iterate
					if (ret instanceof Iterable) {
						// end of array -> restart from beginning
						if (!responseI.hasNext()) {
							responseI = response.iterator()
						}
						ret = responseI.hasNext() ? responseI.next() : []
					}
					// closure => execute
					if (ret instanceof Closure) {
						ret = ret(request)
					}
					// wrap mock response object in JSON and HttpEntity
					return new StringEntity(new JsonBuilder(ret).toString())
				}
			}
			// blocking requests sendOutput here already
			future.completed(mockHttpResponse)

			// simulate AbstractHttpModule.receive, but without propagation
			if (isAsync) {
				module.sendOutput(transaction)
			}
		}
	}

	void "no input, no response, async"() {
		init()
		inputs = [trigger: [1, true, "test"]]
		outputs = [errors: [[], [], []]]
		expect:
		test()
	}

	void "no input, no response, blocking"() {
		init([], [], [], true)
		inputs = [trigger: [1, true, "test"]]
		outputs = [errors: [[], [], []]]
		expect:
		test()
	}

	void "no input, unexpected object response (ignored)"() {
		init()
		inputs = [trigger: [1, true, "test"]]
		outputs = [errors: [[], [], []]]
		response = [foo: 3, bar: 2, shutdown: "now"]
		expect:
		test()
	}

	void "no input, object response"() {
		init(0, ["foo"])
		inputs = [trigger: [1, true, "test"]]
		outputs = [errors: [[], [], []], out1: [3, 3, 3]]
		response = [foo: 3]
		expect:
		test()
	}

	void "empty response"() {
		init(1, 1)
		inputs = [in1: [4, 20, "everyday"]]
		outputs = [errors: [[], [], []]]
		response = []
		expect:
		test()
	}

	void "one input, echo response (just value, no JSON object wrapper)"() {
		init(1, ["foo"])
		def messages = ["4", "20", "everyday"]
		inputs = [in1: messages]
		outputs = [errors: [[], [], []], out1: messages]
		response = { HttpPost r ->
			def jsonString = EntityUtils.toString(r.entity)
			def ob = new JSONObject(jsonString)
			return ob.get("in1")
		}
		expect:
		test()
	}

	void "two inputs, three outputs, array constant response with too many elements (ignored)"() {
		init(2, 3)
		inputs = [in1: [4, 20, "everyday"], in2: [1, 2, "ree"]]
		outputs = [errors: [[], [], []], out1: [true, true, true],
							out2 : ["developers", "developers", "developers"], out3: [1, 1, 1]]
		response = { request -> [true, "developers", 1, 2, 3, 4] }
		expect:
		test()
	}

	void "two inputs, three outputs, array varying response with too few elements"() {
		init(2, 3)
		inputs = [in1: [4, 20, "everyday"], in2: [1, 2, "ree"]]
		outputs = [errors: [[], [], []], out1: [":)", ":|", ":("]]
		response = [[":)"], [":|"], [":("]]
		expect:
		test()
	}

	void "two inputs, three outputs, array varying length response"() {
		init(2, 3)
		inputs = [in1: [4, 20, "everyday"], in2: [1, 2, "ree"]]
		outputs = [errors: [[], [], []], out1: [":)", ":|", ":("],
							out2 : [null, 8, 7], out3: [null, null, 6]]
		response = [[":)"], [":|", 8], [":(", 7, 6, 5, 4, 3]]
		expect:
		test()
	}

	void "GET request generates correct URL params"() {
		init(["inputput", "nother"], 0, [], false, [
			[name: "URL", value: "localhost"],
			[name: "verb", value: "GET"]
		])
		inputs = [in1: [666, "666", 2 * 333], in2: [1 + 1 == 2, true, "true"]]
		outputs = [errors: [[], [], []]]
		response = { HttpUriRequest request ->
			assert request.URI.toString().equals("localhost?inputput=666&nother=true")
		}
		expect:
		test()
	}

	void "HTTP request headers are transmitted correctly"() {
		def headers = [
			user  : [name: "header1", displayName: "user", value: "head"],
			token : [name: "header2", displayName: "token", value: "bang"],
			apikey: [name: "header3", displayName: "apikey", value: "er"]
		]
		init(0, 0, headers.values().toList())
		inputs = [trigger: [1, true, "metal", 666]]
		outputs = [:]
		response = { HttpUriRequest request ->
			int found = 0
			request.allHeaders.each { Header h ->
				if (headers.containsKey(h.name)) {
					assert headers[h.name].value == h.value
					found++
				}
			}
			assert found == headers.size()
		}
		expect:
		test()
	}

	void "JSON object dot notation works for output parsing"() {
		init(0, ["seasons", "best.pony", "best.pals.human"])
		inputs = [trigger: [1, true, "test"]]
		outputs = [errors: [[], [], []], out1: [4, 4, 4],
							out2: ["Pink", "Pink", "Pink"], out3: ["Finn", "Finn", "Finn"]]
		response = [best: [pony: "Pink", pals: [dog: "Jake", human: "Finn"]], seasons: 4]
		expect:
		test()
	}

	void "JSON object dot notation supports array indexing too"() {
		init(0, ["seasons[1]", "best.pals.count", "best.pals[1].name"])
		inputs = [trigger: [1, true, "test"]]
		outputs = [errors: [[], [], []], out1: [3, 3, 3],
							out2: [2, 2, 2], out3: ["Finn", "Finn", "Finn"]]
		response = [best: [pals: [[name: "Jake", species: "Dog"], [name: "Finn", species: "Human"]]], seasons: [4,3,2,1]]
		expect:
		test()
	}
}
