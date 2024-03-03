package unclearPlatformType.java;

import java.util.Random;

class JavaLibSample0 {
    public static String funcInJava0() {
        Random random = new Random();
        if (random.nextInt() > 10) {
            return "11111";
        } else {
            return null;
        }
    }
}