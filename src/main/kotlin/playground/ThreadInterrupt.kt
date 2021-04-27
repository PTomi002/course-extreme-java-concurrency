package playground

import java.util.concurrent.Phaser

fun main() {
    val worker = Thread {
        try {
            repeat(100) {
                println("Index is: $it")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    worker.start()
    // Only sets the interrupted flag true but the thread continues working.
    // sleep, wait, other blocking methods throws interrupted exception
    worker.interrupt()
    println("Worker is: ${worker.isInterrupted}")

    // Phaser
    val phaser = Phaser(1)
    repeat(10) {
        phaser.register()
        Thread {
            phaser.arriveAndAwaitAdvance()
        }.start()
    }
    phaser.arriveAndDeregister()
}