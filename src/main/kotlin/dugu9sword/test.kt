package dugu9sword

import dugu9sword.dmv.Count
import org.nd4j.linalg.util.ArrayUtil
import org.nd4j.linalg.factory.Nd4j as nj


fun main(args: Array<String>) {
    val count = Count()
    println(count.chooseCases)
    println(ArrayUtil.flattenDoubleArray(count.chooseCases))
//
//    FuckKotlin().da()
////    println(x)
////    println(arrayOf(count.chooseCases.size,count.chooseCases[0][0].size,count.chooseCases[0][0].size))
    val choo = nj.create(ArrayUtil.flattenDoubleArray(count.chooseCases),
            intArrayOf(count.chooseCases.size, count.chooseCases[0].size, count.chooseCases[0][0].size))
    val sum_choo = choo.sum(2)
    choo.divColumnVector(sum_choo)
}