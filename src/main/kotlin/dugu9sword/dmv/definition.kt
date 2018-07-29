package dugu9sword.dmv

import dugu9sword.*
import java.util.HashMap


/**
 * This file defines several terms in the dependency model.
 */

// Seal types.
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

// Directions.
object Dir {
    const val L = 0
    const val R = 1

    const val size = 2
}


// Valences.
object Valence {
    const val ADJ = 0
    const val NON_ADJ = 1

    const val size = 2
}

// Inside-outside potential terms.
class PotentialTerm(val sentenceSize: Int) {
    val quantities = doubleArrayAs(
            Seal.size,      // seal type
            sentenceSize,   // h = location of head word
            sentenceSize,   // i
            sentenceSize    // j
    ) { 0.0 }
    val flags = booleanArrayAs(
            Seal.size,      // seal type
            sentenceSize,   // h = location of head word
            sentenceSize,   // i
            sentenceSize    // j
    ) { false }
}


data class IOPair(val insideTerm: PotentialTerm, val outsideTerm: PotentialTerm)

// Node definition.
data class Node(val h_seal: Int, val h: Int, val i: Int, val j: Int)

val STOP_NODE = Node(h_seal = -1, h = -1, i = -1, j = -1)
val TERMINAL_NODE = Node(h_seal = -2, h = -2, i = -2, j = -2)

// Tree definition.
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


// Tree banks.
val trainTreeBank = TreeBank("dataset/wsj_train.nop.txt")
val testTreeBank = TreeBank("dataset/wsj_test.nop.txt")


// Tags.
val tagToId = trainTreeBank.tagDict
val idToTag = tagToId.map { it.value to it.key }.toMap()
val tagNum = tagToId.size
val allTags = trainTreeBank.tagDict.map { it.key }
val rootId = tagToId[Special.ROOT]

// Params of the model.
class Params {
    val chooseProbs = doubleArrayAs(tagToId.size, Dir.size, tagToId.size) { 0.0 }
    val stopProbs = doubleArrayAs(tagToId.size, Dir.size, Valence.size) { 0.0 }
}

// Counts of rules.
class Count(
        val chooseCases: DoubleArray3D = doubleArrayAs(tagNum, Dir.size, tagNum) { 0.0 },
        val decideToStopCases: DoubleArray3D = doubleArrayAs(tagNum, Dir.size, Valence.size) { 0.0 },
        val whetherToStopCases: DoubleArray3D = doubleArrayAs(tagNum, Dir.size, Valence.size) { 0.0 }) {
    operator fun plus(other: Count): Count {
        return Count(
                chooseCases = sumDoubleArray3D(this.chooseCases, other.chooseCases),
                decideToStopCases = sumDoubleArray3D(this.decideToStopCases, other.decideToStopCases),
                whetherToStopCases = sumDoubleArray3D(this.whetherToStopCases, other.whetherToStopCases))
    }
}


fun view(chooseItems: DoubleArray3D, tag: String, dir: Char = 'L', argTags: List<String>): String {
    val idx = tagToId[tag]!!
    val argTagIdxes = argTags.map { tagToId[it] }
    val direction = if (dir == 'L') Dir.L else Dir.R
    val values = argTagIdxes.map { chooseItems[idx][direction][it!!] }
    return argTagIdxes.zip(values)
            .sortedByDescending { it.second }
            .map { "$tag $dir -> ${idToTag[it.first]}: ${it.second}\n" }
            .reduce { a, b -> a + b }.toString()
}

fun view(stopItems: DoubleArray3D, tag: String): String {
    val idx = tagToId[tag]!!

    return "$tag, L, NON-ADJ: ${stopItems[idx][Dir.L][Valence.NON_ADJ]}\n" +
            "$tag, R, NON-ADJ: ${stopItems[idx][Dir.R][Valence.NON_ADJ]}\n" +
            "$tag, L, ADJ: ${stopItems[idx][Dir.L][Valence.ADJ]}\n" +
            "$tag, R, ADJ: ${stopItems[idx][Dir.R][Valence.ADJ]}\n"
}

fun sumDoubleArray3D(array1: DoubleArray3D, array2: DoubleArray3D): DoubleArray3D {
    val shape0 = array1.size
    val shape1 = array1[0].size
    val shape2 = array1[0][0].size
    val sum = doubleArrayAs(shape0, shape1, shape2) { 0.0 }
    for (i in 0 until shape0)
        for (j in 0 until shape1)
            for (k in 0 until shape2)
                sum[i][j][k] = array1[i][j][k] + array2[i][j][k]
    return sum
}

val dbg = Debugger("log.txt")