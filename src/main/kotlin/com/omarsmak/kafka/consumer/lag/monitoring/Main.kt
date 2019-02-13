package com.omarsmak.kafka.consumer.lag.monitoring

import com.omarsmak.kafka.consumer.lag.monitoring.cli.ClientCli
import picocli.CommandLine

/**
 * Main entry for the program
 *
 * @author oalsafi
 * @since 2018-07-16
 */

fun main(arg: Array<String>) {
    CommandLine.call(ClientCli(), *arg)
}
