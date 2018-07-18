package dugu9sword

import dugu9sword.treebank.Accuracy
import dugu9sword.treebank.Sentence
import dugu9sword.treebank.TreeBank
import dugu9sword.treebank.computeAccuracy
import kotlin.system.exitProcess


// sealed types
object Seal {
    const val R_UNSEALED = 0
    const val L_UNSEALED = 1
    const val SEALED = 2

    const val size = 3
}

val seal_to_label = mapOf(
        Seal.R_UNSEALED to "→",
        Seal.L_UNSEALED to "⇆",
        Seal.SEALED to "-"
)

// treebanks
val trainTreebank = TreeBank("dataset/wsj_develop.txt", 1, 3, 6, 7)
val testTreebank = TreeBank("dataset/wsj_test.txt", 1, 3, 6, 7)


// tags
val tagToId = trainTreebank.tagDict
val idToTag = tagToId.map { it.value to it.key }
val tagNum = tagToId.size

// directions
object Dir {
    const val LEFT = 0
    const val RIGHT = 1

    const val size = 2
}


// valence
object Valence {
    const val F = 0
    const val T = 1

    const val size = 2
}

class IOTerm(val sentenceSize: Int) {
    val quantities = arrayAs(
            Seal.size,       // seal type
            sentenceSize,   // h = location of head word
            sentenceSize,   // i
            sentenceSize,   // j
            { 0.0 }
    )
    val flags = arrayAs(
            quantities,
            { false }
    )
}

data class IOPair(val insideTerm: IOTerm, val outsideTerm: IOTerm)

// Node definition
data class Node(val h_seal: Int, val h: Int, val i: Int, val j: Int)

val STOP_NODE = Node(h_seal = -1, h = -1, i = -1, j = -1)
val TERMINAL_NODE = Node(h_seal = -2, h = -2, i = -2, j = -2)

// Tree definition
class Tree {
    private val leftChildren = HashMap<Node, Node>()
    private val rightChildren = HashMap<Node, Node>()

    fun attach(parent: Node, leftChild: Node, rightChild: Node) {
        this.leftChildren[parent] = leftChild
        this.rightChildren[parent] = rightChild
    }

    fun getLeft(parent: Node): Node? = this.leftChildren[parent]
    fun getRight(parent: Node): Node? = this.rightChildren[parent]
}

// A fake fraction
data class FakeFraction(val numerator: Float, val denominator: Float) {
    operator fun plus(fakeFraction: FakeFraction): FakeFraction {
        return FakeFraction(numerator = numerator + fakeFraction.numerator,
                denominator = denominator + fakeFraction.denominator)
    }

    fun eval(): Float {
        return numerator / denominator
    }
}

// params of the model
object params {
    val chooseProbs = initChooseProbs()
    val stopProbs = initStopProbs()

    fun initStopProbs(): Array3D<Double> {
        return arrayAs(tagToId.size, Dir.size, Valence.size, { Math.random() })
    }

    fun initChooseProbs(): Array3D<Double> {
        fun _sumToOneArray(dim: Int): Array<Double> {
            val array = arrayAs(dim, { Math.random() })
            val sum = array.reduce({ a, b -> a + b })
            return array.map { it / sum }.toTypedArray()
        }
        return arrayAs(tagToId.size, Dir.size, { _sumToOneArray(tagToId.size) })
    }
}


