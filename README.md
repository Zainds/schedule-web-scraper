# 🎓 AltSTU Schedule Parser

A high-performance console application developed to parse schedule data from the AltSTU website. Designed as a core prototype for a student schedule aggregator, this tool prioritizes speed and efficient data handling.

Built on a modern stack including **Kotlin**, **OkHttp** for networking, **Jsoup** for HTML parsing.

## How to Run

**Prerequisites:**
* **JDK 25 or higher**
* IntelliJ IDEA (Recommended)

**Installation and Execution:**
1.  Clone the repository.
2.  Let Gradle sync the dependencies.
3.  Navigate to `src/main/kotlin/Main.kt` and run the `main` function.

**First Launch Note:**
When you run the application for the first time, it will automatically detect that the student group json is missing. The crawler will start the scanning process, which takes about 15-20 seconds. Once completed, student groups are cached, and subsequent searches will be instantaneous.

## Example Data Output

The application exports the parsed schedule for a selected group into a `schedule.json` file with the following structure:

```json
[
  {
    "date": "27.01.26",
    "name": "Software Architecture",
    "time": "08:15-09:50",
    "cabinet": "403 Main",
    "teacher": "Dr. Smith J.",
    "teacherGrade": "Professor",
    "format": "lecture"
  }
]