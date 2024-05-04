package compareDataObjectWithReference


fun useDataObjectWithReference(){
    val dobject = DataObject
    val rdobject = createInstanceViaReflection()
    println(dobject == rdobject)
    println(rdobject === dobject)
}

fun createInstanceViaReflection(): DataObject {
    return (DataObject.javaClass.declaredConstructors[0].apply { isAccessible = true } as Constructor<DataObject>).newInstance()
}