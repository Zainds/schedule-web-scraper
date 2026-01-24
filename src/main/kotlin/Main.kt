import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class Lesson(
    val date: String,
    val name: String,
    val time: String,
    val cabinet: String,
    val teacher: String,
    val format: String
)

fun fetchPage(url: String): String? {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url(url)
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .build()

    return client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
            response.body?.string()
        } else {
            null
        }
    }
}

fun extractClasses(doc: org.jsoup.nodes.Document): List<Lesson> {

    val elements = doc.select("div.list-group-item")
    return elements.mapNotNull { element ->

        val nameEl = element.selectFirst("strong")
        val dateText = element.text().slice(0..11)
        val timeText = element.selectFirst("strong")
        val cabinetText = element.selectFirst("strong")
        val teacherText = element.selectFirst("strong")
        val formatText = element.text().slice(12..11)
        val nameText = nameEl?.text()?.trim()

        if (!nameText.isNullOrEmpty()) {
            Lesson(
                date = dateText,
                name = nameText,
                time = nameText,
                cabinet = nameText,
                teacher = nameText,
                format = nameText
                )
        } else {
            null
        }
    }
}

fun parseHtml(html: String, baseUrl: String): Document {
    return Jsoup.parse(html, baseUrl)
}

fun main(){
    val targetUrl = "https://www.altstu.ru/m/s/7000021385/"
    val html = fetchPage(targetUrl) ?: error("Failed to fetch page")
    val doc = Jsoup.parse(html, targetUrl)

    val lessons = extractClasses(doc)

    lessons.forEach { lesson ->
        println("${lesson.date} ${lesson.name}")
    }
    println("\nTotal lessons scraped: ${lessons.size}")

}