package unclearPlatformType.kotlin

import unclearPlatformType.java.JavaLibSample0

fun funcInKotlin0(param:Int) {
//    val data: dynamic = "112"
    val union = if(param > 1) JavaLibSample0.intAInJava() else JavaLibSample0.intBInJava()
    val value = JavaLibSample0.funcInJava0()
    println(value)
}

interface InterfaceA {

}

interface InterfaceB {

}

class UseA : InterfaceA, InterfaceB {

}

class UseB : InterfaceA, InterfaceB {

}