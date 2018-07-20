package dugu9sword.dmv

import dugu9sword.*
import java.util.HashMap
import org.nd4j.linalg.factory.Nd4j as nj


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

// directions
object Dir {
    const val L = 0
    const val R = 1

    const val size = 2
}


// valence
object Valence {
    const val F = 0
    const val T = 1

    const val size = 2
}

class IOTerm(val sentenceSize: Int) {
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


// treebanks
val trainTreeBank = TreeBank("dataset/wsj_develop.txt", 1, 3, 6, 7)
val testTreeBank = TreeBank("dataset/wsj_test.txt", 1, 3, 6, 7)


// tags
val tagToId = trainTreeBank.tagDict
val idToTag = tagToId.map { it.value to it.key }
val tagNum = tagToId.size


// params of the model
class Params {
    val chooseProbs = initChooseProbs()
    val stopProbs = initStopProbs()

    private fun initStopProbs(): Array3D<Double> {
        return arrayAs(tagToId.size, Dir.size, Valence.size) { Math.random() }
    }

    private fun initChooseProbs(): Array3D<Double> {
        fun _sumToOneArray(dim: Int): Array<Double> {
            val array = arrayAs(dim) { Math.random() }
            val sum = array.reduce { a, b -> a + b }
            return array.map { it / sum }.toTypedArray()
        }
        return arrayAs(tagToId.size, Dir.size) { _sumToOneArray(tagToId.size) }
    }
}

class Count {
    val chooseCases = doubleArrayAs(tagNum, Dir.size, tagNum) { 1.0 }
    val decideToStopCases = doubleArrayAs(tagNum, Dir.size, Valence.size) { 0.0 }
    val whetherToStopCases = doubleArrayAs(tagNum, Dir.size, Valence.size) { 0.0 }

    operator fun plus(other: Count): Count {
        val sumCount = Count()
        for (i in 0 until tagNum)
            for (j in 0 until Dir.size)
                for (k in 0 until tagNum)
                    sumCount.chooseCases[i][j][k] = this.chooseCases[i][j][k] + other.chooseCases[i][j][k]
        for (i in 0 until tagNum)
            for (j in 0 until Dir.size)
                for (k in 0 until Valence.size) {
                    sumCount.decideToStopCases[i][j][k] = this.decideToStopCases[i][j][k] + other.decideToStopCases[i][j][k]
                    sumCount.whetherToStopCases[i][j][k] = this.whetherToStopCases[i][j][k] + other.whetherToStopCases[i][j][k]
                }
        return sumCount
    }
}

fun countToParams(count: Count): Params {
    throw NotImplementedError()
}