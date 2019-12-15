grails.server.port.http = '8081'
grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.tomcat.nio = true

grails.project.target.level = 1.8
grails.project.source.level = 1.8
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.war.file = "target/ROOT.war" // "target/${appName}-${appVersion}.war"

grails.project.fork = [
	run: [
		maxMemory: System.getProperty("maxMemory") ? Integer.parseInt(System.getProperty("maxMemory")) : 4196,
		minMemory: 256,
		debug: false,
		maxPerm: 512,
		forkReserve:false
	],
	test: [
		maxMemory: System.getProperty("maxMemory") ? Integer.parseInt(System.getProperty("maxMemory")) : 4196,
		minMemory: 256,
		debug: false,
		maxPerm: 512,
		forkReserve:false,
		daemon:true,
		jvmArgs: [
			"-Djava.awt.headless=true"
		]
	]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {

	// inherit Grails' default dependencies
	inherits("global") {
		// specify dependency exclusions here; for example, uncomment this to disable ehcache:
		// excludes 'ehcache'
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	checksums true // Whether to verify checksums on resolve
	legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

	repositories {
		// Fast local repos first
		grailsHome()
		mavenLocal()

		// Maven central
		mavenRepo "http://repo1.maven.org/maven2/"

		// Ethereum Repository
		mavenRepo "https://dl.bintray.com/ethereum/maven/"

		// Remote Grails repos
		grailsPlugins()
		grailsCentral()

		// New Grails repo
		mavenRepo "https://repo.grails.org/grails/plugins"
	}

	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
		// https://stackoverflow.com/questions/8751508/grails-buildconfig-groovy-difference-between-build-compile-and-runtime

		compile('log4j:log4j:1.2.16')
		compile('com.udojava:EvalEx:1.6')
		compile('com.mashape.unirest:unirest-java:1.4.9')
		compile('org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.1')
		compile('org.antlr:ST4:4.0.8')
		compile('org.postgresql:postgresql:9.4.1208.jre7')
		compile('biz.paluch.redis:lettuce:3.5.0.Final') {
			excludes('com.google.guava:guava:*')
		}
		compile('com.datastax.cassandra:cassandra-driver-core:3.7.1') {
			excludes('com.google.guava:guava:*')
		}
		compile('com.google.code.findbugs:jsr305:3.0.2')
		compile('org.jetbrains:annotations:17.0.0')
		compile('org.ethereum:ethereumj-core:1.12.0-RELEASE') {
			excludes('ch.qos.logback:logback-classic:*')
			excludes('org.springframework:spring-core:*')
			excludes('org.springframework:spring-context:*')
			excludes('org.springframework:spring-orm:*')
		}

		compile('org.web3j:core:4.4.1')
		compile('com.amazonaws:aws-java-sdk:1.11.294')
		compile('org.imgscalr:imgscalr-lib:4.2')
		compile('commons-io:commons-io:2.4')
		compile('org.glassfish.jersey.core:jersey-client:2.27')
		compile('org.glassfish.jersey.inject:jersey-hk2:2.27')
		compile('org.glassfish.jersey.media:jersey-media-json-jackson:2.27')
		compile('com.fasterxml.jackson.core:jackson-databind:2.9.6')
		compile('com.fasterxml.jackson.core:jackson-annotations:2.9.6')
		compile('com.streamr:client:1.2.2')

		compile('in.soket:mesh:1.0')
		compile('com.google.guava:guava:20.0')
		compile('com.googlecode.protobuf-java-format:protobuf-java-format:1.4')
		compile('com.jayway.jsonpath:json-path:2.4.0')

		compile('com.google.code.gson:gson:2.8.5')
		runtime('mysql:mysql-connector-java:5.1.20')
		runtime('commons-net:commons-net:3.3')
		runtime('org.apache.commons:commons-math3:3.2')
		runtime('commons-codec:commons-codec:1.6')
		runtime('com.opencsv:opencsv:3.3')
		runtime('de.ruedigermoeller:fst:2.56')
		runtime('joda-time:joda-time:2.9.3')

		test('cglib:cglib:3.2.6')
	}

	plugins {
		build(":tomcat:7.0.70") { // or ":tomcat:8.0.22"
			export = false
		}

		compile(":mail:1.0.7")
		compile(":cache-headers:1.1.7")

		runtime(':hibernate:3.6.10.19') // or :hibernate4:4.3.10
		runtime(":cors:1.1.8") {
			excludes('spring-security-core')
			excludes('spring-security-web')
		}
		runtime(':database-migration:1.4.0')
		runtime(":spring-security-core:2.0.0")

		test(":plastic-criteria:1.6.7")
		test(":rest-client-builder:2.1.1")
	}
}
