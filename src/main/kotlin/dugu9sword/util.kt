package dugu9sword

typealias Array1D<T> = Array<T>
typealias Array2D<T> = Array<Array<T>>
typealias Array3D<T> = Array<Array<Array<T>>>
typealias Array4D<T> = Array<Array<Array<Array<T>>>>
typealias Array5D<T> = Array<Array<Array<Array<Array<T>>>>>




// 1d array
inline fun <reified T> arrayAs(
        d1: Int,
        noinline init: (Int) -> T): Array1D<T> {
    return Array(d1, init)
}

inline fun <reified T1, reified T2> arrayAs(
        array: Array1D<T1>,
        noinline init: (Int) -> T2): Array1D<T2> {
    return arrayAs(array.size, init)
}

// 2d array
inline fun <reified T> arrayAs(
        d1: Int, d2: Int,
        noinline init: (Int) -> T): Array2D<T> {
    return Array(d1) { Array<T>(d2, init) }
}

inline fun <reified T1, reified T2> arrayAs(
        array: Array2D<T1>,
        noinline init: (Int) -> T2): Array2D<T2> {
    return arrayAs(array.size, array[0].size, init)
}

// 3d array
inline fun <reified T> arrayAs(
        d1: Int, d2: Int, d3: Int,
        noinline init: (Int) -> T): Array3D<T> {
    return Array(d1) { arrayAs(d2, d3, init) }
}

inline fun <reified T1, reified T2> arrayAs(
        array: Array3D<T1>,
        noinline init: (Int) -> T2): Array3D<T2> {
    return arrayAs(array.size, array[0].size, array[0][0].size, init)
}

// 4d array
inline fun <reified T> arrayAs(
        d1: Int, d2: Int, d3: Int, d4: Int,
        noinline init: (Int) -> T): Array4D<T> {
    return Array(d1) { arrayAs(d2, d3, d4, init) }
}

inline fun <reified T1, reified T2> arrayAs(
        array: Array4D<T1>,
        noinline init: (Int) -> T2): Array4D<T2> {
    return arrayAs(array.size, array[0].size, array[0][0].size, array[0][0][0].size, init)
}

// 5d array
inline fun <reified T> arrayAs(
        d1: Int, d2: Int, d3: Int, d4: Int, d5: Int,
        noinline init: (Int) -> T): Array5D<T> {
    return Array(d1) { arrayAs(d2, d3, d4, d5, init) }
}

inline fun <reified T1, reified T2> arrayAs(
        array: Array5D<T1>,
        noinline init: (Int) -> T2): Array5D<T2> {
    return arrayAs(array.size, array[0].size, array[0][0].size, array[0][0][0].size, array[0][0][0][0].size, init)
}