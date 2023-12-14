package optimizedTailRecursion

fun factorial0(n: Int, acc: Int = 1): Int {
    if (n != 0) {
        val t = n-1
        val sum = acc*n
        return factorial0(t, sum)
    }
    return acc + 1
}

fun factorial1(n: Int, acc: Int = 1): Int {
    if (n != 0) {
        return factorial1(n - 1, acc * n) + 2
    }
    return acc + 1
}

tailrec fun factorial2(n: Int, acc: Int = 1): Int {
    if (n == 0) {
        return acc + 1
    }
    return factorial2(n - 1, acc * n)
}

fun factorial3(n: Int, acc: Int = 1): Int {
    if (n != 0) {
        return factorial3(n - 1, acc * n) + factorial2(n + 1, acc)
    }
    return acc + 1
}

fun factorial4(n: Int, acc: Int = 1): Int {
    return if (n > 10) {
        factorial4(n - 1, acc * n)
    } else if (n < 10) {
        factorial4(n - 2, acc * n)
    } else if (n > 0 && n < 10) {
        factorial4(n - 3, acc * n)
    } else acc + 1
}

