package dugu9sword.dmv

import dugu9sword.*
import kotlin.math.abs

fun main(args: Array<String>) {
//    val wiki = TreeBank("dataset/wiki_88w.nop.txt")
    val trainSentences = trainTreeBank.sentences.filter { it.size in 0..11 }//.subList(0, 100)
    val testSentences = testTreeBank.sentences.filter { it.size in 0..11 }

    var params = Params()
    initChooseProbs(params, trainSentences, ChooseProbInitMode.RANDOM)
    initStopProbs(params, trainSentences)

    println("Size: ${trainSentences.size}")

//    val tagsToShow = trainSentences[0].map { it -> it.tag }.toHashSet().toList()
    val tagsToShow = allTags
    for (epochId in 0 until 100) {
        val time1=System.currentTimeMillis()

        if (epochId == 0) {
            dbg.log(view(params.chooseProbs, "*ROOT", 'R', tagsToShow),
                    Color.RED, Mode.BOTH, title = "init")
//            dbg.log(view(params.chooseProbs, "NNP", 'R', tagsToShow),
//                    Color.RED, Mode.BOTH, title = "init")
            for (tag in listOf("VB", "NN"))
                dbg.log(view(params.stopProbs, tag),
                        Color.RED, Mode.BOTH, "init")
        }

        /** expectation */
        println(green("[EXPECTATION]"))
        var totalCount = Count()
        for (senId in 0 until trainSentences.size) {
            val sentence = trainSentences[senId]
            val sentenceCount = expectCount(sentence = sentence, params = params)
//            println(sentence.map { it -> it.tag })
            totalCount += sentenceCount
        }

//        dbg.log(view(totalCount.chooseCases, "*ROOT", 'R', tagsToShow))
//        dbg.log(view(totalCount.chooseCases, "VBZ", 'L', tagsToShow))

        /** maximization */
        println(green("[MAXIMIZATION]"))
        params = maximization(count = totalCount)

//        dbg.log(view(params.chooseProbs, "*ROOT", 'R', tagsToShow),
//                Color.RED, Mode.BOTH, "epoch - $epochId")
//        dbg.log(view(totalCount.decideToStopCases, "NN"),
//                Color.RED, Mode.BOTH, "epoch - $epochId decide")
//        dbg.log(view(totalCount.whetherToStopCases, "NN"),
//                Color.RED, Mode.BOTH, "epoch - $epochId whether")
//        for (tag in listOf("VB", "NN", "NNP"))
//            dbg.log(view(params.stopProbs, tag),
//                    Color.RED, Mode.BOTH, "epoch - $epochId prob")

        /** evaluation */
        println(green("[EVALUATION]"))
        val evalSentences = mapOf(
                "train" to trainSentences
                ,"test " to testSentences
        )
        for ((evalSetName, evalSet) in evalSentences) {
            var totalDDA = Fraction()
            var totalUDA = Fraction()
            for (senId in 0 until evalSet.size) {
                val sentence = evalSet[senId]
                val prediction = decode(sentence = sentence, params = params)
                val dda = computeAccuracy(sentence = sentence, prediction = prediction, isDirected = true)
                val uda = computeAccuracy(sentence = sentence, prediction = prediction, isDirected = false)
                totalDDA += dda
                totalUDA += uda
//                println("#$evalSetName $senId:\n" +
//                        "words: ${sentence.map { it -> it.word }}\n" +
//                        "tags:  ${sentence.map { it -> it.tag }}\n" +
//                        "pred:  $prediction\n" +
//                        "gold:  ${sentence.map { it -> it.head }}\n" +
//                        "DDA :  $dda\n" +
//                        "UDA :  $uda")
            }
            dbg.log("[epoch]: $evalSetName - $epochId, [DDA]: $totalDDA, [UDA]: $totalUDA",
                    Color.RED, Mode.BOTH)
        }
        val time2=System.currentTimeMillis()
        dbg.log("[cost] ${(time2-time1)/1000}s")


    }
}

enum class ChooseProbInitMode {
    RANDOM,
    UNIFORM,
    HARMONIC,
    PRIOR,
}

