package com.you.chat.error

class CompletionException : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super("Failed to process completion", cause)
}
