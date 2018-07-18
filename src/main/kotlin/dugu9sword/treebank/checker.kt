package dugu9sword.treebank

data class Accuracy(val corrNum: Int, val totalNum: Int) {
    operator fun plus(accuracy: Accuracy): Accuracy {
        return Accuracy(corrNum = corrNum + accuracy.corrNum,
                totalNum = totalNum + accuracy.totalNum)
    }
}

private fun isPunctuation(word: String): Boolean {
    return !word.contains(Regex("""[a-zA-Z0-9]"""))
}


fun computeAccuracy(sentence: Sentence, prediction: List<Int>, directional: Boolean = true): Accuracy {
    var corr = 0
    var total = 0
    for (i in 1 until sentence.size) {
        if (isPunctuation(sentence[i].word))
            continue
        total++
        when (directional) {
            true ->
                if (sentence[i].parent == prediction[i])
                    corr++
            false ->
                if (sentence[i].parent == prediction[i] ||
                        prediction[i] in 0 until sentence.size && sentence[prediction[i]].parent == i)
                    corr++
        }
    }
    return Accuracy(corr, total)
}