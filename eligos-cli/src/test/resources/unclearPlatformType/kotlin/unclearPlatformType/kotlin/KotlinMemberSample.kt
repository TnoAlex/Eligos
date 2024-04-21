package unclearPlatformType.kotlin

import unclearPlatformType.java.JavaLibSample0

val x = JavaLibSample0.funcInJava0()
val y: String
    get() = JavaLibSample0.funcInJava1.name()
class KotlinMemberSample {
    private val property0 = JavaLibSample0.funcInJava0()
    private val property1
        get() = JavaLibSample0.funcInJava0()

    fun test1(str: String) {

    }

    fun test() {
        val x = JavaLibSample0.funcInJava0()
        test1(x)
        if (x != null) {
            test1(x)
            test1(JavaLibSample0.funcInJava0())
            test1(JavaLibSample0.funcInJava0()!!)
        } else {
            test1(x)
            println("x is null")
        }
        test1(x)
    }

    fun inKotlin(){
        val x = JavaLibSample0.funcInJava1.name()
        test1(x)
    }
}

