package com.example.serviceauth.e2e

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.UUID
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import spock.lang.Shared
import spock.lang.Specification

class DevUserRegistrationE2ESpec extends Specification {

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
                e2e_admin_password_hash: new BCryptPasswordEncoder().encode("e2e-admin-password"),
                e2e_reader_password_hash: new BCryptPasswordEncoder().encode("e2e-reader-password"),
            ])
            .load()
            .migrate()
    }

    def "POST /api/dev/users registers a reader user that can read but not write"() {
        given:
        def username = "e2e-dev-reader-${UUID.randomUUID()}"
        def password = "reader-password"
        def registerResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/dev/users"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([
                    username: username,
                    password: password,
                    permissions: ["hello_world_message:read"],
                ])))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        when:
        def registerBody = json.parseText(registerResponse.body())
        def getResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages"))
                .header("Authorization", basicAuth(username, password))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        def postResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages"))
                .header("Authorization", basicAuth(username, password))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([message: "should fail"])))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        then:
        registerResponse.statusCode() == 201
        registerResponse.headers().firstValue("Location").orElse("").startsWith("/api/dev/users/")
        registerBody.username == username
        registerBody.permissions as Set == ["hello_world_message:read"] as Set
        getResponse.statusCode() == 200
        postResponse.statusCode() == 403
    }

    def "POST /api/dev/users registers a writer user that can create messages"() {
        given:
        def username = "e2e-dev-writer-${UUID.randomUUID()}"
        def password = "writer-password"
        def registerResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/dev/users"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([
                    username: username,
                    password: password,
                    permissions: ["hello_world_message:read", "hello_world_message:write"],
                ])))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        when:
        def createResponse = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/hello-world-messages"))
                .header("Authorization", basicAuth(username, password))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([message: "created by dev user"])))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        def createBody = json.parseText(createResponse.body())

        then:
        registerResponse.statusCode() == 201
        createResponse.statusCode() == 201
        createBody.message == "created by dev user"
    }

    def "POST /api/dev/users returns 400 for invalid payload"() {
        when:
        def response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/dev/users"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonOutput.toJson([
                    username: " ",
                    password: "",
                    permissions: [],
                ])))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        then:
        response.statusCode() == 400
    }

    private static String basicAuth(String username, String password) {
        String token = Base64.getEncoder()
            .encodeToString("${username}:${password}".getBytes(StandardCharsets.UTF_8))
        return "Basic ${token}"
    }
}
