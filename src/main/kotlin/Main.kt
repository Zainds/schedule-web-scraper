import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.File

@Serializable
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
        var teacherGrade = ""

        val dateText = dayBlock.selectFirst("h2")?.text()?.slice(0..7) ?: "-"

        val lessonElements = dayBlock.select("div.list-group-item")

        for (element in lessonElements) {
            val name = element.selectFirst("strong")?.text() ?: "-"

            val fullText = element.text()
            val time = fullText.slice(0..10)

            // Формат: ищем текст в скобках (л.), (пр.), (экз.) и т.д.
            // Логика: ищем текст в скобках, который похож на формат занятия
            val formatRegex = Regex("""\((.*?)\)""")
            val format = formatRegex.findAll(fullText)
                .map { it.groupValues[1] } // Берем содержимое скобок
                .firstOrNull { it.contains("л.") || it.contains("пр.") || it.contains("экз") || it.contains("подгруппа") || it.contains("ф.") }
                ?: ""

            // cabinet и teacher лежат в тегах <nobr>
            val nobrTags = element.select("nobr")

            for (nobr in nobrTags) {
                if (nobr.text().count { it == '.' } >= 2) teacher = nobr.text()
                if (nobr.text().count { it.isDigit() } >= 1) cabinet = nobr.text()
            }

            if (teacher != "-") teacherGrade = fullText.substringAfterLast("- ")

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

fun writeLessonsJson(lessons: List<Lesson>){
    val jsonFormat = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val jsonString = jsonFormat.encodeToString(lessons)

    val file = File("schedule.json")
    file.writeText(jsonString)

    println("\nУспешно записано в файл: ${file.absolutePath}")
}

fun extractGroupClasses(id: String) {
    val url = "https://www.altstu.ru/m/s/${id}/"

    println("Загружаю данные с $url ...")
    val html = fetchPage(url) ?: error("Failed to fetch page")
    val doc = Jsoup.parse(html, url)

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

        writeLessonsJson(lessons)
    }
}

fun main(){
    val groupsFile = File("all_groups_full.json")

    if (!groupsFile.exists()) {
        println("Файл 'all_groups_full.json' не найден!")
        println("Сначала запустите GroupGrabber.kt, чтобы загрузить базу групп.")
        return
    }

    println("Загрузка базы групп...")
    val jsonParser = Json { ignoreUnknownKeys = true }
    val allGroups: List<GroupEntity> = try {
        jsonParser.decodeFromString(groupsFile.readText())
    } catch (e: Exception) {
        println("Ошибка чтения JSON: ${e.message}")
        return
    }
    println("База загружена. Всего групп: ${allGroups.size}")

    while (true) {
        print("\nВведите название группы (или 'exit'): ")
        val input = readlnOrNull()?.trim() ?: break

        if (input.lowercase() == "exit") break
        if (input.length < 2) {
            println("Введите хотя бы 2 символа.")
            continue
        }

        val foundGroups = allGroups.filter {
            it.name.contains(input, ignoreCase = true)
        }

        if (foundGroups.isEmpty()) {
            println("Группы не найдены.")
        } else {
            foundGroups.forEachIndexed { index, group ->
                println("${index + 1}. ${group.name}")
            }

            print("Выберите номер (1-${foundGroups.size}): ")
            val choiceStr = readlnOrNull()
            val choice = choiceStr?.toIntOrNull()

            if (choice != null && choice in 1..foundGroups.size) {
                val selectedGroup = foundGroups[choice - 1]
                println("\nВыбрана группа: ${selectedGroup.name}")

                extractGroupClasses(selectedGroup.id)
            } else {
                println("Неверный выбор.")
            }
        }
    }
}