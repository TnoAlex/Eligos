package provideImmutableCollection.java;

import provideImmutableCollection.kotlin.ProviderInKotlinKt;

public class UseInJava {
    public void uInJava(){
        var list = ProviderInKotlinKt.pInKotlin();
        System.out.println(list);
    }
}
