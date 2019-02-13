package com.omarsmak.kafka.consumer.lag.monitoring

import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.concurrent.Callable


@Command(mixinStandardHelpOptions = true)
class CliOptions : Callable<Void> {

    @Option(names = [ "-a", "--algorithm" ], description = ["MD5, SHA-1, SHA-256, ..."], required = true)
    var algorithm = "SHA-1"

    override fun call(): Void? {
        println(algorithm)
        return null
    }

}