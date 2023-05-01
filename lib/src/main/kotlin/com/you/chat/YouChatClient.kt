package com.you.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.you.chat.data.CompletionResponse
import com.you.chat.data.InputData
import com.you.chat.error.CompletionException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object YouChatClient {
    @Throws(CompletionException::class)
    fun complete(data: InputData): CompletionResponse {
        val client = OkHttpClient()
        val objectMapper = ObjectMapper()
        val urlBuilder: HttpUrl.Builder = "https://you.com/api/streamingSearch".toHttpUrlOrNull()!!.newBuilder()
        urlBuilder.addQueryParameter("q", data.prompt)
        urlBuilder.addQueryParameter("page", data.page.toString())
        urlBuilder.addQueryParameter("count", data.count.toString())
        urlBuilder.addQueryParameter("safeSearch", data.safeSearch)
        urlBuilder.addQueryParameter("onShoppingPage", data.isOnShoppingPage.toString())
        urlBuilder.addQueryParameter("mkt", data.mkt)
        urlBuilder.addQueryParameter("responseFilter", data.responseFilter)
        urlBuilder.addQueryParameter("domain", data.domain)
        urlBuilder.addQueryParameter(
            "queryTraceId",
            if (data.queryTraceId == null) UUID.randomUUID().toString() else data.queryTraceId
        )
        val request: Request = Request.Builder()
            .url(urlBuilder.build())
            .addHeader("authority", "you.com")
            .addHeader("accept", "text/event-stream")
            .addHeader("accept-language", "en,fr-FR;q=0.9,fr;q=0.8,es-ES;q=0.7,es;q=0.6,en-US;q=0.5,am;q=0.4,de;q=0.3")
            .addHeader("cache-control", "no-cache")
            .addHeader("referer", "https://you.com/search?q=who+are+you&tbm=youchat")
            .addHeader(
                "sec-ch-ua",
                "\"Not_A Brand - youide - reloadium official\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\""
            )
            .addHeader("sec-ch-ua-mobile", "?0")
            .addHeader("sec-ch-ua-platform", "\"Windows\"")
            .addHeader("sec-fetch-dest", "empty")
            .addHeader("sec-fetch-mode", "cors")
            .addHeader("sec-fetch-site", "same-origin")
            .addHeader("cookie", "safesearch_guest=Moderate; uuid_guest=" + UUID.randomUUID().toString())
            .addHeader("user-agent", "youide")
            .build()
        return try {
            val response: Response = client.newCall(request).execute()
            val responseText: String = response.body!!.string()
            if (data.isDebug) {
                println("\n\n------------------\n\n")
                println(responseText)
                println("\n\n------------------\n\n")
            }
            if (!responseText.contains("youChatToken")) {
                throw CompletionException("Response didn't contain any completion results: $responseText")
            }
            val youChatSerpResultsPattern =
                Pattern.compile("(?<=event: youChatSerpResults\\ndata:)(.*\\n)*?(?=event: )")
            val youChatSerpResultsMatcher = youChatSerpResultsPattern.matcher(responseText)
            youChatSerpResultsMatcher.find()
            val youChatSerpResults = youChatSerpResultsMatcher.group()
            val thirdPartySearchResultsPattern =
                Pattern.compile("(?<=event: thirdPartySearchResults\\ndata:)(.*\\n)*?(?=event: )")
            val thirdPartySearchResultsMatcher = thirdPartySearchResultsPattern.matcher(responseText)
            thirdPartySearchResultsMatcher.find()
            val thirdPartySearchResults = thirdPartySearchResultsMatcher.group()
            val matches: MutableList<String> = ArrayList()
            val pattern = Pattern.compile("\\{\"youChatToken\": \"(.*?)\"\\}")
            val matcher = pattern.matcher(responseText)
            while (matcher.find()) {
                matches.add(matcher.group(1))
            }
            val result = java.lang.String.join("", matches)
            val extra: MutableMap<String, Any> = HashMap()
            extra["youChatSerpResults"] = objectMapper.readValue(youChatSerpResults, MutableMap::class.java)
            val formattedText = result.replace("\\n", "\n").replace("\\\\", "\\").replace("\\\"", "\"")
            CompletionResponse(
                formattedText,
                extra,
                null
            )
        } catch (e: IOException) {
            throw CompletionException(e)
        }
    }
}
