package dugu9sword

import java.awt.peer.WindowPeer
import java.io.BufferedReader
import java.io.FileReader

data class WordElement(val word: String,
                       val tag: String,
                       val head: Int,
                       val arc: String) {
    override fun toString(): String = "($word,$tag,$head,$arc)"
}

class Sentence : ArrayList<WordElement>() {
    init {
        add(WordElement(Special.ROOT, Special.ROOT, -1, Special.ROOT))
    }
}

object Special {
    const val ROOT = "*ROOT"
    const val UNK = "*UNK"
    const val NONE = "*NON"
    const val DIGIT = "*DIG"
}


class TreeBank(path: String,
               private val pWord: Int = 1,
               private val pTag: Int = 2,
               private val pParent: Int = 3,
               private val pArc: Int = 4) {
    val sentences = ArrayList<Sentence>()
    val wordDict = HashMap<String, Int>()
    val tagDict = HashMap<String, Int>()
    val arcDict = HashMap<String, Int>()

    init {
        val reader = BufferedReader(FileReader(path))
        val lines = ArrayList<String>()
        for (line: String in reader.lines()) {
            if (line == "") {
                if (lines.size > 1)
                    sentences.add(buildSentence(lines))
                lines.clear()
            } else
                lines.add(line)
        }
//        sentences.forEach { x -> println(x) }

        wordDict[Special.ROOT] = 0
        wordDict[Special.NONE] = 1
        wordDict[Special.UNK] = 2
        wordDict[Special.DIGIT] = 3
        tagDict[Special.ROOT] = 0
        tagDict[Special.UNK] = 1
        arcDict[Special.ROOT] = 0

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
            if (split[pParent] == "-")
                sentence.add(WordElement(word, split[pTag], -1, "-"))
            else
                sentence.add(WordElement(word, split[pTag], split[pParent].toInt(), split[pArc]))
        }
        return sentence
    }

}

private fun isPunctuation(word: String): Boolean {
    return !word.contains(Regex("""[a-zA-Z0-9]"""))
}

const val eps = 1e-100

data class Fraction(val numerator: Float = 0.0f, val denominator: Float = 0.0f) {
    operator fun plus(fraction: Fraction): Fraction {
        return Fraction(numerator = numerator + fraction.numerator,
                denominator = denominator + fraction.denominator)
    }

    fun eval(): Float {
        return numerator / (denominator + 1e-12f)
    }

    override fun toString(): String {
        return "$numerator/$denominator=${numerator / (denominator + eps)}"
    }
}

fun computeAccuracy(sentence: Sentence, prediction: List<Int>, isDirected: Boolean = true): Fraction {
    var corr = 0.0f
    var total = 0.0f

    for (i in 1 until sentence.size) {
        if (isPunctuation(sentence[i].word))
            continue
        total++
        when (isDirected) {
            true ->
                if (sentence[i].head == prediction[i])
                    corr++
            false ->
                if (sentence[i].head == prediction[i] ||
                        prediction[i] in 0 until sentence.size && sentence[prediction[i]].head == i) {
                    corr++
                }
        }
    }
    return Fraction(corr, total)
}

fun main(args: Array<String>) {
    val dev = TreeBank("dataset/dev.gold.conll")
    println(dev.sentences[0].map { it.tag })
}