fun initChooseProbs(params: Params, sentences: List<Sentence>, initMode: ChooseProbInitMode) {
    val rootId = tagToId[Special.ROOT]!!

    for (i in 0 until tagNum) {
        // Root attachment equally
        params.chooseProbs[rootId][Dir.R][i] = if (i == rootId) 0.0 else 1.0 / (tagNum - 1)
        params.chooseProbs[rootId][Dir.L][i] = 0.0
        // Root never attached
        params.chooseProbs[i][Dir.R][rootId] = 0.0
        params.chooseProbs[i][Dir.L][rootId] = 0.0
    }

    when (initMode) {
        ChooseProbInitMode.UNIFORM -> {
            for (pTagIdx in 0 until tagNum)
                for (cTagIdx in 0 until tagNum)
                    if (pTagIdx != rootId && cTagIdx != rootId) {
                        params.chooseProbs[pTagIdx][Dir.L][cTagIdx] = 1.0
                        params.chooseProbs[pTagIdx][Dir.R][cTagIdx] = 1.0
                    }
            normalizeDoubleArray_(params.chooseProbs)
        }
        ChooseProbInitMode.HARMONIC -> {
            val k = 0
            val totalDistance = doubleArrayAs(tagNum, Dir.size, tagNum) { 10.0 } // give a bias
            val totalTimes = doubleArrayAs(tagNum, Dir.size, tagNum) { 1.0 }
            for (sentence in sentences) {
                val tags = sentence.map { it -> it.tag }
                for (p in 1 until tags.size)
                    for (c in 1 until tags.size) {
                        val pTagIdx = tagToId[tags[p]]!!
                        val cTagIdx = tagToId[tags[c]]!!
                        val dist = abs(0.0 + p - c)
                        if (c < p) {
                            totalDistance[pTagIdx][Dir.L][cTagIdx] += dist
                            totalTimes[pTagIdx][Dir.L][cTagIdx] += 1.0
                        }
                        if (c > p) {
                            totalDistance[pTagIdx][Dir.R][cTagIdx] += dist
                            totalTimes[pTagIdx][Dir.R][cTagIdx] += 1.0
                        }
                    }
            }
            for (pTagIdx in 0 until tagNum)
                for (cTagIdx in 0 until tagNum) {
                    if (pTagIdx == rootId || cTagIdx == rootId)
                        continue
                    val avgLDist =
                            totalDistance[pTagIdx][Dir.L][cTagIdx] / (totalTimes[pTagIdx][Dir.L][cTagIdx] + eps)
                    params.chooseProbs[pTagIdx][Dir.L][cTagIdx] = 1 / (avgLDist + k)
                    val avgRDist =
                            totalDistance[pTagIdx][Dir.R][cTagIdx] / (totalTimes[pTagIdx][Dir.R][cTagIdx] + eps)
                    params.chooseProbs[pTagIdx][Dir.R][cTagIdx] = 1 / (avgRDist + k)
                }
            normalizeDoubleArray_(params.chooseProbs)
        }
        ChooseProbInitMode.RANDOM -> {
            for (pTagIdx in 0 until tagNum)
                for (cTagIdx in 0 until tagNum)
                    if (pTagIdx != rootId && cTagIdx != rootId) {
                        params.chooseProbs[pTagIdx][Dir.L][cTagIdx] = Math.random()
                        params.chooseProbs[pTagIdx][Dir.R][cTagIdx] = Math.random()
                    }
        }
        ChooseProbInitMode.PRIOR ->{
            for (pTagIdx in 0 until tagNum)
                for (cTagIdx in 0 until tagNum)
                    if (pTagIdx != rootId && cTagIdx != rootId) {
                        params.chooseProbs[pTagIdx][Dir.L][cTagIdx] = 1.0
                        params.chooseProbs[pTagIdx][Dir.R][cTagIdx] = 1.0
                    }
            params.chooseProbs[rootId][Dir.R][tagToId["VBZ"]!!] = 3.0
            params.chooseProbs[rootId][Dir.R][tagToId["VBD"]!!] = 3.0
            params.chooseProbs[rootId][Dir.R][tagToId["VBP"]!!] = 3.0
            normalizeDoubleArray_(params.chooseProbs)
        }
    }
}


fun initStopProbs(params: Params, sentences: List<Sentence>) {
    val rootId = tagToId[Special.ROOT]!!

    for (i in 0 until tagNum) {
        if (i == rootId) {
            params.stopProbs[rootId][Dir.L][Valence.ADJ] = 1.0
            params.stopProbs[rootId][Dir.L][Valence.NON_ADJ] = 1.0
            params.stopProbs[rootId][Dir.R][Valence.ADJ] = 0.0
            params.stopProbs[rootId][Dir.R][Valence.NON_ADJ] = 1.0
        } else {
            params.stopProbs[i][Dir.L][Valence.ADJ] = 0.5
            params.stopProbs[i][Dir.R][Valence.ADJ] = 0.5
            params.stopProbs[i][Dir.L][Valence.NON_ADJ] = 0.5
            params.stopProbs[i][Dir.R][Valence.NON_ADJ] = 0.5
        }
    }
}