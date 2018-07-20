package dugu9sword

import dugu9sword.dmv.Count
import org.nd4j.linalg.util.ArrayUtil
import org.nd4j.linalg.factory.Nd4j as nj


fun main(args: Array<String>) {
    val count = Count()
    println(count.chooseCases)
    println(ArrayUtil.flattenDoubleArray(count.chooseCases))
}