// Given a sentence, compute the inside terms and outside terms
fun expectation(sentence: Sentence): IOPair {
    val tags = sentence.map { it.tag }
    val insideTerms = IOTerm(sentenceSize = sentence.size)
    val outsideTerms = IOTerm(sentenceSize = sentence.size)

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
                 *      i < j (ATTACH RIGHT)
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
                            value += (1 - params.stopProbs[h_tag][Dir.RIGHT][valence]) *
                                    params.chooseProbs[h_tag][Dir.RIGHT][a_tag] *
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
                    value += params.stopProbs[h_tag][Dir.RIGHT][valence] *
                            inside(Seal.R_UNSEALED, h, i, j)
                }
                if (h > i) {
                    for (k in i + 1..h) {
                        for (a in i..k - 1) {
                            val a_tag = tagToId[tags[a]]!!
                            val valence = if (h == k) Valence.F else Valence.T
                            value += (1 - params.stopProbs[h_tag][Dir.LEFT][valence]) *
                                    params.chooseProbs[h_tag][Dir.LEFT][a_tag] *
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
                value += params.stopProbs[h_tag][Dir.LEFT][valence] *
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
                 *      RIGHT SEALING
                 *          [h⇆]{i..j} -> [h→]{i..j}
                 *      RIGHT ATTACHMENT
                 *          [h→]{i..k} -> [h→]{i..j} + [a-]{j..k}
                 */
                val valence = if (h == j) Valence.F else Valence.T
                for (k in j..(sentence.size - 1)) {
                    if (k == j) {
                        value += outside(Seal.L_UNSEALED, h, i, j) *
                                params.stopProbs[h_tag][Dir.RIGHT][valence]
                    }
                    if (k > j) {
                        for (a in (j + 1)..k) {
                            val a_tag = tagToId[tags[a]]!!
                            if (k > j) {
                                value += outside(Seal.R_UNSEALED, h, i, k) *
                                        inside(Seal.SEALED, a, j, k) *
                                        params.chooseProbs[h_tag][Dir.RIGHT][a_tag] *
                                        (1 - params.stopProbs[h_tag][Dir.RIGHT][valence])
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
                                params.stopProbs[h_tag][Dir.LEFT][valence]
                    }
                    if (k < i) {
                        for (a in k..i - 1) {
                            val a_tag = tagToId[tags[a]]!!
                            value += outside(Seal.L_UNSEALED, h, k, j) *
                                    inside(Seal.SEALED, a, k, i - 1) *
                                    (1 - params.stopProbs[h_tag][Dir.LEFT][valence]) *
                                    params.chooseProbs[h_tag][Dir.LEFT][a_tag]
                        }
                    }
                }
            }
            Seal.SEALED -> {
                /**
                 * Two cases:
                 *      TERMINAL_NODE CASE
                 *          [h-]{$..^}
                 *      LEFT ATTACHMENT
                 *          [m⇆]{i..k} -> [h-]{i..j} + [m⇆]{j+1..k}
                 *      RIGHT ATTACHMENT
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
                // RIGHT
                if (i > 0)
                    for (k in 0..(i - 1)) {
                        val k_tag = tagToId[tags[k]]!!
                        val valence = if (k == i - 1) Valence.F else Valence.T
                        value += outside(Seal.R_UNSEALED, k, k, j) *
                                inside(Seal.SEALED, k, k, i - 1) *
                                (1 - params.stopProbs[k_tag][Dir.RIGHT][valence]) *
                                params.chooseProbs[k_tag][Dir.RIGHT][h_tag]
                    }
                // LEFT
                if (j < sentence.size - 1)
                    for (k in (j + 1)..(sentence.size - 1)) {
                        for (m in (j + 1)..k) {
                            val m_tag = tagToId[tags[m]]!!
                            val valence = if (m == j + 1) Valence.F else Valence.T
                            value += outside(Seal.L_UNSEALED, m, i, k) *
                                    inside(Seal.L_UNSEALED, m, j + 1, k) *
                                    (1 - params.stopProbs[m_tag][Dir.LEFT][valence]) *
                                    params.chooseProbs[m_tag][Dir.LEFT][h_tag]
                        }
                    }
            }
        }
        outsideTerms.quantities[h_seal][h][i][j] = value
        outsideTerms.flags[h_seal][h][i][j] = true
        return value
    }

    inside(Seal.SEALED, 0, 0, sentence.size - 1)

    return IOPair(insideTerms, outsideTerms)
}

// Given a sentence and the in/out-side terms, re-estimate the params
fun maximization(sentence: Sentence, ioPair: IOPair) {
    /**
     * Calculate the STOP probabilities
     */
    print(sentence)
    val tags = sentence.map { it.tag }
    for (entry in tagToId) {
        val tag = entry.key
        val tag_idx = entry.value
        for (sen_idx in 0..sentence.size - 1) {
            if (tags[sen_idx] == tag) {
                for (i in 0..sen_idx) {
                    for (j in sen_idx..sentence.size - 1){
                        nominator = 
                    }
                }
            }
        }
    }
    exitProcess(0)
}

// Given a sentence and the in/out-side terms, decode the tree and return the prediction
fun decode(sentence: Sentence, ioPair: IOPair): List<Int> {
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
                 *      LEFT SEALING:
                 *          [h-]{i..j} -> [h⇆]{i..j}
                 */
                val valence = if (h == i) Valence.F else Valence.T
                prob = params.stopProbs[h_tag][Dir.LEFT][valence] * maxSpanning(Seal.L_UNSEALED, h, i, j)
                tree.attach(parent,
                        STOP_NODE,
                        Node(Seal.L_UNSEALED, h, i, j))
            }
            Seal.L_UNSEALED -> {
                /**
                 * Two cases:
                 *      LEFT ATTACHMENT
                 *          [h⇆]{i..j} -> [a-]{i..k} + [h⇆]{k+1..j}
                 *      RIGHT SEALING
                 *          [h⇆]{i..j} -> [h→]{i..j}
                 */
                if (h == i) {
                    val valence = if (h == j) Valence.F else Valence.T
                    prob = params.stopProbs[h_tag][Dir.RIGHT][valence] * maxSpanning(Seal.R_UNSEALED, h, i, j)
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
                            val case_prob = (1 - params.stopProbs[h_tag][Dir.LEFT][valence]) *
                                    params.chooseProbs[h_tag][Dir.LEFT][a_tag] *
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
                 *      RIGHT ATTACHMENT
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
                            val case_prob = (1 - params.stopProbs[h_tag][Dir.RIGHT][valence]) *
                                    params.chooseProbs[h_tag][Dir.RIGHT][a_tag] *
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

    val prediction = Array1D(sentence.size, { -1 })
    search(node = Node(h_seal = Seal.SEALED, h = 0, i = 0, j = sentence.size - 1),
            left = false,
            prediction = prediction)
    return prediction.asList()
}


fun main(args: Array<String>) {
    val trainSentences = trainTreebank.sentences.filter { it.size == 10 }

    for (epochId in 0 until 10) {
        var totalAccu = Accuracy(0, 0)
        for (senId in 0 until trainSentences.size) {
            val sentence = trainSentences[senId]
            val ioPair = expectation(sentence = sentence)
            val newParam = maximization(ioPair = ioPair, sentence = sentence)
            val prediction = decode(ioPair = ioPair, sentence = sentence)
            val accu = computeAccuracy(sentence = sentence, prediction = prediction, directional = false)
            totalAccu += accu
        }
        println("epoch: $epochId, " +
                "total corr: ${totalAccu.corrNum}, " +
                "total num: ${totalAccu.totalNum}, " +
                "accu : ${(totalAccu.corrNum + 0.0) / totalAccu.totalNum}")
    }
}