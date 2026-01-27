import okhttp3.OkHttpClient
import okhttp3.Request

object NetworkUtils {

    val client = OkHttpClient.Builder().build()

    fun fetchPage(url: String): String? {

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build()

        return client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body.string()
            } else {
                null
            }
        }
    }
}