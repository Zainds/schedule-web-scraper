import kotlinx.serialization.Serializable

@Serializable
data class ApiGroup(val id: String, val value: String)

@Serializable
data class GroupEntity(val id: String, val name: String)

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
