package circularceferences.pkg0

internal class CircularReferences0 {

    fun funC0(){
        val c1 = CircularReferences1()
        c1.funcC1()
        println("C0 fun")
    }
}