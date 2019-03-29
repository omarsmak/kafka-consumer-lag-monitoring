@file:Suppress("MaximumLineLength", "MaxLineLength", "ImportOrdering")

package com.omarsmak.kafka.consumer.lag.monitoring.cli

import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClient
import com.omarsmak.kafka.consumer.lag.monitoring.client.KafkaConsumerLagClientFactory
import com.omarsmak.kafka.consumer.lag.monitoring.config.KafkaConsumerLagClientConfig
import mu.KotlinLogging
import org.apache.kafka.clients.admin.AdminClientConfig
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.*
import java.util.concurrent.Callable

@Command(mixinStandardHelpOptions = true)
class ClientCli : Callable<Void> {

    companion object {
        private val logger = KotlinLogging.logger {}

        const val DEFAULT_POLL_INTERVAL = KafkaConsumerLagClientConfig.DEFAULT_POLL_INTERVAL
        const val DEFAULT_HTTP_PORT = KafkaConsumerLagClientConfig.DEFAULT_HTTP_PORT
    }

    private val kafkaConsumerLagClient: KafkaConsumerLagClient by lazy {
        KafkaConsumerLagClientFactory.create(buildClientProp())
    }

    private enum class ClientModes(val mode: String) {
        CONSOLE("console"),
        PROMETHEUS("prometheus")
    }

    @Option(names = ["-m", "--mode"], description = ["Mode to run client, possible values 'console' or 'prometheus', default to 'console'"])
    var clientMode: String = ClientModes.CONSOLE.mode

    @Option(names = ["-b", "--bootstrap.servers"], description = ["A list of host/port pairs to use for establishing the initial connection to the Kafka cluster"], required = true)
    lateinit var kafkaBootstrapServers: String

    @Option(names = ["-c", "--consumer.groups"], description = ["A list of Kafka consumer groups or list ending with star (*) to fetch all consumers with matching pattern, e.g: 'test_v*'"], required = true)
    lateinit var kafkaConsumerClients: String

    @Option(names = ["-i", "--poll.interval"], description = ["Interval delay in ms to that refreshes the client lag metrics, default to 2000ms"])
    var pollInterval: Int = DEFAULT_POLL_INTERVAL

    @Option(names = ["-p", "-http.port"], description = ["Http port that is used to expose metrics in case prometheus mode is selected, default to 9000"])
    var httpPort: Int = DEFAULT_HTTP_PORT

    override fun call(): Void? {
        // Scrap the consumer groups
        val targetConsumerGroups = initializeConsumerGroups()

        // Load config
        val config = initializeConfigurations(targetConsumerGroups)

        // Initialize response view plugins
        val responseViewPlugins = Utils.loadResponseViewPlugins(kafkaConsumerLagClient, config)

        // Find our desired response view
        val responseView = responseViewPlugins.find { it.identifier() == clientMode }

        // Run our CLI
        if (responseView != null) {
            responseView.execute()
        } else {
            logger.error("Output mode `$clientMode` is not valid")
        }

        // Add the shutdown hook
        Runtime.getRuntime().apply {
            addShutdownHook(Thread(kafkaConsumerLagClient::close))
        }

        return null
    }

    private fun initializeConsumerGroups(): Set<String> {
        val configConsumerGroups = kafkaConsumerClients.split(",")
        return Utils.getTargetConsumerGroups(kafkaConsumerLagClient, configConsumerGroups)
    }

    private fun initializeConfigurations(targetConsumerGroups: Set<String>) = KafkaConsumerLagClientConfig.create(mapOf(
            KafkaConsumerLagClientConfig.BOOTSTRAP_SERVERS to kafkaBootstrapServers,
            KafkaConsumerLagClientConfig.HTTP_PORT to httpPort,
            KafkaConsumerLagClientConfig.POLL_INTERVAL to pollInterval,
            KafkaConsumerLagClientConfig.CONSUMER_GROUPS to targetConsumerGroups
    ))

    private fun buildClientProp(): Properties = Properties().apply {
        this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers
    }
}