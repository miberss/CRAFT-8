package me.mibers.Extra

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun readPastebinContent(pastebinUrl: String): String {
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(pastebinUrl))
        .GET()
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun textPastebin() {
    val pastebinUrl = ""
    val content = readPastebinContent(pastebinUrl)
    println(content)
}