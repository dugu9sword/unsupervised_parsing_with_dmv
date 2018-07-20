package dugu9sword.dmv

import dugu9sword.Array1D
import dugu9sword.Sentence

// Given a sentence, decode the tree and return the prediction
fun decode(sentence: Sentence, params: Params): List<Int> {
    val tags = sentence.map { it.tag }
    val decodingTerms = IOTerm(sentenceSize = sentence.size)
    val tree = Tree()

    fun maxSpanning(h_seal: Int, h: Int, i: Int, j: Int): Double {
        if (decodingTerms.flags[h_seal][h][i][j])
            return decodingTerms.quantities[h_seal][h][i][j]

        val h_tag = tagToId[tags[h]]!!
        var prob = -1.0
        val parent = Node(h_seal, h, i, j)
        when (h_seal) {
            Seal.SEALED -> {
                /**
                 * One case:
                 *      L SEALING:
                 *          [h-]{i..j} -> [h⇆]{i..j}
                 */
                val valence = if (h == i) Valence.F else Valence.T
                prob = params.stopProbs[h_tag][Dir.L][valence] * maxSpanning(Seal.L_UNSEALED, h, i, j)
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
                    val valence = if (h == j) Valence.F else Valence.T
                    prob = params.stopProbs[h_tag][Dir.R][valence] * maxSpanning(Seal.R_UNSEALED, h, i, j)
                    tree.attach(parent,
                            Node(Seal.R_UNSEALED, h, i, j),
                            STOP_NODE)
                }
                if (h != i) {
                    var good_prob = 0.0
                    var good_k = 0
                    var good_a = 0
                    for (k in i..(h - 1)) {
                        for (a in i..k) {
                            val a_tag = tagToId[tags[a]]!!
                            val valence = if (h == a + 1) Valence.F else Valence.T
                            val case_prob = (1 - params.stopProbs[h_tag][Dir.L][valence]) *
                                    params.chooseProbs[h_tag][Dir.L][a_tag] *
                                    maxSpanning(Seal.SEALED, a, i, k) *
                                    maxSpanning(Seal.L_UNSEALED, h, k + 1, j)
                            if (case_prob > good_prob) {
                                good_a = a
                                good_k = k
                                good_prob = case_prob
                            }
                        }
                    }
                    tree.attach(parent,
                            Node(Seal.SEALED, good_a, i, good_k),
                            Node(Seal.L_UNSEALED, h, good_k + 1, j))
                    prob = good_prob
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
                    var good_prob = 0.0
                    var good_k = 0
                    var good_a = 0
                    for (k in (i + 1)..j) {
                        for (a in (k..j)) {
                            val a_tag = tagToId[tags[a]]!!
                            val valence = if (k == h + 1) Valence.F else Valence.T
                            val case_prob = (1 - params.stopProbs[h_tag][Dir.R][valence]) *
                                    params.chooseProbs[h_tag][Dir.R][a_tag] *
                                    maxSpanning(Seal.SEALED, a, k, j) *
                                    maxSpanning(Seal.R_UNSEALED, h, i, k - 1)
                            if (case_prob > good_prob) {
                                good_a = a
                                good_k = k
                                good_prob = case_prob
                            }
                        }
                    }
                    tree.attach(parent,
                            Node(Seal.R_UNSEALED, h, i, good_k - 1),
                            Node(Seal.SEALED, good_a, good_k, j))
                    prob = good_prob
                }
            }
        }
        assert(prob >= 0.0)
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

    maxSpanning(h_seal = Seal.SEALED, h = 0, i = 0, j = sentence.size - 1)
    val prediction = Array1D(sentence.size) { -1 }
    search(node = Node(h_seal = Seal.SEALED, h = 0, i = 0, j = sentence.size - 1),
            left = false,
            prediction = prediction)
    return prediction.asList()
}