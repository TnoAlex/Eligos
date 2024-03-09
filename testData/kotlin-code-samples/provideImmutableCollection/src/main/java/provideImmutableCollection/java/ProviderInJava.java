package provideImmutableCollection.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProviderInJava {
    public static List<String> pInJava() {
        return Collections.unmodifiableList(List.of("1","2","3"));
    }

    public static List<String> pmInJava() {
        return List.of();
    }
}
