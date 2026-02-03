import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

val allGroups = ConcurrentHashMap.newKeySet<GroupEntity>()

// Пример сложной группы - "5ГМУ(с)-21"
val fullCharset = (0..9).map { it.toString() } +
        ('А'..'Я').map { it.toString() } +
        listOf("-", "(", ")", ".")

fun scanAllGroups(): List<GroupEntity> = runBlocking {
    println("Запуск парсинга групп...")
    val startTime = System.currentTimeMillis()

    supervisorScope {
        fullCharset.map { char ->
            async(Dispatchers.IO) {
                scanPrefixRecursive(char)
            }
        }.awaitAll()
    }

    println("Найдено групп: ${allGroups.size}")
    println("Время выполнения: ${(System.currentTimeMillis() - startTime) / 1000} сек.")

    val resultList = allGroups.sortedBy { it.name }
    writeDataJson(resultList, "all_groups.json")

    return@runBlocking resultList
}

private suspend fun scanPrefixRecursive(prefix: String) {

    if (prefix.length > 12) return

    val url = "https://www.altstu.ru/m/s/ajax/?query=$prefix"

    try {
        val responseBody = withContext(Dispatchers.IO) {
            fetchPage(
                url = url,
                headers = mapOf(
                    "X-Requested-With" to "XMLHttpRequest",
                    "Referer" to "https://www.altstu.ru/m/s/",
                    "Accept" to "application/json, text/javascript, */*; q=0.01",
                    "Accept-Language" to "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
            )
        } ?: return

        if (responseBody.length < 5) return

        val groups = try {
            jsonFormat.decodeFromString<List<ApiGroup>>(responseBody)
        } catch (e: Exception) { emptyList() }

        groups.forEach { g ->
            allGroups.add(GroupEntity(g.id, g.value))
        }

        // Если вернулось 8 групп, значит есть еще
        if (groups.size >= 8) {
            coroutineScope {
                fullCharset.map { char ->
                    launch(Dispatchers.IO) {
                        scanPrefixRecursive(prefix + char)
                    }
                }
            }
        }
    } catch (e: Exception) {

    }
}