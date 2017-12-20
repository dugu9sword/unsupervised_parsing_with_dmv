package dugu9sword

import dugu9sword.treebank.Sentence
import kotlin.math.abs
import kotlin.test.todo

class InsideOutside(
        val choose_probs: Array3D<Double>,
        val stop_probs: Array3D<Double>,
        val sentence: Sentence) {

    val tags = sentence.map { it.tag }

    val inside_quantities = arrayAs(
            sentence.size,
            st_size,
            sentence.size,
            sentence.size,
            { 0.0 }
    )
    val inside_flags = arrayAs(inside_quantities, { false })

    val outside_quantities = arrayAs(
            sentence.size,
            st_size,
            sentence.size,
            sentence.size,
            { 0.0 }
    )
    val outside_flags = arrayAs(outside_quantities, { false })


    fun adj(a: Int, b: Int): Int {
        if (abs(a - b) == 1)
            return 1
        else
            return 0
    }


    fun inside_if_not_flag(h_loc: Int,
                           st_id: Int,
                           i: Int,
                           j: Int) {
        todo {}
    }

    fun inside(h_loc: Int,
               st_id: Int,
               i: Int,
               j: Int) {
        assert(i < j)
        println("inside $h_loc, $st_id, $i, $j")
        val h_tag = tag_to_id[tags[h_loc]]!!
        if (j == i + 1) {
            inside_quantities[h_tag][st_id][i][j] = 1.0
        } else {
            when (st_id) {
                st_unsealed -> {
                    var sum = 0.0
                    for (k in i until j) {
                        if (!inside_flags[h_tag][st_unsealed][i][k])
                            inside(h_tag, st_unsealed, i, k)
                        for (a_tag in IntRange(0, tag_num)) {
                            if (!inside_flags[a_tag][st_sealed][k][j])
                                inside(a_tag, st_sealed, k, j)
                            sum += (1 - stop_probs[h_tag][dir_right][adj(h_loc, k)]) *
                                    choose_probs[h_tag][dir_right][a_tag] *
                                    inside_quantities[h_tag][st_unsealed][i][k] *
                                    inside_quantities[a_tag][st_sealed][k][j]
                        }
                    }
                    inside_quantities[h_tag][st_unsealed][i][j] = sum
                }
                st_sealed -> {
                    if (!inside_flags[h_tag][st_half_sealed][i][j])
                        inside(h_tag, st_half_sealed, i, j)
                    inside_quantities[h_tag][st_sealed][i][j] = stop_probs[h_tag][dir_left][adj(h_loc, i)] *
                            inside_quantities[h_tag][st_half_sealed][i][j]
                }
                st_half_sealed -> {
                    var sum = 0.0
                    for (k in i until j) {
                        if (!inside_flags[h_tag][st_unsealed][i][k])
                            inside(h_tag, st_unsealed, i, k)
                        for (a_tag in IntRange(0, tag_num)) {
                            if (!inside_flags[a_tag][st_sealed][k][j])
                                inside(a_tag, st_sealed, k, j)
                            sum += (1 - stop_probs[h_tag][dir_left][adj(h_loc, k)]) *
                                    choose_probs[h_tag][dir_left][a_tag] *
                                    inside_quantities[h_tag][st_unsealed][i][k] *
                                    inside_quantities[a_tag][st_sealed][k][j]
                        }
                    }
                    if (!inside_flags[h_tag][st_unsealed][i][j])
                        inside(h_tag, st_unsealed, i, j)
                    sum += stop_probs[h_tag][dir_right][adj(h_loc, j)] *
                            inside_quantities[h_tag][st_unsealed][i][j]
                    inside_quantities[h_tag][st_unsealed][i][j] = sum
                }

            }
        }
        inside_flags[h_tag][st_id][i][j] = true
    }

}