# Code Smell Examples
This document presents examples of all code smells that Eligos can fix and 
explains some false positive cases.

## Circular References
Circular references at class granularity.
```java
class A {
    public void func(B b) {}
    //               ^
    // class A uses class B here.
}
class B {
    A a;
//  ^
// class B uses class A here.
}
```
Similar situations may occur between more than two classes, whose programming languages are not limited.

## Provide Immutable Collection
Kotlin provides an immutable collection for Java, while Java does not distinguish the variability of collections in syntax.
We report such situation as a code smell.

For more information, see [test cases](../eligos-cli/src/test/resources/provideImmutableCollection)
```kotlin
// ProviderInKotlin.kt
fun pInKotlin(): List<String> {
    //           ^^^^^^^^^^^^
    // Kotlin function provides an immutable collection here.
    return listOf("1", "2", "3")
}
```
```java
// UseInJava.java
public class UseInJava {
    public void uInJava(){
        var list = ProviderInKotlinKt.pInKotlin();
        //         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        // Java uses the collection here.
        System.out.println(list);
    }
}
```

## Internal Exposed
Java expose Kotlin internal declaration to public.
Due to the lack of internal level access permissions in the JVM, 
Kotlin's internal level access permissions can only be guaranteed 
by the Kotlin compiler itself. 
The Java compiler may expose internal level Kotlin classes in certain 
situations, which is a compilation error in Kotlin.
### Java extends or implements Kotlin internal
```kotlin
internal open class KotlinInternal
```
```java
public class JavaClass extends KotlinInternal {
    //                         ^^^^^^^^^^^^^^
    // Java class extends Kotlin internal class and exposes it to public
}
```
### Java return Kotlin internal
```kotlin
internal class KotlinInternal
```
```java
public class JavaClass {
    public KotlinInternal func() {}
    //     ^^^^^^^^^^^^^^
    // Java method returns a Kotlin internal class which exposes it to public
}
```
### Java take Kotlin internal as parameter
```kotlin
internal class KotlinInternal
```
```java
public class JavaClass {
    public void func(KotlinInternal kotlinInternal) {}
    //               ^^^^^^^^^^^^^^
    // Java method takes a Kotlin internal class as parameter which exposes it to public
}
```

## Uncertain Nullable Platform Expression Usage
Pass a platform type expression to a non-null type parameter in Kotlin.
To avoid false positive cases, add a @NotNull annotation on Java method 
or use non-null assertion (!!) / null safe operator (?. or ?:) in Kotlin.
```java
public class JavaClass {
    public static String func() {
        //        ^^^^^^
        // the return type is platform type in Kotlin
        // ... do something
    }
    public static @NotNull String func1() {
        // the return type here is non-null type in kotlin
        // ... do something
    }
    public static @Nullable String func2() {
        // the return type here is nullable type in kotlin
        // ... do something
    }
}
```
```kotlin
fun test1(str: String) {
    //         ^^^^^^
    // need a non-null String
}
fun test() {
    test1(JavaClass.func())
    //    ^^^^^^^^^^^^^^^^
    // pass platform value into non-null type parameter here.
    
    test1(JavaClass.func()!!)
    // as there has non-null assertion (!!), no code smell will be report.
    
    JavaClass.func()?.let { test1(it) }
    // as there has null safe operator (?.), no code smell will be report.
    
    test1(JavaClass.func1())
    // as type of "JavaClass.func1()" is not platform type, no code smell will be report.
    
    test1(JavaClass.func2())
    // as there is a compilation error here, no code smell will be report.
    // Argument type mismatch: actual type is 'String?', but 'String' was expected.
}
```

## Uncertain Nullable Platform Caller
Appear at a caller whose type is platform type in Kotlin.
To avoid false positive cases, add a @NotNull annotation on Java method
or use non-null assertion (!!) / null safe operator (?. or ?:) in Kotlin.
```java
public class JavaClass {
    public static KtClass func() {
        //        ^^^^^^^
        // the return type is platform type in Kotlin
        // ... do something
    }
}
```
```kotlin
class KtClass {
    fun funcKt() {}
}

fun main() {
    JavaClass.func().funcKt()
//  ^^^^^^^^^^^^^^^^
//  the type of caller "JavaClass.func()" is platform type in Kotlin.
}
```

## Nullable Passed To Platform Parameter
Passed a nullable value to platform parameter.
To avoid false positive cases, add a @NotNull annotation on Java method
or use non-null assertion (!!) / null safe operator (?. or ?:) in Kotlin.
```java
public class JavaClass {
    public static void func(String str) {
        //                  ^^^^^^
        // platform parameter here.
        // ... do something
    }
}
```
```kotlin
fun main() {
    val a: String? = null
    func(a)
    //   ^
    // Passed a nullable value to platform parameter here.
}
```

## Uncertain Nullable Platform Type In Property
Using platform type expression as the value of a property and not declaring the type of the property.
To avoid false positive cases, add a @NotNull annotation on Java method, 
use non-null assertion (!!) / null safe operator (?. or ?:) in Kotlin or
declare the type of the property.
```java
public class JavaClass {
    public static String func() {
        //        ^^^^^^
        // the return type is platform type in Kotlin
        // ... do something
    }
}
```
```kotlin
class Foo {
    val x0 = JavaClass.func()
    //       ^^^^^^^^^^^^^^^^
    // Use platform type expression as the value of a property and not declaring the type here.
    
    val x1 = JavaClass.func()!!
    // as there has non-null assertion (!!), no code smell will be report.
    
    val x2 = JavaClass.func() ?: ""
    // as there has null safe operator (?:) and the final type is non-null String, no code smell will be report.
    
    val x3 = JavaClass.func() ?: JavaClass.func()
    //       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    // Although there has null safe operator (?:), but the final type is still platform type.
}
```

