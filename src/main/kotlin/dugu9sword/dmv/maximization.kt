package dugu9sword.dmv

import dugu9sword.eps


// Given the expected count, re-estimate the params
fun maximization(count: Count): Params {
    val params = Params()
    // update choose rules
    for (i in 0 until count.chooseCases.size)
        for (j in 0 until count.chooseCases[0].size) {
            val sum = count.chooseCases[i][j].sum() + eps
            for (k in 0 until count.chooseCases[0][0].size)
                params.chooseProbs[i][j][k] = count.chooseCases[i][j][k]/ sum
        }
    // update stop rules
    for (i in 0 until count.decideToStopCases.size)
        for (j in 0 until count.decideToStopCases[0].size)
            for (k in 0 until count.decideToStopCases[0][0].size)
                params.stopProbs[i][j][k] =
                        count.decideToStopCases[i][j][k] / (count.whetherToStopCases[i][j][k] + eps)
    return params
}

