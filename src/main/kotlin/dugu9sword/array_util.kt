package dugu9sword

typealias Array1D<T> = Array<T>
typealias Array2D<T> = Array<Array1D<T>>
typealias Array3D<T> = Array<Array2D<T>>
typealias Array4D<T> = Array<Array3D<T>>

typealias DoubleArray1D = DoubleArray
typealias DoubleArray2D = Array<DoubleArray1D>
typealias DoubleArray3D = Array<DoubleArray2D>
typealias DoubleArray4D = Array<DoubleArray3D>

typealias BooleanArray4D = Array<Array<Array<BooleanArray>>>

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


// 1d double array
fun doubleArrayAs(d1: Int, init: (Int) -> Double): DoubleArray1D {
    return DoubleArray(d1, init)
}

// 2d double array
fun doubleArrayAs(d1: Int, d2: Int, init: (Int) -> Double): DoubleArray2D {
    return arrayAs(d1) {
        DoubleArray(d2, init)
    }
}

// 3d double array
fun doubleArrayAs(d1: Int, d2: Int, d3: Int, init: (Int) -> Double): DoubleArray3D {
    return arrayAs(d1, d2) {
        DoubleArray(d3, init)
    }
}

// 4d double array
fun doubleArrayAs(d1: Int, d2: Int, d3: Int, d4: Int, init: (Int) -> Double): DoubleArray4D {
    return arrayAs(d1, d2, d3) {
        DoubleArray(d4, init)
    }
}

// 4d boolean array
fun booleanArrayAs(d1: Int, d2: Int, d3: Int, d4: Int, init: (Int) -> Boolean): BooleanArray4D {
    return arrayAs(d1, d2, d3) {
        BooleanArray(d4, init)
    }
}


fun normalizeDoubleArray_(doubleArray3D: DoubleArray3D) {
    for (i in 0 until doubleArray3D.size)
        for (j in 0 until doubleArray3D[0].size) {
            val sum = doubleArray3D[i][j].sum() + eps
            for (k in 0 until doubleArray3D[0][0].size)
                doubleArray3D[i][j][k] = doubleArray3D[i][j][k] / sum
        }
}