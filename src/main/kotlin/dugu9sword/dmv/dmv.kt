package dugu9sword.dmv

import dugu9sword.*
import org.nd4j.linalg.factory.Nd4j as nj


fun main(args: Array<String>) {
    var params = Params()
    val trainSentences = trainTreeBank.sentences.filter { it.size < 10 }
    println("Size: ${trainSentences.size}")
    for (epochId in 0 until 1) {
        val time_start = System.currentTimeMillis()

        // training
        var totalCount = Count()
        for (senId in 0 until trainSentences.size) {
            val sentence = trainSentences[senId]

            val ioPair = computeInsideOutside(sentence = sentence, params = params)
            val sentenceCount = expectCount(sentence = sentence, params = params, ioPair = ioPair)

//            totalCount += count
        }

        // evaluation
        var totalAccuracy = Fraction()
        for (senId in 0 until trainSentences.size) {
//            val prediction = decode(sentence = sentence, params = params)
//            val accuracy = computeAccuracy(sentence = sentence, prediction = prediction, isDirected = false)
//            totalAccuracy += accuracy
//            println("#$senId\t:  ${sentence.map { it -> it.word }}")
//            println("Accuracy: $accuracy")
////            exitProcess(0)

        }

        val time_end = System.currentTimeMillis()
        println("[epoch]: $epochId, [accuracy]: $totalAccuracy, [cost]: ${time_end - time_start}")
    }
}