import java.io.File

fun extractGroupClasses(id: String) {
    val url = "https://www.altstu.ru/m/s/${id}/"

    println("Загружаю данные с $url ...")
    val html = fetchPage(url) ?: error("Failed to fetch page")
    val lessons = parseSchedule(html, url)

    lessons.print()
    if ( lessons.isNotEmpty() ) {
        writeDataJson(lessons, "schedule.json")
    }
}

fun main(){
    val groupsFile = File("all_groups_full.json")

    val allGroups: List<GroupEntity>

    if (groupsFile.exists()) {
        println("Загрузка локальной базы групп...")
        allGroups = try {
            jsonFormat.decodeFromString(groupsFile.readText())
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