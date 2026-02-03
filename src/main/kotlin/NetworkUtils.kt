import okhttp3.OkHttpClient
import okhttp3.Request

val client = OkHttpClient.Builder().build()

val userAgents = listOf(
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15",
    "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/119.0",
    "Mozilla/5.0 (Linux; Android 13; SM-S911U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36",
    "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36>"
)

private fun randomUserAgent(): String = userAgents.random()

fun fetchPage(url: String, headers: Map<String, String> = emptyMap() ): String? {
    val builder = Request.Builder()
        .url(url)
        .header("User-Agent", randomUserAgent())

    headers.forEach { (key, value) ->
        builder.header(key, value)
    }

    val request = builder.build()

    return try {
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body.string()
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}
