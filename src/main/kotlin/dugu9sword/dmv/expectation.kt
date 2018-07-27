package dugu9sword.dmv

import dugu9sword.*

// Given a sentence and the params, calculate the expected count
// for different rules.
fun expectCount(sentence: Sentence, params: Params): Count {
    val io = InsideOutsideCounter(sentence = sentence, params = params)
    val count = Count()

    val tags = sentence.map { it.tag }
    val sentenceSize = sentence.size

    // count stop rules
    for (h in 0 until sentenceSize) {
        val hTagIdx = tagToId[tags[h]]!!

        // h, left, non_adj
        for (i in 0 until h)
            for (j in h until sentenceSize) {
                count.decideToStopCases[hTagIdx][Dir.L][Valence.NON_ADJ] += io.count(Seal.SEALED, h, i, j)
                count.whetherToStopCases[hTagIdx][Dir.L][Valence.NON_ADJ] += io.count(Seal.L_UNSEALED, h, i, j)
            }
        // h, left, adj
        for (j in h until sentenceSize) {
            count.decideToStopCases[hTagIdx][Dir.L][Valence.ADJ] += io.count(Seal.SEALED, h, h, j)
            count.whetherToStopCases[hTagIdx][Dir.L][Valence.ADJ] += io.count(Seal.L_UNSEALED, h, h, j)
        }
        // h, right, adj
        count.decideToStopCases[hTagIdx][Dir.R][Valence.ADJ] += io.count(Seal.L_UNSEALED, h, h, h)
        count.whetherToStopCases[hTagIdx][Dir.R][Valence.ADJ] += io.count(Seal.R_UNSEALED, h, h, h)
        // h, right, non_adj
        for (j in h + 1 until sentenceSize) {
            count.decideToStopCases[hTagIdx][Dir.R][Valence.NON_ADJ] += io.count(Seal.L_UNSEALED, h, h, j)
            count.whetherToStopCases[hTagIdx][Dir.R][Valence.NON_ADJ] += io.count(Seal.R_UNSEALED, h, h, j)
        }

        // h, left, a
        for (i in 0 until h)
            for (j in h until sentenceSize) {
                for (a in i until h) {
                    val aTagIdx = tagToId[tags[a]]!!
                    for (k in a until h)
                        count.chooseCases[hTagIdx][Dir.L][aTagIdx] += io.countLeftAttachment(h, a, i, k, j)
                }
            }

        // h, right, a
        for (j in h + 1 until sentenceSize)
            for (a in h..j) {
                val aTagIdx = tagToId[tags[a]]!!
                for (k in h + 1..a)
                    count.chooseCases[hTagIdx][Dir.R][aTagIdx] += io.countRightAttachment(h, a, k, j)
            }
    }
    return count
}

