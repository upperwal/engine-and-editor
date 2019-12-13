package core
databaseChangeLog = {
	changeSet(author: "upperwal", id: "add-stream-modules-4") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 50001)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 53)
			column(name: "implementing_class", value: "com.unifina.signalpath.pravah.PravahStreamModule")
			column(name: "name", value: "CreatePravahStream")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"channel":"the channel from which data is subscribed"},"paramNames":["channel"],"outputs":{"topic":"name of the topic","raw":"raw data stream"},"outputNames":["topic","raw"],"helpText":"<p>Create a new stream.</p>"}')
		}
	}

	changeSet(author: "upperwal", id: "tour-resources-6") {

		// Grant public read permission to demo streams
		insert(tableName: "permission") {
			column(name: "version", valueNumeric: 0)
			column(name: "clazz", value: "com.unifina.domain.data.Stream")
			column(name: "operation", value: "read")
			column(name: "string_id", value: "/AirQuality/in/ncr")
			column(name: "anonymous", valueBoolean: true)
		}
	}
}
