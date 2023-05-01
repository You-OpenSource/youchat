package com.you.chat

import com.you.chat.data.CompletionResponse
import com.you.chat.data.InputData
import com.you.chat.error.CompletionException

internal object Demo {
    @JvmStatic
    fun main(args: Array<String>) {
        val prompt = "Given the next line of code, give me a fix: \n```\nprit('hello world')\n```"
        val page = 1
        val count = 10
        val safe_search = "Moderate"
        val on_shopping_page = false
        val mkt = "en-US"
        val response_filter = "WebPages,Translations,TimeZone,Computation,RelatedSearches"
        val domain = "youchat"
        val query_trace_id: String? = null
        val chat: String? = null
        val debug = false
        val detailed = false
        var completeResponse: CompletionResponse? = null
        try {
            completeResponse = YouChatClient.complete(
                InputData(
                    prompt,
                    page,
                    count,
                    safe_search,
                    on_shopping_page,
                    mkt,
                    response_filter,
                    domain,
                    query_trace_id,
                    chat,
                    debug,
                    detailed
                )
            )
        } catch (e: CompletionException) {
            println("Failed to autocomplete: " + e.message)
            e.cause!!.printStackTrace()
        }
        println("Chat response:")
        println(completeResponse!!.formattedText)
    }
}
