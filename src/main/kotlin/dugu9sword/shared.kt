package dugu9sword

import dugu9sword.treebank.TreeBank

// sealed types
object Seal {
    val R_UNSEALED = 0
    val L_UNSEALED = 1
    val SEALED = 2

    val size = 3
}

val seal_to_label = mapOf<Int, String>(
        Seal.R_UNSEALED to "→",
        Seal.L_UNSEALED to "⇆",
        Seal.SEALED to "-"
)
// treebanks
val train_treebank = TreeBank("dataset/wsj_develop.txt", 1, 3, 6, 7)
val test_treebank = TreeBank("dataset/wsj_test.txt", 1, 3, 6, 7)


// tags
val tag_to_id = train_treebank.tag_dict
val id_to_tag = tag_to_id.map { it.value to it.key }
val tag_num = tag_to_id.size

// directions
object Dir {
    val LEFT = 0
    val RIGHT = 1

    val size = 2

}


// valence
object Valence {
    val F = 0
    val T = 1

    val size = 2
}