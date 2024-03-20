package ignoreException

import java.rmi.AccessException

fun inKotlin(parm:Int): String {
    if (parm > 10) return parm.toString()
    throw AccessException("")
}

class MyException : Exception(){

}