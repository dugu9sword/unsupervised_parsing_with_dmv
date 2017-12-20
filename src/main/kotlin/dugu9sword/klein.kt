package dugu9sword

fun init_stop_probs(): Array3D<Double> {
    return arrayAs(tag_to_id.size, Dir.size, Valence.size, { Math.random() })
}

fun init_choose_probs(): Array3D<Double> {
    fun _sumToOneArray(dim: Int): Array<Double> {
        val array = arrayAs(dim, { Math.random() })
        val sum = array.reduce({ a, b -> a + b })
        return array.map { it / sum }.toTypedArray()
    }
    return arrayAs(tag_to_id.size, Dir.size, { it -> _sumToOneArray(tag_to_id.size) })
}


fun main(args: Array<String>) {
    val choose_probs = init_choose_probs()
    val stop_probs = init_stop_probs()
    val train_sentences = train_treebank.sentences.filter { it.size == 10 }
    for (sen_id in 0..3) {
        val sentence = train_sentences[sen_id]
        val io = InsideOutside(choose_probs, stop_probs, sentence)
        io.inside(Seal.SEALED, 0, 0, sentence.size - 1)
        println(io.inside(Seal.SEALED, 0, 0, sentence.size - 1))
        println(io.inside(Seal.L_UNSEALED, 3, 3, 5) *
                io.outside(Seal.L_UNSEALED, 3, 3, 5))
        println("sentence: $sen_id, length: ${sentence.size}, inside: ${io.inside_times}")
    }
}