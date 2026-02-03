import kotlinx.serialization.json.Json
import java.io.File

val jsonFormat = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

inline fun <reified T>  writeDataJson(data: List<T>, fileName: String) {
    try {
        val jsonString = jsonFormat.encodeToString(data)

        val file = File(fileName)
        file.writeText(jsonString)

        println("\nУспешно записано в файл: ${file.absolutePath}")
    }catch (e:Exception){
        println("\nОшибка записи файла $fileName: ${e.message}")
    }
}

fun List<Lesson>.print(){
    if (this.isEmpty()) {
        println("Пар не найдено.")
    } else {
        println("Найдено занятий: ${this.size}\n")
        var lastDate = ""

        this.forEach { lesson ->
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