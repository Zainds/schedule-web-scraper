import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun parseSchedule(html: String, baseUrl: String): List<Lesson> {
    val doc = Jsoup.parse(html, baseUrl)
    return extractClasses(doc)
}

private fun extractClasses(doc: Document): List<Lesson> {
    val resultList = mutableListOf<Lesson>()
    val dayBlocks = doc.select("div.block-index")

    for (dayBlock in dayBlocks) {
        var cabinet = "-"
        var teacher = "-"
        var teacherGrade = ""

        val dateText = dayBlock.selectFirst("h2")?.text()?.slice(0..7) ?: "-"
        val lessonElements = dayBlock.select("div.list-group-item")

        for (element in lessonElements) {
            val name = element.selectFirst("strong")?.text() ?: "-"
            val fullText = element.text()
            val time = if (fullText.length >= 11) fullText.slice(0..10) else "??:??"

            val formatRegex = Regex("""\((.*?)\)""")
            val format = formatRegex.findAll(fullText)
                .map { it.groupValues[1] }
                .firstOrNull {
                    it.contains("л.") || it.contains("пр.") ||
                            it.contains("экз") || it.contains("подгруппа") ||
                            it.contains("ф.") || it.contains("зач.")
                } ?: ""

            val nobrTags = element.select("nobr")
            for (nobr in nobrTags) {
                if (nobr.text().count { it == '.' } >= 2) teacher = nobr.text()
                if (nobr.text().count { it.isDigit() } >= 1) cabinet = nobr.text()
            }

            if (teacher != "-") teacherGrade = fullText.substringAfterLast("- ")

            resultList.add(
                Lesson(dateText, name, time, cabinet, teacher, teacherGrade, format)
            )
        }
    }
    return resultList
}
