package javabaseinfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;

import java.util.Iterator;

@AfterAll
class Test{
    static {
        final class InStatic{

        }
    }
    {

    }

    Iterable it = new Iterable() {
        @NotNull
        @Override
        public Iterator iterator() {
            return null;
        }
    };
    @Nullable
    private String inTestFunc(String s1,@Nullable int ... args){
        protected class Infunc{

        }
    }
}