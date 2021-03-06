package dugu9sword

import kotlin.system.exitProcess

const val ANSI_RESET = "\u001B[0m"
const val ANSI_BLACK = "\u001B[30m"
const val ANSI_RED = "\u001B[31m"
const val ANSI_GREEN = "\u001B[32m"
const val ANSI_YELLOW = "\u001B[33m"
const val ANSI_BLUE = "\u001B[34m"
const val ANSI_PURPLE = "\u001B[35m"
const val ANSI_CYAN = "\u001B[36m"
const val ANSI_WHITE = "\u001B[37m"

fun red(any: Any) = ANSI_RED + any.toString() + ANSI_RESET
fun green(any: Any) = ANSI_GREEN + any.toString() + ANSI_RESET
fun yellow(any: Any) = ANSI_YELLOW + any.toString() + ANSI_RESET
fun blue(any: Any) = ANSI_BLUE + any.toString() + ANSI_RESET

fun exit(): Unit = exitProcess(0)