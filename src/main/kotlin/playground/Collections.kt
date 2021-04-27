package playground

import java.math.BigInteger
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

fun main() {

    // thread-safe, sync on 'this'
    val col1: Collection<Int> = Vector<Int>()
    val col2: Collection<Int> = Stack<Int>()
    // sync on 'this'
    val col3: Collection<Int> = Collections.synchronizedList(ArrayList())
    val col5: Map<String, Int> = Collections.synchronizedMap(LinkedHashMap())
    // subset of map uses the same mutex as the parent!
    val subCol1 = col5.keys

    // live lock test
    val fibo = LiveLockFibonacci()
    val fibo100 = fibo.apply(100)
    println("Result is: $fibo100")
}

// extra sync is needed for these kind of operations in case of syncXYZ collections
fun <T> MutableCollection<T>.addIfAbsent(element: T) =
    synchronized(this) {
        if (!contains(element)) add(element)
        else false
    }

// live lock with computeIfAbsent way...
class LiveLockFibonacci : Function<Int, BigInteger> {
    override fun apply(t: Int): BigInteger {
//        val cache = HashMap<Int, BigInteger>() // concurrent exc
        val cache = ConcurrentHashMap<Int, BigInteger>() // with Java 1.8 live lock happens, with version > Java 1.8 it gives recursive update
        cache[0] = BigInteger.ZERO
        cache[1] = BigInteger.ONE
        return apply(t, cache)
    }

    private fun apply(t: Int, cache: MutableMap<Int, BigInteger>): BigInteger {
//        var result = cache[t]
        // older idea suggested to replace it with compute if absent
//        if (result == null) {
//            result = apply(t - 1, cache).add(apply(t - 2, cache))
//            cache[t] = result
//        }
//        return result!!
        return cache.computeIfAbsent(t) { apply(it - 1, cache).add(apply(it - 2, cache)) }
    }
}

