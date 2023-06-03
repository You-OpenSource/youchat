# YouChat java sdk

YouChat java sdk allows you to integrate your applications with YouChat



## How to:

Add dependency to your project

For maven: 
```
<dependency>
  <groupId>io.github.you-opensource</groupId>
  <artifactId>youchat</artifactId>
  <version>0.0.3</version>
</dependency>
```
For Gradle:
```
testImplementation 'io.github.you-opensource:youchat:0.0.3'
```

Use it to integrate with your Java/Kotlin code:

```kotlin
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
```

### Async:
```kotlin
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

YouChatClient.completeAsyncMessages(
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
).subscribe({print(it)}, { print("Error occurred : $it")})

```