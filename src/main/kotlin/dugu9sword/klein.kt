package dugu9sword


fun init_stop_probs(): Array3D<Double> {
    return arrayAs(tag_to_id.size, dir_num, val_num, { Math.random() })
}

fun init_choose_probs(): Array3D<Double> {
    fun _sumToOneArray(dim: Int): Array<Double> {
        val array = arrayAs(dim, { Math.random() })
        val sum = array.reduce({ a, b -> a + b })
        return array.map { it / sum }.toTypedArray()
    }
    return arrayAs(tag_to_id.size, dir_num, { it -> _sumToOneArray(tag_to_id.size) })
}


fun main(args: Array<String>) {
    val choose_probs = init_choose_probs()
    val stop_probs = init_stop_probs()
    val sentence = test_treebank.sentences[0]
    val io = InsideOutside(choose_probs, stop_probs, sentence)
    io.inside(0, st_sealed, 0, sentence.size - 1)
    println(io.inside_quantities[0][st_sealed][0][sentence.size - 1])
    println(id_to_tag)
}