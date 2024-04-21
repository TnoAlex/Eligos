package ignoreException;

import java.io.IOException;

public class UseInJava {
    public static void main(String[] args) {
        String s = ProviderInKotlinKt.inKotlin(11);
    }

    public void useThrows() throws IOException {
        ProviderInKotlinKt.inKotlinThrow();
    }
}
