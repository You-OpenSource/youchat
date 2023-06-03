package io.github.you_opensource.chat

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.you_opensource.chat.data.CompletionResponse
import io.github.you_opensource.chat.data.InputData
import io.github.you_opensource.chat.error.CompletionException
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.internal.sse.RealEventSource
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import java.io.IOException
import java.util.*
import java.util.regex.Pattern


object YouChatClient {
    @Throws(CompletionException::class)
    fun complete(data: InputData): CompletionResponse {
        val client = OkHttpClient()
        val objectMapper = ObjectMapper()
        val request: Request = getRequest(data)
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

    @Throws(CompletionException::class)
    fun completeAsyncMessages(data: InputData): Flowable<String> {
        val objectMapper = ObjectMapper()
        val client = OkHttpClient()
        val request: Request = getRequest(data)
        return try {
            Flowable.create({ emitter ->
                val realEventSource = RealEventSource(request, object : EventSourceListener() {
                    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                        if (data.contains("youChatToken")) {
                            val value = objectMapper.readValue(data, MutableMap::class.java)
                            emitter.onNext(value["youChatToken"].toString())
                        }
                    }

                    override fun onClosed(eventSource: EventSource) {
                        eventSource.cancel()
                        client.dispatcher.executorService.shutdown();
                        emitter.onComplete()
                    }

                    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                        emitter.onError(t!!)
                        emitter.onComplete()
                    }
                })
                realEventSource.connect(client)
            }, BackpressureStrategy.BUFFER)
        } catch (e: IOException) {
            throw CompletionException(e)
        }
    }


    private fun getRequest(data: InputData): Request {
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
        return request
    }
}
