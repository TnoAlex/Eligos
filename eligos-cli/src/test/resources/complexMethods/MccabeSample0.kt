package complexmethods


fun ccSample0() {
    for (i in 0..10) {
        println(i)
        if (i % 2 == 0 && (i != 2 || i!=4)) {
            println("$i!")
        } else if (i % 3 == 0 || i % 5 == 0) {
            println("..")
        } else {
            println("...")
        }
    }
}

fun ccSample1(p1: Int, p2: ArrayList<Int>) { //1
    val t = listOf(1, 2, 3, 4) as ArrayList<Int> //1
    when (p1) {
        10 -> {
            try {
                println(p2.last())
            } catch (e: Exception) {
                throw RuntimeException()
            }
        }

        11 -> {
            if (p2.size > 10) {
                while (t.isNotEmpty()) {
                    if (t[0] > 10) {
                        t[1]++
                        continue
                    } else {
                        t.removeAt(2)
                    }
                }
            }
        }

        7 -> {
            var t = p1
            loop@ while (t > 1) {
                while (t < 10) {
                    t++
                    break@loop
                }
            }
        }
    }
}

