package dugu9sword

const val isLeftBranch = true
const val isDirected = false
const val useWSJ10 = true


fun main(args: Array<String>) {
    val treeBank = TreeBank("dataset/wsj_test.txt", 1, 3, 6, 7)
    var totalAccuracy = Fraction(0.0f,0.0f)
    val sentences = if (useWSJ10)
        treeBank.sentences.filter { it.size <= 11 }
    else
        treeBank.sentences
    for (sentence in sentences) {
        val prediction = if (isLeftBranch)
            listOf(-1) + (2 until sentence.size).toList() + 0
        else
            listOf(-1) + 0 + (1 until sentence.size - 1).toList()
        val accuracy = computeAccuracy(sentence, prediction, isDirected)
        totalAccuracy+=accuracy
    }
    println("Accuracy: $totalAccuracy")
}