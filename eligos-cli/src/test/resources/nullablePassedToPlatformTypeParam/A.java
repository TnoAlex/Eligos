import org.jetbrains.annotations.Nullable;

public class A {
    public int a = 1;
    public static void func(@Nullable A a) {
        System.out.println(a.a);
    }

    public static void func1(A a) {
        System.out.println(a.a);
    }
}