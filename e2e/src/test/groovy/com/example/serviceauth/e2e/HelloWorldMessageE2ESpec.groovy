package com.example.serviceauth.e2e

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class HelloWorldMessageE2ESpec extends Specification {

    @Shared
    HttpClient client = HttpClient.newHttpClient()

    @Shared
    String baseUrl = System.getProperty("app.base-url", "http://localhost:8080")

    @Shared
    JsonSlurper json = new JsonSlurper()

    def "POST /api/hello-world-messages creates a message and returns 201"() {
        given:
        def request = HttpRequest.newBuilder()
            .uri(URI.create("${baseUrl}/api/hello-world-messages"))
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
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        then:
        response.statusCode() == 404
    }
}
