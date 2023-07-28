package com.example.multitenancy

import io.r2dbc.spi.ConnectionFactoryMetadata
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory
import reactor.core.publisher.Mono


@SpringBootApplication
class MultitenancyApplication

fun main(args: Array<String>) {
    runApplication<MultitenancyApplication>(*args)
}



