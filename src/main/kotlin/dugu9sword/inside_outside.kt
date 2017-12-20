package dugu9sword

import dugu9sword.treebank.Sentence


class IOTerm(val sentence_size: Int) {
    val quantities = arrayAs(
            Seal.size,
            sentence_size,
            sentence_size,
            sentence_size,
            { 0.0 }
    )
    val flags = arrayAs(
            quantities,
            { false }
    )
}

class InsideOutside(
        val choose_probs: Array3D<Double>,
        val stop_probs: Array3D<Double>,
        val sentence: Sentence) {


    val tags = sentence.map { it.tag }

    val inside_terms = IOTerm(sentence_size = sentence.size)
    val outside_terms = IOTerm(sentence_size = sentence.size)

    var inside_times = 0
    var outside_times = 0

    fun inside(h_seal: Int, h: Int, i: Int, j: Int): Double {
//        println("inside: ${tags[h]}${seal_to_label[h_seal]}\t[$i, $j]")

        /** Terminal case **/
        if (inside_terms.flags[h_seal][h][i][j])
            return inside_terms.quantities[h_seal][h][i][j]
        inside_times++

        val h_tag = tag_to_id[tags[h]]!!
        var value = 0.0
        when (h_seal) {
            Seal.R_UNSEALED -> {
                /**
                 * Notice that the case satisfies
                 *      h = i
                 * Two cases:
                 *      i = j (BASE CASE)
                 *          [h→]{i..j} -> [h]
                 *      i < j (ATTACH RIGHT)
                 *          [h→]{i..j} -> [h→]{i..k-1} + [a-]{k,j}
                 */
                if (i == j) {
                    value += 1.0
                }
                if (i < j) {
                    for (k in i + 1..j) {
                        for (a in k..j) {
                            val a_tag = tag_to_id[tags[a]]!!
                            val valence = if (k == h + 1) Valence.F else Valence.T
                            value += (1 - stop_probs[h_tag][Dir.RIGHT][valence]) *
                                    choose_probs[h_tag][Dir.RIGHT][a_tag] *
                                    inside(h_seal, h, i, k - 1) *
                                    inside(Seal.SEALED, a, k, j)
                        }
                    }
                }
            }
            Seal.L_UNSEALED -> {
                /**
                 * Two cases:
                 *      h = i (SEAL RIGHT)
                 *          [h⇆]{i..j} -> [h→]{i..j}
                 *      h > i (ATTACH LEFT)
                 *          [h⇆]{i..j} -> [a-]{i..k-1} + [h⇆]{k..j}
                 */
                if (h == i) {
                    val valence = if (h == j) Valence.F else Valence.T
                    value += stop_probs[h_tag][Dir.RIGHT][valence] *
                            inside(Seal.R_UNSEALED, h, i, j)
                }
                if (h > i) {
                    for (k in i + 1..h) {
                        for (a in i..k - 1) {
                            val a_tag = tag_to_id[tags[a]]!!
                            val valence = if (h == k) Valence.F else Valence.T
                            value += (1 - stop_probs[h_tag][Dir.LEFT][valence]) *
                                    choose_probs[h_tag][Dir.LEFT][a_tag] *
                                    inside(Seal.SEALED, a, i, k - 1) *
                                    inside(Seal.L_UNSEALED, h, k, j)
                        }
                    }
                }
            }
            Seal.SEALED -> {
                /**
                 * One case:
                 *      ~ (SEAL LEFT)
                 *          [h-]{i..j} -> [h⇆]{i..j}
                 */
                val valence = if (h == i) Valence.F else Valence.T
                value += stop_probs[h_tag][Dir.LEFT][valence] *
                        inside(Seal.L_UNSEALED, h, i, j)
            }
        }
        inside_terms.quantities[h_seal][h][i][j] = value
        inside_terms.flags[h_seal][h][i][j] = true
        return value
    }


    fun outside(h_seal: Int, h: Int, i: Int, j: Int): Double {
//        println("outside: ${tags[h]}${seal_to_label[h_seal]}\t[$i, $j]")

        /** Terminal case **/
        if (outside_terms.flags[h_seal][h][i][j])
            return outside_terms.quantities[h_seal][h][i][j]

        outside_times++

        val h_tag = tag_to_id[tags[h]]!!
        var value = 0.0
        when (h_seal) {
            Seal.R_UNSEALED -> {
                /**
                 * Two cases:
                 *      RIGHT SEALING
                 *          [h⇆]{i..j} -> [h→]{i..j}
                 *      RIGHT ATTACHMENT
                 *          [h→]{i..k} -> [h→]{i..j} + [a-]{j..k}
                 */
                val valence = if (h == j) Valence.F else Valence.T
                for (k in j..(sentence.size - 1)) {
                    if (k == j) {
                        value += outside(Seal.L_UNSEALED, h, i, j) *
                                stop_probs[h_tag][Dir.RIGHT][valence]
                    }
                    if (k > j) {
                        for (a in (j + 1)..k) {
                            val a_tag = tag_to_id[tags[a]]!!
                            if (k > j) {
                                value += outside(Seal.R_UNSEALED, h, i, k) *
                                        inside(Seal.SEALED, a, j, k) *
                                        choose_probs[h_tag][Dir.RIGHT][a_tag] *
                                        (1 - stop_probs[h_tag][Dir.RIGHT][valence])
                            }
                        }
                    }
                }
            }
            Seal.L_UNSEALED -> {
                /**
                 * Two cases:
                 *      LEFT ATTACHMENT
                 *          [h⇆]{k..j} -> [a-]{k..i-1} + [h⇆]{i..j}
                 *      LEFT SEALING
                 *          [h-]{i..j} -> [h⇆]{i..j}
                 */
                val valence = if (h == i) Valence.F else Valence.T
                for (k in 0..i) {
                    if (k == i) {
                        value += outside(Seal.SEALED, h, k, j) *
                                stop_probs[h_tag][Dir.LEFT][valence]
                    }
                    if (k < i) {
                        for (a in k..i - 1) {
                            val a_tag = tag_to_id[tags[a]]!!
                            value += outside(Seal.L_UNSEALED, h, k, j) *
                                    inside(Seal.SEALED, a, k, i - 1) *
                                    (1 - stop_probs[h_tag][Dir.LEFT][valence]) *
                                    choose_probs[h_tag][Dir.LEFT][a_tag]
                        }
                    }
                }
            }
            Seal.SEALED -> {
                /**
                 * Two cases:
                 *      TERMINAL CASE
                 *          [h-]{$..^}
                 *      LEFT ATTACHMENT
                 *          [m⇆]{i..k} -> [h-]{i..j} + [m⇆]{j+1..k}
                 *      RIGHT ATTACHMENT
                 *          Notice that in this case, m = k
                 *          [m→]{k..j} -> [m→]{k..i-1} + [h-]{i..j}
                 */
                // TERMINAL
                if (i == 0 && j == sentence.size - 1) {
                    if (h == 0)
                        value += 1.0
                    else
                        value += 0.0
                }
                // RIGHT
                if (i > 0)
                    for (k in 0..(i - 1)) {
                        val k_tag = tag_to_id[tags[k]]!!
                        val valence = if (k == i - 1) Valence.F else Valence.T
                        value += outside(Seal.R_UNSEALED, k, k, j) *
                                inside(Seal.SEALED, k, k, i - 1) *
                                (1 - stop_probs[k_tag][Dir.RIGHT][valence]) *
                                choose_probs[k_tag][Dir.RIGHT][h_tag]
                    }
                // LEFT
                if (j < sentence.size - 1)
                    for (k in (j + 1)..(sentence.size - 1)) {
                        for (m in (j + 1)..k) {
                            val m_tag = tag_to_id[tags[m]]!!
                            val valence = if (m == j + 1) Valence.F else Valence.T
                            value += outside(Seal.L_UNSEALED, m, i, k) *
                                    inside(Seal.L_UNSEALED, m, j + 1, k) *
                                    (1 - stop_probs[m_tag][Dir.LEFT][valence]) *
                                    choose_probs[m_tag][Dir.LEFT][h_tag]
                        }
                    }
            }
        }
        outside_terms.quantities[h_seal][h][i][j] = value
        outside_terms.flags[h_seal][h][i][j] = true
        return value
    }

}