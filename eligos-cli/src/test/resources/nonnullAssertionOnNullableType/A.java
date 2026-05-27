import org.jetbrains.annotations.Nullable;

public class A {
    // todo: KAA resolve this @Nullable incorrectly into default package
    //  instead of org.jetbrains.annotations package, which causes the test to fail
    @Nullable
    public static A func() {
        return null;
    }
}