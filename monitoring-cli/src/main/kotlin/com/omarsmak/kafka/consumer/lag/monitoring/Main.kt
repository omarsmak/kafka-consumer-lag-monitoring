package com.omarsmak.kafka.consumer.lag.monitoring

import com.omarsmak.kafka.consumer.lag.monitoring.cli.ClientCli
import picocli.CommandLine

/**
 * Main entry for the program
 *
 * @author oalsafi
 */

fun main(arg: Array<String>) {
    CommandLine(ClientCli()).execute(*arg)
}
