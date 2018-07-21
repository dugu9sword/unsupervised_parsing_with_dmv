package dugu9sword

import java.io.BufferedWriter
import java.io.FileWriter

class Logger(filePath: String) {

    val file = BufferedWriter(FileWriter(filePath))

    fun log(string: String) {
        return
        file.write(string)
//        print(string)
    }
}