package io.github.you_opensource.chat.data

class InputData(
    var prompt: String, var page: Int, var count: Int, var safeSearch: String, var isOnShoppingPage: Boolean,
    var mkt: String, var responseFilter: String, var domain: String, var queryTraceId: String?,
    var chat: String?, var isDebug: Boolean, var isDetailed: Boolean
)


fun simpleInput(prompt: String): InputData {
    return InputData(
        prompt,
        1,
        10,
        "Moderate",
        false,
        "en-US",
        "WebPages,Translations,TimeZone,Computation,RelatedSearches",
        "youchat",
        null,
        null,
        false,
        false
    )
}
