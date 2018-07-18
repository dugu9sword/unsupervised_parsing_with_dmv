package dugu9sword




fun main(args: Array<String>) {
    val x = mapOf<String, Int>(
            "a" to 0,
            "c" to 2,
            "b" to 1
    )
    for (ele in x){
        print(ele.key)
    }
}