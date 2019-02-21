package com.omarsmak.kafka.consumer.lag.monitoring.outputs

import java.util.Properties

/**
 * Interface for [Output] public API
 *
 * @author oalsafi
 */

interface Output {

    /**
     * Configure an output task with instance of [Properties],
     * this will be called upon initializing the client
     */
    fun configure(properties: Properties)

    /**
     * Execute the output task after the Output being initialized
     */
    fun excute()
}