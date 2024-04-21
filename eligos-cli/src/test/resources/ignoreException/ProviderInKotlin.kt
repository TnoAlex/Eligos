package ignoreException

import java.io.IOException


fun inKotlin(parm:Int): String {
    if (parm > 10) return parm.toString()
    throw IOException()
}

@Throws(IOException::class)
fun inKotlinThrow(){
    throw IOException("")
}

class MyException : Exception(){

}