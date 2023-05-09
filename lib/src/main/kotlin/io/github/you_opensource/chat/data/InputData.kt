package io.github.you_opensource.chat.data

class InputData(
    var prompt: String, var page: Int, var count: Int, var safeSearch: String, var isOnShoppingPage: Boolean,
    var mkt: String, var responseFilter: String, var domain: String, var queryTraceId: String?,
    var chat: String?, var isDebug: Boolean, var isDetailed: Boolean
)
