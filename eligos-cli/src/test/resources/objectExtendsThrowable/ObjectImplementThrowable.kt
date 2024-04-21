package objectExtendsThrowable

import java.lang.RuntimeException

object ExtendsThrowable : RuntimeException() {

}


object NotExtendsThrowable {

}

class ClassExtendsThrowable : RuntimeException {

}