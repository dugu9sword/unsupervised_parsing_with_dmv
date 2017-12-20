package dugu9sword.treebank

import java.io.BufferedReader
import java.io.FileReader

data class WordElement(val word: String,
                       val tag: String,
                       val parent: Int,
                       val arc: String) {
    override fun toString(): String = "($word,$tag,$parent,$arc)"
}

class Sentence : ArrayList<WordElement>() {
    init {
        add(WordElement(Special.ROOT, Special.ROOT, -1, Special.ROOT))
    }
}

object Special {
    val ROOT = "*ROOT"
    val UNK = "*UNK"
    val NONE = "*NON"
    val DIGIT = "*DIG"
}


class TreeBank(path: String,
               val p_word: Int = 1,
               val p_tag: Int = 4,
               val p_parent: Int = 6,
               val p_arc: Int = 7) {
    val sentences = ArrayList<Sentence>()
    val word_dict = HashMap<String, Int>()
    val tag_dict = HashMap<String, Int>()
    val arc_dict = HashMap<String, Int>()

    init {
        val reader = BufferedReader(FileReader(path))
        val lines = ArrayList<String>()
        for (line: String in reader.lines()) {
            if (line == "") {
                sentences.add(buildSentence(lines))
                lines.clear()
            } else
                lines.add(line)
        }
//        sentences.forEach { x -> println(x) }

        for (dict in listOf(word_dict, tag_dict, arc_dict)) {
            dict.put(Special.ROOT, 0)
            dict.put(Special.NONE, 1)
            dict.put(Special.UNK, 2)
            dict.put(Special.DIGIT, 3)
        }
        for (sentence in sentences)
            for (element in sentence) {
                word_dict.putIfAbsent(element.word, word_dict.size)
                tag_dict.putIfAbsent(element.tag, tag_dict.size)
                arc_dict.putIfAbsent(element.arc, arc_dict.size)
            }
    }

    private fun buildSentence(lines: ArrayList<String>): Sentence {
        val sentence = Sentence()
        for (line in lines) {
            val split = line.split("\t")
            val word =
                    if (Regex(""".*\d+.*""").matches(split[p_word]))
                        Special.DIGIT
                    else
                        split[p_word].toLowerCase()
            sentence.add(WordElement(word, split[p_tag], split[p_parent].toInt(), split[p_arc]))
        }
        return sentence
    }

}

fun main(args: Array<String>) {
    val dev = TreeBank("dataset/dev.gold.conll")
    println(dev.sentences[0].map { it.tag })
}