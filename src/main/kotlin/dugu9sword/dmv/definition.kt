package dugu9sword.dmv

import dugu9sword.*
import java.util.HashMap
import org.nd4j.linalg.factory.Nd4j as nj


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
    const val F = 0
    const val T = 1

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
val trainTreeBank = TreeBank("dataset/wsj_develop.txt", 1, 3, 6, 7)
val testTreeBank = TreeBank("dataset/wsj_test.txt", 1, 3, 6, 7)


// Tags.
val tagToId = trainTreeBank.tagDict
val idToTag = tagToId.map { it.value to it.key }
val tagNum = tagToId.size


// Params of the model.
class Params {
    val chooseProbs = initChooseProbs()
    val stopProbs = initStopProbs()

    private fun initStopProbs(): DoubleArray3D {
        return doubleArrayAs(tagToId.size, Dir.size, Valence.size) { Math.random() }
    }

    private fun initChooseProbs(): DoubleArray3D {
        fun _sumToOneArray(dim: Int): DoubleArray {
            val array = DoubleArray(dim) { Math.random() }
            val sum = array.reduce { a, b -> a + b }
            return array.map { it / sum }.toDoubleArray()
        }
        return arrayAs(tagToId.size, Dir.size) { _sumToOneArray(tagToId.size) }
    }
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