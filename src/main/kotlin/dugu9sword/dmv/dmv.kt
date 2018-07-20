package dugu9sword.dmv

import dugu9sword.*
import org.nd4j.linalg.factory.Nd4j as nj


fun main(args: Array<String>) {
    var params = Params()
    val trainSentences = trainTreeBank.sentences.filter { it.size < 10 }
    println("Size: ${trainSentences.size}")
    for (epochId in 0 until 1) {
        val time_start = System.currentTimeMillis()

        // expectation
        println("[EXPECTATION]")
        var totalCount = Count()
        for (senId in 0 until trainSentences.size) {
            println("#$senId\n")
            val sentence = trainSentences[senId]

            val ioPair = computeInsideOutside(sentence = sentence, params = params)
            val sentenceCount = expectCount(sentence = sentence, params = params, ioPair = ioPair)

            totalCount += sentenceCount
        }

        // maximization
        println("[MAXIMIZATION]")
        params = maximization(count = totalCount)

        // evaluation
        println("[EVALUATION]")
        var totalAccuracy = Fraction()
        for (senId in 0 until trainSentences.size) {
            val sentence = trainSentences[senId]
            val prediction = decode(sentence = sentence, params = params)
            val accuracy = computeAccuracy(sentence = sentence, prediction = prediction, isDirected = true)
            totalAccuracy += accuracy
            println("#$senId:\n" +
                    "words: ${sentence.map { it -> it.word }}\n" +
                    "tags:  ${sentence.map { it -> it.tag }}\n" +
                    "pred:  $prediction\n" +
                    "gold:  ${sentence.map { it -> it.head }}\n" +
                    "accu:  $accuracy\n")
        }

        val time_end = System.currentTimeMillis()

        println("[epoch]: $epochId, [accuracy]: $totalAccuracy, [cost]: ${time_end - time_start}")
    }
}