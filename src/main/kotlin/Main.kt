import kotlinx.serialization.json.Json
import java.io.File

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

    val lessons = parseSchedule(html, url)

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

    val allGroups: List<GroupEntity>

    if (groupsFile.exists()) {
        println("Загрузка локальной базы групп...")
        val jsonParser = Json { ignoreUnknownKeys = true }
        allGroups = try {
            jsonParser.decodeFromString(groupsFile.readText())
        } catch (e: Exception) {
            println("Ошибка чтения файла: ${e.message}. Пересоздаю базу...")
            scanAllGroups()
        }
    } else {
        allGroups = scanAllGroups()
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