class InsideOutsideCounter(
        private val sentence: Sentence,
        private val params: Params) {

    private val tags = sentence.map { it.tag }
    private val insideTerms = PotentialTerm(sentenceSize = sentence.size)
    private val outsideTerms = PotentialTerm(sentenceSize = sentence.size)

    var insideTimes = 0
    var outsideTimes = 0

    fun count(hSeal: Int, h: Int, i: Int, j: Int): Double {
        return inside(hSeal, h, i, j) *
                outside(hSeal, h, i, j) /
//                1.0
                inside(Seal.SEALED, 0, 0, sentence.size - 1)
    }

    // [h⇆]{i..j} -> [h⇆]{k+1,j} + [a-]{i,k}
    fun countLeftAttachment(h: Int, a: Int, i: Int, k: Int, j: Int): Double {
        val hTagIdx = tagToId[tags[h]]!!
        val aTagIdx = tagToId[tags[a]]!!
        val valence = if (h == k + 1) Valence.ADJ else Valence.NON_ADJ
        val ret = inside(Seal.SEALED, a, i, k) *
                inside(Seal.L_UNSEALED, h, k + 1, j) *
                outside(Seal.L_UNSEALED, h, i, j) *
                params.chooseProbs[hTagIdx][Dir.L][aTagIdx] *
                (1 - params.stopProbs[hTagIdx][Dir.L][valence]) /
//                1.0
                inside(Seal.SEALED, 0, 0, sentence.size - 1)
//        dbg.log("outside: ${tags[h]}${seal_to_label[Seal.L_UNSEALED]}\t[$i, $j] = ${outside(Seal.L_UNSEALED, h, i, j)}\n")
        return ret
    }

    // [h→]{h..j} -> [h→]{h..k-1} + [a-]{k..j}
    fun countRightAttachment(h: Int, a: Int, k: Int, j: Int): Double {
        val hTagIdx = tagToId[tags[h]]!!
        val aTagIdx = tagToId[tags[a]]!!
        val valence = if (h == k - 1) Valence.ADJ else Valence.NON_ADJ
        val ret = inside(Seal.SEALED, a, k, j) *
                inside(Seal.R_UNSEALED, h, h, k - 1) *
                outside(Seal.R_UNSEALED, h, h, j) *
                params.chooseProbs[hTagIdx][Dir.R][aTagIdx] *
                (1 - params.stopProbs[hTagIdx][Dir.R][valence]) /
                inside(Seal.SEALED, 0, 0, sentence.size - 1)
//        if (h == 0)
//            print("right: $ret\n")
        return ret
    }

    fun inside(hSeal: Int, h: Int, i: Int, j: Int): Double {

        /** Terminal case **/
        if (insideTerms.flags[hSeal][h][i][j])
            return insideTerms.quantities[hSeal][h][i][j]
        insideTimes++

        val hTagIdx = tagToId[tags[h]]!!
        var value = 0.0
        when (hSeal) {
            Seal.R_UNSEALED -> {
                /**
                 * Notice that the case satisfies
                 *      h = i
                 * Two cases:
                 *      i = j (BASE CASE)
                 *          [h→]{i..j} -> [h] (NT -> Terminal, with probability 1.0)
                 *      i < j (ATTACH R)
                 *          [h→]{i..j} -> [h→]{i..k-1} + [a-]{k,j}
                 */
                if (i == j) {
                    value += 1.0
                }
                if (i < j) {
                    for (k in i + 1..j) {
                        for (a in k..j) {
                            val aTagIdx = tagToId[tags[a]]!!
                            val valence = if (k == h + 1) Valence.ADJ else Valence.NON_ADJ
                            value += (1 - params.stopProbs[hTagIdx][Dir.R][valence]) *
                                    params.chooseProbs[hTagIdx][Dir.R][aTagIdx] *
                                    inside(hSeal, h, i, k - 1) *
                                    inside(Seal.SEALED, a, k, j)
                        }
                    }
                }
            }
            Seal.L_UNSEALED -> {
                /**
                 * Two cases:
                 *      h = i (SEAL R)
                 *          [h⇆]{i..j} -> [h→]{i..j}
                 *      h > i (ATTACH L)
                 *          [h⇆]{i..j} -> [a-]{i..k-1} + [h⇆]{k..j}
                 */
                if (h == i) {
                    val valence = if (h == j) Valence.ADJ else Valence.NON_ADJ
                    value += params.stopProbs[hTagIdx][Dir.R][valence] *
                            inside(Seal.R_UNSEALED, h, i, j)
                }
                if (h > i) {
                    for (k in i + 1..h) {
                        for (a in i..k - 1) {
                            val aTagIdx = tagToId[tags[a]]!!
                            val valence = if (h == k) Valence.ADJ else Valence.NON_ADJ
                            value += (1 - params.stopProbs[hTagIdx][Dir.L][valence]) *
                                    params.chooseProbs[hTagIdx][Dir.L][aTagIdx] *
                                    inside(Seal.SEALED, a, i, k - 1) *
                                    inside(Seal.L_UNSEALED, h, k, j)
                        }
                    }
                }
            }
            Seal.SEALED -> {
                /**
                 * One case:
                 *      ~ (SEAL L)
                 *          [h-]{i..j} -> [h⇆]{i..j}
                 */
                val valence = if (h == i) Valence.ADJ else Valence.NON_ADJ
                value += params.stopProbs[hTagIdx][Dir.L][valence] *
                        inside(Seal.L_UNSEALED, h, i, j)
            }
        }
        insideTerms.quantities[hSeal][h][i][j] = value
        insideTerms.flags[hSeal][h][i][j] = true
//        println("inside: ${tags[h]}${seal_to_label[hSeal]}\t[$i, $j]  ${insideTerms.quantities[hSeal][h][i][j]}")

        return value
    }


    fun outside(hSeal: Int, h: Int, i: Int, j: Int): Double {
//        dbg.log("outside: ${tags[h]}${seal_to_label[hSeal]}\t[$i, $j]\n")

        /** Terminal case **/
        if (outsideTerms.flags[hSeal][h][i][j])
            return outsideTerms.quantities[hSeal][h][i][j]

        outsideTimes++

        val hTagIdx = tagToId[tags[h]]!!
        var value = 0.0
        when (hSeal) {
            Seal.R_UNSEALED -> {
                /**
                 * Two cases:
                 *      R SEALING
                 *          [h⇆]{i..j} -> [h→]{i..j}
                 *      R ATTACHMENT
                 *          [h→]{i..k} -> [h→]{i..j} + [a-]{j+1..k}
                 */
                val valence = if (h == j) Valence.ADJ else Valence.NON_ADJ
                value += outside(Seal.L_UNSEALED, h, i, j) *
                        params.stopProbs[hTagIdx][Dir.R][valence]
                for (k in j + 1..(sentence.size - 1)) {
                    for (a in (j + 1)..k) {
                        val aTagIdx = tagToId[tags[a]]!!
                        value += outside(Seal.R_UNSEALED, h, i, k) *
                                inside(Seal.SEALED, a, j + 1, k) *
                                params.chooseProbs[hTagIdx][Dir.R][aTagIdx] *
                                (1 - params.stopProbs[hTagIdx][Dir.R][valence])
                    }
                }
            }
            Seal.L_UNSEALED -> {
                /**
                 * Two cases:
                 *      L ATTACHMENT
                 *          [h⇆]{k..j} -> [a-]{k..i-1} + [h⇆]{i..j}
                 *      L SEALING
                 *          [h-]{i..j} -> [h⇆]{i..j}
                 */
                val valence = if (h == i) Valence.ADJ else Valence.NON_ADJ
                value += outside(Seal.SEALED, h, i, j) *
                        params.stopProbs[hTagIdx][Dir.L][valence]
                for (k in 0 until i) {
                    for (a in k until i) {
                        val aTagIdx = tagToId[tags[a]]!!
                        value += outside(Seal.L_UNSEALED, h, k, j) *
                                inside(Seal.SEALED, a, k, i - 1) *
                                (1 - params.stopProbs[hTagIdx][Dir.L][valence]) *
                                params.chooseProbs[hTagIdx][Dir.L][aTagIdx]
                    }
                }
            }
            Seal.SEALED -> {
                /**
                 * Two cases:
                 *      TERMINAL_NODE CASE
                 *          [h-]{$..^}
                 *      L ATTACHMENT
                 *          [m⇆]{i..k} -> [h-]{i..j} + [m⇆]{j+1..k}
                 *      R ATTACHMENT
                 *          Notice that in this case, m = k
                 *          [m→]{k..j} -> [m→]{k..i-1} + [h-]{i..j}
                 */
                // TERMINAL_NODE
                if (i == 0 && j == sentence.size - 1)
                    value += if (h == 0) 1.0 else 0.0
                // L
                if (j < sentence.size - 1)
                    for (k in (j + 1)..(sentence.size - 1)) {
                        for (m in (j + 1)..k) {
                            val mTagIdx = tagToId[tags[m]]!!
                            val valence = if (m == j + 1) Valence.ADJ else Valence.NON_ADJ
                            value += outside(Seal.L_UNSEALED, m, i, k) *
                                    inside(Seal.L_UNSEALED, m, j + 1, k) *
                                    (1 - params.stopProbs[mTagIdx][Dir.L][valence]) *
                                    params.chooseProbs[mTagIdx][Dir.L][hTagIdx]
                        }
                    }
                // R
                if (i > 0)
                    for (k in 0..(i - 1)) {
                        val kTagIdx = tagToId[tags[k]]!!
                        val valence = if (k == i - 1) Valence.ADJ else Valence.NON_ADJ
                        value += outside(Seal.R_UNSEALED, k, k, j) *
                                inside(Seal.R_UNSEALED, k, k, i - 1) *
                                (1 - params.stopProbs[kTagIdx][Dir.R][valence]) *
                                params.chooseProbs[kTagIdx][Dir.R][hTagIdx]
                    }
            }
        }
        outsideTerms.quantities[hSeal][h][i][j] = value
        outsideTerms.flags[hSeal][h][i][j] = true
        return value
    }

}


