package unclearPlantformCaller.kotlin

class Test {
    fun func() {
        val streamAllBytes = javaClass.getResourceAsStream("route_guide_db.json").readAllBytes()
    }
}
