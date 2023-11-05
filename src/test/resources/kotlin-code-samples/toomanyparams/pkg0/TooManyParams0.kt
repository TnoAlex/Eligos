package `kotlin-code-samples`.toomanyparams.pkg0

class TooManyParams0 {
    fun funP0(
        p0: String,
        p1: Int,
        p2: Double,
        p3: List<Int>,
        p4: HashMap<String, String>,
        p5: Int,
        p6: ArrayList<Int>,
        p7: Array<Int>
    ) {
        p6.add(p1)
        p7[0] = p3[3]
        p4[p0] = "111"
        if (p2 > p5) {
            println()
        }

    }
}