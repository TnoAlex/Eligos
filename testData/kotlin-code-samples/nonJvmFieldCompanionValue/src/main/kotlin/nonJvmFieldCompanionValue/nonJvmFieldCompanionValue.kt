package nonJvmFieldCompanionValue

class Test {
    companion object {
        const val str = "12323"
        val strts = "2222"

        @JvmField
        val ste = "ste"
    }
}