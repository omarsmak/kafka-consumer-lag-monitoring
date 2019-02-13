@file:Suppress("MaximumLineLength", "MaxLineLength", "ImportOrdering")

package com.omarsmak.kafka.consumer.lag.monitoring.cli

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory
import com.omarsmak.kafka.consumer.lag.monitoring.output.Console
import com.omarsmak.kafka.consumer.lag.monitoring.output.Prometheus
import mu.KotlinLogging
import org.apache.kafka.clients.admin.AdminClientConfig
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.Properties
import java.util.concurrent.Callable

private val logger = KotlinLogging.logger {}

@Command(mixinStandardHelpOptions = true)
class ClientCli : Callable<Void> {

    companion object {
        const val DEFAULT_POLL_INTERVAL = 2000
        const val DEFAULT_HTTP_PORT = 9000
        const val DEFAULT_CLIENT_TYPE = "java"
        const val DEFAULT_LAG_THRESHOLD = 500
    }

    private enum class ClientModes(val mode: String) {
        CONSOLE("console"),
        PROMETHEUS("prometheus")
    }

    @Option(names = ["-m", "--mode"], description = ["Mode to run client, possible values 'console' or 'prometheus"])
    var clientMode: String = ClientModes.CONSOLE.mode

    @Option(names = ["-b", "--bootstrap.servers"], description = ["A list of host/port pairs to use for establishing the initial connection to the Kafka cluster"], required = true)
    lateinit var kafkaBootstrapServers: String

    @Option(names = ["-c", "--consumer.groups"], description = ["A list of Kafka consumer groups or list ending with regex, e.g: 'test_v*"], required = true)
    lateinit var kafkaConsumerClients: String

    @Option(names = ["-i", "--poll.interval"], description = ["Interval delay in ms to that refreshes the client lag metrics, default to 2000ms"])
    var pollInterval: Int = DEFAULT_POLL_INTERVAL

    @Option(names = ["-p", "-http.port"], description = ["Http port that is used to expose metrics in case prometheus mode is selected, default to 9000"])
    var httpPort: Int = DEFAULT_HTTP_PORT

    override fun call(): Void? {
        // Load config
        val configConsumerGroups = kafkaConsumerClients.split(",")

        // Create client
        val kafkaConsumerLagClient = KafkaConsumerLagClientFactory.getClient(DEFAULT_CLIENT_TYPE, buildClientProp())

        // Scrap the consumer groups
        val targetConsumerGroups = Utils.getTargetConsumerGroups(kafkaConsumerLagClient, configConsumerGroups)

        when (clientMode) {
            ClientModes.CONSOLE.mode -> initializeConsoleMode(kafkaConsumerLagClient, targetConsumerGroups)
            ClientModes.PROMETHEUS.mode -> initializePrometheusMode(kafkaConsumerLagClient, targetConsumerGroups, httpPort)
            else -> logger.error("Output mode `$clientMode` is not valid")
        }

        return null
    }

    private fun buildClientProp(): Properties = Properties().apply {
        this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers
    }

    private fun initializeConsoleMode(kafkaConsumerLagClient: KafkaConsumerLagClient, consumers: Set<String>) {
        Console(kafkaConsumerLagClient, pollInterval, DEFAULT_LAG_THRESHOLD).show(consumers)
    }

    private fun initializePrometheusMode(kafkaConsumerLagClient: KafkaConsumerLagClient, consumers: Set<String>, httpPort: Int) {
        Prometheus(kafkaConsumerLagClient, pollInterval).initialize(consumers, httpPort)
    }
}