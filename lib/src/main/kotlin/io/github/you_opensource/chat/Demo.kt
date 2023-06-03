package io.github.you_opensource.chat

import io.github.you_opensource.chat.data.CompletionResponse
import io.github.you_opensource.chat.data.simpleInput
import io.github.you_opensource.chat.error.CompletionException

internal object Demo {
    @JvmStatic
    fun main(args: Array<String>) {
        val inputData = simpleInput("Given the next line of code, give me a fix: \n```\nprit('hello world')\n```")
        val completeResponse: CompletionResponse
        try {
            // to request solution sync
            completeResponse = YouChatClient.complete(
                inputData
            )
            println("Chat response sync:")
            println(completeResponse!!.formattedText)
        } catch (e: CompletionException) {
            println("Failed to autocomplete: " + e.message)
            e.cause!!.printStackTrace()
        }
        println("Chat response async:")
        YouChatClient.completeAsyncMessages(inputData)
            .subscribe({ print(it) }, {})
    }
}
