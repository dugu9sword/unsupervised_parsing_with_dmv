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
               val pWord: Int = 1,
               val pTag: Int = 4,
               val pParent: Int = 6,
               val pArc: Int = 7) {
    val sentences = ArrayList<Sentence>()
    val wordDict = HashMap<String, Int>()
    val tagDict = HashMap<String, Int>()
    val arcDict = HashMap<String, Int>()

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

        for (dict in listOf(wordDict, tagDict, arcDict)) {
            dict[Special.ROOT] = 0
            dict[Special.NONE] = 1
            dict[Special.UNK] = 2
            dict[Special.DIGIT] = 3
        }
        for (sentence in sentences)
            for (element in sentence) {
                wordDict.putIfAbsent(element.word, wordDict.size)
                tagDict.putIfAbsent(element.tag, tagDict.size)
                arcDict.putIfAbsent(element.arc, arcDict.size)
            }
    }

    private fun buildSentence(lines: ArrayList<String>): Sentence {
        val sentence = Sentence()
        for (line in lines) {
            val split = line.split("\t")
            val word =
                    if (Regex(""".*\d+.*""").matches(split[pWord]))
                        Special.DIGIT
                    else
                        split[pWord].toLowerCase()
            sentence.add(WordElement(word, split[pTag], split[pParent].toInt(), split[pArc]))
        }
        return sentence
    }

}

fun main(args: Array<String>) {
    val dev = TreeBank("dataset/dev.gold.conll")
    println(dev.sentences[0].map { it.tag })
}