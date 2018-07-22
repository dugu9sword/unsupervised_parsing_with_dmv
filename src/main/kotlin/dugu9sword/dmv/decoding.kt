package dugu9sword.dmv

import dugu9sword.Array1D
import dugu9sword.Sentence

// Given a sentence, decode the tree and return the prediction
fun decode(sentence: Sentence, params: Params): List<Int> {
    val tags = sentence.map { it.tag }
    val decodingTerms = PotentialTerm(sentenceSize = sentence.size)
    val tree = Tree()

    fun maxSpanning(hSeal: Int, h: Int, i: Int, j: Int): Double {
        if (decodingTerms.flags[hSeal][h][i][j])
            return decodingTerms.quantities[hSeal][h][i][j]

        val hTagIdx = tagToId[tags[h]]!!
        var prob = -1.0
        val parent = Node(hSeal, h, i, j)
        when (hSeal) {
            Seal.SEALED -> {
                /**
                 * One case:
                 *      L SEALING:
                 *          [h-]{i..j} -> [h⇆]{i..j}
                 */
                val valence = if (h == i) Valence.ADJ else Valence.NON_ADJ
                prob = params.stopProbs[hTagIdx][Dir.L][valence] *
                        maxSpanning(Seal.L_UNSEALED, h, i, j)
                tree.attach(parent,
                        STOP_NODE,
                        Node(Seal.L_UNSEALED, h, i, j))
            }
            Seal.L_UNSEALED -> {
                /**
                 * Two cases:
                 *      L ATTACHMENT
                 *          [h⇆]{i..j} -> [a-]{i..k} + [h⇆]{k+1..j}
                 *      R SEALING
                 *          [h⇆]{i..j} -> [h→]{i..j}
                 */
                if (h == i) {
                    val valence = if (h == j) Valence.ADJ else Valence.NON_ADJ
                    prob = params.stopProbs[hTagIdx][Dir.R][valence] *
                            maxSpanning(Seal.R_UNSEALED, h, i, j)
                    tree.attach(parent,
                            Node(Seal.R_UNSEALED, h, i, j),
                            STOP_NODE)
                }
                if (h != i) {
                    var goodProb = Double.NEGATIVE_INFINITY
                    var goodK = 0
                    var goodA = 0
                    for (k in i..(h - 1)) {
                        for (a in i..k) {
                            val aTagIdx = tagToId[tags[a]]!!
                            val valence = if (h == a + 1) Valence.ADJ else Valence.NON_ADJ
                            val caseProb = (1 - params.stopProbs[hTagIdx][Dir.L][valence]) *
                                    params.chooseProbs[hTagIdx][Dir.L][aTagIdx] *
                                    maxSpanning(Seal.SEALED, a, i, k) *
                                    maxSpanning(Seal.L_UNSEALED, h, k + 1, j)
                            if (caseProb > goodProb) {
                                goodA = a
                                goodK = k
                                goodProb = caseProb
                            }
                        }
                    }
                    tree.attach(parent,
                            Node(Seal.SEALED, goodA, i, goodK),
                            Node(Seal.L_UNSEALED, h, goodK + 1, j))
                    prob = goodProb
                }
            }
            Seal.R_UNSEALED -> {
                /**
                 * In this case, h==i.
                 *
                 * Two cases:
                 *      R ATTACHMENT
                 *          [h→]{i..j} -> [h→]{i..k-1} + [a-]{k..j}
                 *      BASE CASE
                 *          [h→]{i..i} -> [h]{i..i}
                 */
                if (i == j) {
                    tree.attach(parent,
                            TERMINAL_NODE,
                            STOP_NODE)
                    prob = 1.0
                }
                if (i < j) {
                    var goodProb = Double.NEGATIVE_INFINITY
                    var goodK = 0
                    var goodA = 0
                    for (k in (i + 1)..j) {
                        for (a in (k..j)) {
                            val aTagIdx = tagToId[tags[a]]!!
                            val valence = if (k == h + 1) Valence.ADJ else Valence.NON_ADJ
                            val caseProb = (1 - params.stopProbs[hTagIdx][Dir.R][valence]) *
                                    params.chooseProbs[hTagIdx][Dir.R][aTagIdx] *
                                    maxSpanning(Seal.SEALED, a, k, j) *
                                    maxSpanning(Seal.R_UNSEALED, h, i, k - 1)
                            if (caseProb > goodProb) {
                                goodA = a
                                goodK = k
                                goodProb = caseProb
                            }
                        }
                    }
                    tree.attach(parent,
                            Node(Seal.R_UNSEALED, h, i, goodK - 1),
                            Node(Seal.SEALED, goodA, goodK, j))
                    prob = goodProb
                }
            }
        }
        assert(prob >= 0.0)
        decodingTerms.flags[hSeal][h][i][j] = true
        decodingTerms.quantities[hSeal][h][i][j] = prob
        return prob
    }

    fun search(node: Node, left: Boolean, prediction: Array1D<Int>) {
        val child = if (left) tree.getLeft(node)!! else tree.getRight(node)!!
        if (child == STOP_NODE || child == TERMINAL_NODE)
            return
        if (child.h != node.h)
            prediction[child.h] = node.h
        search(child, left = true, prediction = prediction)
        search(child, left = false, prediction = prediction)
    }

    maxSpanning(hSeal = Seal.SEALED, h = 0, i = 0, j = sentence.size - 1)
    val prediction = Array1D(sentence.size) { -1 }
    search(node = Node(h_seal = Seal.SEALED, h = 0, i = 0, j = sentence.size - 1),
            left = false,
            prediction = prediction)
    return prediction.asList()
}