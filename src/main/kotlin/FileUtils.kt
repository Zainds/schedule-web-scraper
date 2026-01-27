import kotlinx.serialization.json.Json
import java.io.File

inline fun <reified T>  writeDataJson(data: List<T>, fileName: String) {
    val jsonFormat = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    try {
        val jsonString = jsonFormat.encodeToString(data)

        val file = File(fileName)
        file.writeText(jsonString)

        println("\nУспешно записано в файл: ${file.absolutePath}")
    }catch (e:Exception){
        println("\nОшибка записи файла $fileName: ${e.message}")
    }
}