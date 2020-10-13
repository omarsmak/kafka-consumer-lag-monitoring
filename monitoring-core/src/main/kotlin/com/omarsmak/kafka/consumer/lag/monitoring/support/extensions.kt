package com.omarsmak.kafka.consumer.lag.monitoring.support

import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL


fun Any.castToInt(): Int {
    if (this is String) return this.toInt()

    return (this as Number).toInt()
}

fun Any.castToLong(): Long {
    if (this is String) return this.toLong()

    return (this as Number).toLong()
}

fun String.asResource(): InputStream =
        object {}.javaClass.getResourceAsStream("/$this") ?: throw FileNotFoundException("File '$this' not found..")
