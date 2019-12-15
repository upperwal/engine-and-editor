package core
databaseChangeLog = {
	changeSet(author: "upperwal", id: "add-stream-modules-5") {
		insert(tableName: "module") {
			column(name: "id", valueNumeric: 50003)
			column(name: "version", valueNumeric: 0)
			column(name: "category_id", valueNumeric: 53)
			column(name: "implementing_class", value: "com.unifina.signalpath.json.JSONFilter")
			column(name: "name", value: "JSON Filter")
			column(name: "js_module", value: "GenericModule")
			column(name: "type", value: "module")
			column(name: "module_package_id", valueNumeric: 1)
			column(name: "json_help", value: '{"params":{"json":"JSON string","query":"query string"},"paramNames":["json", "query"],"outputs":{"result":"filtered json"},"outputNames":["result"],"helpText":"<p>Filter a json.</p>"}')
		}
	}
}