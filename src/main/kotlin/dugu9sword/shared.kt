package dugu9sword

import dugu9sword.treebank.TreeBank

// sealed types
val st_unsealed = 0
val st_sealed = 1
val st_half_sealed = 2
val st_size = 3

// treebanks
val train_treebank = TreeBank("dataset/wsj_develop.txt", 1, 3, 6, 7)
val test_treebank = TreeBank("dataset/wsj_test.txt", 1, 3, 6, 7)


// tags
val tag_to_id = train_treebank.tag_dict
val id_to_tag = tag_to_id.map { it.value to it.key }
val tag_num = tag_to_id.size

// directions
val dir_left = 0
val dir_right = 1
val dir_num = 2


val val_num = 2