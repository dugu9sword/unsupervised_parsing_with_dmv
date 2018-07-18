package dugu9sword

import dugu9sword.treebank.TreeBank
import dugu9sword.treebank.computeAccuracy

val leftBranch = false
val directional = false
val wsj10 = true


fun main(args: Array<String>) {
    val treebank = TreeBank("dataset/wsj_test.txt",1,3,6,7)
    var corr = 0
    var total = 0
    val sentences = if (wsj10)
        treebank.sentences.filter { it.size <= 11 }
    else
        treebank.sentences
    for (sentence in sentences) {
        val prediction = if (leftBranch)
            listOf(-1) + (2 until sentence.size).toList() + 0
        else
            listOf(-1) + 0 + (1 until sentence.size - 1).toList()
        val accuracy = computeAccuracy(sentence, prediction, directional)
        corr += accuracy.corrNum
        total += accuracy.totalNum
    }
    println("Accuracy: ${corr.toFloat() / total}")
}