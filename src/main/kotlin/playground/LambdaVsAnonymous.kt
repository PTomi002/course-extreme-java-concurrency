package playground

import java.io.Serializable

class LambdaVsAnonymous {
    fun foo() {
        // # 1 context of this
        val r1 = object : Runnable {
            override fun run() {
                // this refers to the anonymous object
                println("Running: $this")
            }
        }
        // this refers to the closing object
        val r2 = Runnable { println("Running: $this") }

        r1.run()
        r2.run()
    }
}

fun main() {
    LambdaVsAnonymous().foo()

    // 2# Not all () -> creates new objects (based on hashcode)
    repeat(3) {
        val r1 = object : Runnable {
            override fun run() {
                println("Running")
            }
        }
        val r2 = Runnable { println("Running") }
        val r3 = Runnable { println("Running: $r1") }

        println("Job: $r1")
        println("lJob: $r2")
        println("otherLJob: $r3")
    }

    // 3# Anonymous obj. can implement more methods
    // 4# Anonymous obj. can extend classes
    val r1 = object : Runnable, Serializable, Cloneable {

        // 5# Anonymous can have state
        private val str = "state holder"

        override fun run() {
            println("")
        }
    }
    println("${r1.javaClass.interfaces.toList()}")

}

