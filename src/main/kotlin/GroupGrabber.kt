import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.File
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap


val allGroups = Collections.newSetFromMap(ConcurrentHashMap<GroupEntity, Boolean>())
val jsonParser = Json { ignoreUnknownKeys = true }

// ПОЛНЫЙ набор символов
// Пример сложной группы - "5ГМУ(с)-21"
val fullCharset = (0..9).map { it.toString() } +
        ('А'..'Я').map { it.toString() } +
        listOf("-", "(", ")", ".")

fun scanAllGroups(): List<GroupEntity> = runBlocking {
    println("Запуск парсинга групп...")
    val startTime = System.currentTimeMillis()

    supervisorScope {
        // Стартуем со всех возможных символов
        fullCharset.map { char ->
            async(Dispatchers.IO) {
                scanPrefixRecursive(char)
            }
        }.awaitAll()
    }

    println("Найдено групп: ${allGroups.size}")
    println("Время выполнения: ${(System.currentTimeMillis() - startTime) / 1000} сек.")

    val resultList = allGroups.sortedBy { it.name }
    val jsonString = Json { prettyPrint = true }.encodeToString(resultList)
    File("all_groups_full.json").writeText(jsonString)

    return@runBlocking resultList
}

private suspend fun scanPrefixRecursive(prefix: String) {

    if (prefix.length > 12) return

    val url = "https://www.altstu.ru/m/s/ajax/?query=$prefix"

    val request = Request.Builder()
        .url(url)
        .header("User-Agent", "Mozilla/5.0")
        .header("X-Requested-With", "XMLHttpRequest")
        .build()

    try {
        val responseBody = withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) null else response.body.string()
            }
        } ?: return

        if (responseBody.length < 5) return

        val groups = try {
            jsonParser.decodeFromString<List<ApiGroup>>(responseBody)
        } catch (e: Exception) { emptyList() }

        groups.forEach { g ->
            allGroups.add(GroupEntity(g.id, g.value))
        }

        // Если вернулось 8 групп (максимум), то мы нашли не всё
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