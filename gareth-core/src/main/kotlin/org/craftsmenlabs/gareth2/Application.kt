package org.craftsmenlabs.gareth2

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = arrayOf("org.craftsmenlabs.gareth2"))
open class Application {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }

        fun run(args: Array<String>): ConfigurableApplicationContext = SpringApplication.run(Application::class.java, *args)
    }
}

