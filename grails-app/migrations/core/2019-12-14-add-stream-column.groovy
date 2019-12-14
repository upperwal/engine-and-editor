package core

databaseChangeLog = {
	changeSet(author: "upperwal", id: "update-table") {
		addColumn(tableName: "stream") {
			column(name: "channel", type: "varchar(1024)") {
				constraints(nullable: "false")
			}
		}
	}
}