package unclearPlatformType.java;

import unclearPlatformType.kotlin.InterfaceA;
import unclearPlatformType.kotlin.InterfaceB;

import java.util.Random;

public class JavaLibSample0 {
    public static String funcInJava0() {
        Random random = new Random();
        if (random.nextInt() > 10) {
            return "11111";
        } else {
            return null;
        }
    }

    public static InterfaceA intAInJava(){
        return  new AInJava();
    }

    public static InterfaceB intBInJava(){
        return new BInJava();
    }
}

class AInJava implements InterfaceA, InterfaceB{

}

class BInJava implements InterfaceA,InterfaceB{

}