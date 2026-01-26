import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

data class Lesson(
    val date: String,
    val name: String,
    val time: String,
    val cabinet: String,
    val teacher: String,
    val teacherGrade: String,
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
            response.body.string()
        } else {
            null
        }
    }
}

fun extractClasses(doc: org.jsoup.nodes.Document): List<Lesson> {
    val resultList = mutableListOf<Lesson>()

    val dayBlocks = doc.select("div.block-index")

    for (dayBlock in dayBlocks) {
        var cabinet = "-"
        var teacher = "-"

        val dateText = dayBlock.selectFirst("h2")?.text()?.slice(0..7) ?: "-"

        val lessonElements = dayBlock.select("div.list-group-item")

        for (element in lessonElements) {
            val name = element.selectFirst("strong")?.text() ?: "-"

            val fullText = element.text()
            val time = fullText.slice(0..11)

            // Формат: ищем текст в скобках (л.), (пр.), (экз.) и т.д.
            // Логика: ищем текст в скобках, который похож на формат занятия
            val formatRegex = Regex("""\((.*?)\)""")
            val format = formatRegex.findAll(fullText)
                .map { it.groupValues[1] } // Берем содержимое скобок
                .firstOrNull { it.contains("л.") || it.contains("пр.") || it.contains("экз") || it.contains("подгруппа") }
                ?: ""

            // cabinet и teacher лежат в тегах <nobr>
            val nobrTags = element.select("nobr")

            for (nobr in nobrTags) {
                if (nobr.text().count { it == '.' } >= 2) teacher = nobr.text()
                if (nobr.text().count { it.isDigit() } >= 1) cabinet = nobr.text()
            }

            val teacherGrade = fullText.substringAfterLast("- ")

            resultList.add(
                Lesson(
                    date = dateText,
                    time = time,
                    name = name,
                    format = format,
                    cabinet = cabinet,
                    teacher = teacher,
                    teacherGrade = teacherGrade
                )
            )
        }
    }
    return resultList
}

fun main(){
    val targetUrl = "https://www.altstu.ru/m/s/7000021385/"
    val html = fetchPage(targetUrl) ?: error("Failed to fetch page")
    val doc = Jsoup.parse(html, targetUrl)

    val lessons = extractClasses(doc)

    if (lessons.isEmpty()) {
        println("Пар не найдено.")
    } else {
        println("Найдено занятий: ${lessons.size}\n")
        var lastDate = ""

        lessons.forEach { lesson ->
            if (lesson.date != lastDate) {
                println("\n========= ${lesson.date} =========")
                lastDate = lesson.date
            }

            println("${lesson.time} | ${lesson.name} (${lesson.format})")
            println("   Аудитория: ${lesson.cabinet}")
            println("   Преподаватель: ${lesson.teacher} ${lesson.teacherGrade}")
        }
    }
}