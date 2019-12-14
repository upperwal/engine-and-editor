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
			column(name: "json_help", value: '{"params":{"stream":"the stream to get data from","geospace":"geo location of the data"},"paramNames":["stream", "geospace"],"outputs":{"topic":"name of the topic","raw":"raw data stream"},"outputNames":["topic","raw"],"helpText":"<p>Create a new stream.</p>"}')
		}
	}
}
