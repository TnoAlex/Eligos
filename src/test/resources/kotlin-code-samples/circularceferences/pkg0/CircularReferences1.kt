package circularceferences.pkg0

class CircularReferences1 {
    val c0 = CircularReferences0()
    fun funcC1(){
        println("C1 func")
        c0.funC0()
    }
}