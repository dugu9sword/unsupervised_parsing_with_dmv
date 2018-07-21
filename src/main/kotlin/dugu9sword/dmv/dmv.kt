package dugu9sword.dmv

import dugu9sword.*
import kotlin.system.exitProcess
import org.nd4j.linalg.factory.Nd4j as nj

fun main(args: Array<String>) {
    var params = Params()
    val trainSentences = trainTreeBank.sentences.filter { it.size <= 11 }
    println("Size: ${trainSentences.size}")
    for (epochId in 0 until 10) {
        val time_start = System.currentTimeMillis()


        // expectation
//        println("[EXPECTATION]")
        var totalCount = Count()
        for (senId in 0 until trainSentences.size) {
//        for (senId in 0 until 1) {
            val sentence = trainSentences[senId]

            val sentenceCount = expectCount(sentence = sentence, params = params)
            println(sentence.map { it -> it.tag })

//                print(sentenceCount.chooseCases[tagToId["DT"]!!][0].sum())
//            exitProcess(0)

//            println("Processing: #$senId\n")


            totalCount += sentenceCount
        }

//        if (epochId == 0) {
//            println("======  Initialize...  ======")
//            showChooseItems(params.chooseProbs, "VBP", true, trainSentences[0].map { it.tag })
//            showChooseItems(params.chooseProbs, "*ROOT", false, trainSentences[0].map { it.tag })
//            println("=============================")
//        }

        // maximization
//        println("[MAXIMIZATION]")
        params = maximization(count = totalCount)

//        showChooseItems(totalCount.chooseCases, "VBP", true, trainSentences[0].map { it.tag })
//        showChooseItems(totalCount.chooseCases, "*ROOT", false, trainSentences[0].map { it.tag })
//
        println(yellow("decide count"))
        showStopItems(totalCount.decideToStopCases, "PRP")
        println(yellow("whether count"))
        showStopItems(totalCount.whetherToStopCases, "PRP")



        // evaluation
//        println("[EVALUATION]")
        var totalAccuracy = Fraction()
        for (senId in 0 until trainSentences.size) {
            val sentence = trainSentences[senId]
            val prediction = decode(sentence = sentence, params = params)
            val accuracy = computeAccuracy(sentence = sentence, prediction = prediction, isDirected = true)
            totalAccuracy += accuracy
//            println("#$senId:\n" +
//                    "words: ${sentence.map { it -> it.word }}\n" +
//                    "tags:  ${sentence.map { it -> it.tag }}\n" +
//                    "pred:  $prediction\n" +
//                    "gold:  ${sentence.map { it -> it.head }}\n" +
//                    "accu:  $accuracy\n")
        }

        val time_end = System.currentTimeMillis()

        println(red("[epoch]: $epochId, [accuracy]: $totalAccuracy, [cost]: ${time_end - time_start}"))
    }
}