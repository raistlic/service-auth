package com.example.serviceauth.e2e

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.Base64
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class HelloWorldMessageE2ESpec extends Specification {

    private static final String ADMIN_USERNAME = "e2e-admin"
    private static final String ADMIN_PASSWORD = "e2e-admin-password"
    private static final String READER_USERNAME = "e2e-reader"
    private static final String READER_PASSWORD = "e2e-reader-password"

    @Shared
    HttpClient client = HttpClient.newHttpClient()

    @Shared
    String baseUrl = System.getProperty("app.base-url", "http://localhost:8080")

    @Shared
    JsonSlurper json = new JsonSlurper()

    def setupSpec() {
        Flyway.configure()
            .dataSource(
                System.getProperty("db.url", "jdbc:postgresql://localhost:5432/serviceauth"),
                System.getProperty("db.user", "serviceauth"),
                System.getProperty("db.password", "serviceauth")
            )
            .locations("classpath:db/e2e")
            .table("flyway_schema_history_e2e")
            .baselineOnMigrate(true)
            .baselineVersion(MigrationVersion.fromVersion("0"))
            .placeholders([
                e2e_admin_password_hash: new BCryptPasswordEncoder().encode(ADMIN_PASSWORD),
                e2e_reader_password_hash: new BCryptPasswordEncoder().encode(READER_PASSWORD),
            ])
            .load()
            .migrate()
    }

    def "GET /api/hello-world-messages returns 401 without credentials"() {
        when:
        def response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages"))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        then:
        response.statusCode() == 401
    }

    def "POST /api/hello-world-messages creates a message and returns 201"() {
        given:
        def request = HttpRequest.newBuilder()
            .uri(URI.create("${baseUrl}/api/hello-world-messages"))
            .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([message: "hello e2e"])))
            .build()

        when:
        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        def body = json.parseText(response.body())

        then:
        response.statusCode() == 201
        body.id != null
        body.message == "hello e2e"
    }

    def "GET /api/hello-world-messages returns all messages including created ones"() {
        given: "a message has been created"
        def createResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages"))
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([message: "list me"])))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        def createdId = json.parseText(createResponse.body()).id

        when:
        def response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages"))
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        def body = json.parseText(response.body())

        then:
        response.statusCode() == 200
        body.any { it.id == createdId }
    }

    def "GET /api/hello-world-messages/{id} returns the message"() {
        given: "a message has been created"
        def createResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages"))
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([message: "get me"])))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        def createdId = json.parseText(createResponse.body()).id

        when:
        def response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages/${createdId}"))
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        def body = json.parseText(response.body())

        then:
        response.statusCode() == 200
        body.id == createdId
        body.message == "get me"
    }

    def "PUT /api/hello-world-messages/{id} updates the message and returns 200"() {
        given: "a message has been created"
        def createResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages"))
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([message: "original"])))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        def createdId = json.parseText(createResponse.body()).id

        when:
        def response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages/${createdId}"))
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([message: "updated"])))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        def body = json.parseText(response.body())

        then:
        response.statusCode() == 200
        body.message == "updated"
    }

    def "DELETE /api/hello-world-messages/{id} deletes the message and returns 204"() {
        given: "a message has been created"
        def createResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages"))
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([message: "delete me"])))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        def createdId = json.parseText(createResponse.body()).id

        when:
        def deleteResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages/${createdId}"))
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .DELETE()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        then:
        deleteResponse.statusCode() == 204

        when: "the deleted resource is fetched"
        def getResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages/${createdId}"))
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        then: "it is gone"
        getResponse.statusCode() == 404
    }

    def "GET /api/hello-world-messages/{id} returns 404 for a missing id"() {
        when:
        def response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages/nonexistent-id-that-does-not-exist"))
                .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        then:
        response.statusCode() == 404
    }

    def "POST /api/hello-world-messages returns 403 for a user without write permission"() {
        given:
        def request = HttpRequest.newBuilder()
            .uri(URI.create("${baseUrl}/api/hello-world-messages"))
            .header("Authorization", basicAuth(READER_USERNAME, READER_PASSWORD))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([message: "forbidden"])))
            .build()

        when:
        def response = client.send(request, HttpResponse.BodyHandlers.ofString())

        then:
        response.statusCode() == 403
    }

    private static String basicAuth(String username, String password) {
        String token = Base64.getEncoder()
            .encodeToString("${username}:${password}".getBytes(StandardCharsets.UTF_8))
        return "Basic ${token}"
    }
}
