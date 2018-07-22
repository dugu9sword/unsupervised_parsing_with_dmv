package dugu9sword

import java.io.BufferedWriter
import java.io.FileWriter

enum class Color {
    WHITE, YELLOW, GREEN, BLUE, RED
}

enum class Mode {
    CONSOLE, FILE, BOTH
}

class Debugger(filePath: String) {

    val file = BufferedWriter(FileWriter(filePath))

    fun log(any: Any,
            color: Color = Color.WHITE,
            mode: Mode = Mode.CONSOLE,
            title: String = "") {
        val string = if (title != "")
            "+++ $title +++" + "\n" + any.toString() + "\n" + "--- $title ---"
        else
            any.toString()
        if (mode == Mode.CONSOLE || mode == Mode.BOTH) {
            when (color) {
                Color.WHITE -> println(string)
                Color.RED -> println(red(string))
                Color.GREEN -> println(green(string))
                Color.YELLOW -> println(yellow(string))
                Color.BLUE -> println(blue(string))
            }
        }
        if (mode == Mode.FILE || mode == Mode.BOTH) {
            file.write("$string\n")
            file.flush()
        }
    }
}