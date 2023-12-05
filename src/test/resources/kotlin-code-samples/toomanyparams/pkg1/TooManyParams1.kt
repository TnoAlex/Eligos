package `kotlin-code-samples`.toomanyparams.pkg1

typealias Person = (String, Int) -> Unit
fun funP1(
    p0: String,
    p1: Int,
    p2: Double,
    p3: List<Int>,
    p4: HashMap<String, String>,
    p5: Int,
    p6: ArrayList<Int>,
    p7: Person,
) {
    p6.add(p1)
    p4[p0] = "111"
    if (p2 > p5) {
        println()
    }

}