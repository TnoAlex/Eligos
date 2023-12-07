package toomanyparams.pkg0

import org.jetbrains.annotations.NotNull

class TooManyParams0 {
    fun funP0(
        vararg p0: String,
        @NotNull p1: Int,
        p2: Double,
        p3: List<Int>,
        p4: (String,Int)->Unit,
        p5: Int,
        p6: ArrayList<Int>,
        p7: Array<Int>
    ) {
        p6.add(p1)
        p7[0] = p3[3]
        if (p2 > p5) {
            println()
        }

    }
}