package dugu9sword.dmv

import dugu9sword.Sentence

// Given a sentence, compute the inside terms and outside terms
fun computeInsideOutside(sentence: Sentence, params: Params): IOPair {
    val tags = sentence.map { it.tag }
    val insideTerms = PotentialTerm(sentenceSize = sentence.size)
    val outsideTerms = PotentialTerm(sentenceSize = sentence.size)

    var insideTimes = 0
    var outsideTimes = 0

    fun inside(h_seal: Int, h: Int, i: Int, j: Int): Double {
//        println("inside: ${tags[h]}${seal_to_label[h_seal]}\t[$i, $j]")

        /** Terminal case **/
        if (insideTerms.flags[h_seal][h][i][j])
            return insideTerms.quantities[h_seal][h][i][j]
        insideTimes++

        val h_tag = tagToId[tags[h]]!!
        var value = 0.0
        when (h_seal) {
            Seal.R_UNSEALED -> {
                /**
                 * Notice that the case satisfies
                 *      h = i
                 * Two cases:
                 *      i = j (BASE CASE)
                 *          [h→]{i..j} -> [h]
                 *      i < j (ATTACH R)
                 *          [h→]{i..j} -> [h→]{i..k-1} + [a-]{k,j}
                 */
                if (i == j) {
                    value += 1.0
                }
                if (i < j) {
                    for (k in i + 1..j) {
                        for (a in k..j) {
                            val a_tag = tagToId[tags[a]]!!
                            val valence = if (k == h + 1) Valence.F else Valence.T
                            value += (1 - params.stopProbs[h_tag][Dir.R][valence]) *
                                    params.chooseProbs[h_tag][Dir.R][a_tag] *
                                    inside(h_seal, h, i, k - 1) *
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
                    val valence = if (h == j) Valence.F else Valence.T
                    value += params.stopProbs[h_tag][Dir.R][valence] *
                            inside(Seal.R_UNSEALED, h, i, j)
                }
                if (h > i) {
                    for (k in i + 1..h) {
                        for (a in i..k - 1) {
                            val a_tag = tagToId[tags[a]]!!
                            val valence = if (h == k) Valence.F else Valence.T
                            value += (1 - params.stopProbs[h_tag][Dir.L][valence]) *
                                    params.chooseProbs[h_tag][Dir.L][a_tag] *
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
                val valence = if (h == i) Valence.F else Valence.T
                value += params.stopProbs[h_tag][Dir.L][valence] *
                        inside(Seal.L_UNSEALED, h, i, j)
            }
        }
        insideTerms.quantities[h_seal][h][i][j] = value
        insideTerms.flags[h_seal][h][i][j] = true
        return value
    }


    fun outside(h_seal: Int, h: Int, i: Int, j: Int): Double {
//        println("outside: ${tags[h]}${seal_to_label[h_seal]}\t[$i, $j]")

        /** Terminal case **/
        if (outsideTerms.flags[h_seal][h][i][j])
            return outsideTerms.quantities[h_seal][h][i][j]

        outsideTimes++

        val h_tag = tagToId[tags[h]]!!
        var value = 0.0
        when (h_seal) {
            Seal.R_UNSEALED -> {
                /**
                 * Two cases:
                 *      R SEALING
                 *          [h⇆]{i..j} -> [h→]{i..j}
                 *      R ATTACHMENT
                 *          [h→]{i..k} -> [h→]{i..j} + [a-]{j..k}
                 */
                val valence = if (h == j) Valence.F else Valence.T
                for (k in j..(sentence.size - 1)) {
                    if (k == j) {
                        value += outside(Seal.L_UNSEALED, h, i, j) *
                                params.stopProbs[h_tag][Dir.R][valence]
                    }
                    if (k > j) {
                        for (a in (j + 1)..k) {
                            val a_tag = tagToId[tags[a]]!!
                            if (k > j) {
                                value += outside(Seal.R_UNSEALED, h, i, k) *
                                        inside(Seal.SEALED, a, j, k) *
                                        params.chooseProbs[h_tag][Dir.R][a_tag] *
                                        (1 - params.stopProbs[h_tag][Dir.R][valence])
                            }
                        }
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
                val valence = if (h == i) Valence.F else Valence.T
                for (k in 0..i) {
                    if (k == i) {
                        value += outside(Seal.SEALED, h, k, j) *
                                params.stopProbs[h_tag][Dir.L][valence]
                    }
                    if (k < i) {
                        for (a in k..i - 1) {
                            val a_tag = tagToId[tags[a]]!!
                            value += outside(Seal.L_UNSEALED, h, k, j) *
                                    inside(Seal.SEALED, a, k, i - 1) *
                                    (1 - params.stopProbs[h_tag][Dir.L][valence]) *
                                    params.chooseProbs[h_tag][Dir.L][a_tag]
                        }
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
                if (i == 0 && j == sentence.size - 1) {
                    if (h == 0)
                        value += 1.0
                    else
                        value += 0.0
                }
                // R
                if (i > 0)
                    for (k in 0..(i - 1)) {
                        val k_tag = tagToId[tags[k]]!!
                        val valence = if (k == i - 1) Valence.F else Valence.T
                        value += outside(Seal.R_UNSEALED, k, k, j) *
                                inside(Seal.SEALED, k, k, i - 1) *
                                (1 - params.stopProbs[k_tag][Dir.R][valence]) *
                                params.chooseProbs[k_tag][Dir.R][h_tag]
                    }
                // L
                if (j < sentence.size - 1)
                    for (k in (j + 1)..(sentence.size - 1)) {
                        for (m in (j + 1)..k) {
                            val m_tag = tagToId[tags[m]]!!
                            val valence = if (m == j + 1) Valence.F else Valence.T
                            value += outside(Seal.L_UNSEALED, m, i, k) *
                                    inside(Seal.L_UNSEALED, m, j + 1, k) *
                                    (1 - params.stopProbs[m_tag][Dir.L][valence]) *
                                    params.chooseProbs[m_tag][Dir.L][h_tag]
                        }
                    }
            }
        }
        outsideTerms.quantities[h_seal][h][i][j] = value
        outsideTerms.flags[h_seal][h][i][j] = true
        return value
    }

    inside(Seal.SEALED, 0, 0, sentence.size - 1)
    println("inside: $insideTimes")

    return IOPair(insideTerms, outsideTerms)
}


// Given a sentence and the inside/outside terms, calculate the expected count
// for different rules.
fun expectCount(sentence: Sentence, params: Params, ioPair: IOPair): Count {
    return Count()
}