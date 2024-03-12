package whenInsteadOfCascadeIf

fun test(obj: Any) {
    if (obj is String){
        println("String")
    }else if(obj is Number){
        println("Number")
    }else if(obj is Boolean){
        println("Boolean")
    }else{
        println("Unknown")
    }
}