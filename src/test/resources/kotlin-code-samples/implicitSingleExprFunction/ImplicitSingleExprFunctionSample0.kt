package implicitSingleExprFunction

fun test0() = String.valueOf("12.3")
fun test1():String = String.valueOf("12.3")

fun test2(): String {
    return String("aaa")
}
