package nonJVMStaticCompanionFunction

class Test {
    companion object {
        fun test(): String {
            return "11"
        }

        @JvmStatic
        fun test1(): String {
            return "11"
        }
    }
}