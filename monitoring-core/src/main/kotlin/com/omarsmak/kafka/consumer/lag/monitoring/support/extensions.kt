package com.omarsmak.kafka.consumer.lag.monitoring.support


fun Any.castToInt(): Int {
    if (this is String) return this.toInt()

    return (this as Number).toInt()
}

fun Any.castToLong(): Long {
    if (this is String) return this.toLong()

    return (this as Number).toLong()
}