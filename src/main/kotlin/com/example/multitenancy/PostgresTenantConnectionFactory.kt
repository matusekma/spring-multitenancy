package com.example.multitenancy

import io.r2dbc.spi.ConnectionFactoryMetadata
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory
import reactor.core.publisher.Mono

class PostgresTenantConnectionFactory : AbstractRoutingConnectionFactory() {

    internal class PostgresqlConnectionFactoryMetadata private constructor() : ConnectionFactoryMetadata {
        override fun getName(): String {
            return NAME
        }

        companion object {
            val INSTANCE = PostgresqlConnectionFactoryMetadata()
            const val NAME = "PostgreSQL"
        }
    }

    override fun determineCurrentLookupKey(): Mono<Any> {
        return Mono
            .deferContextual { data ->
                Mono.just(data)
            }
            .filter { it.hasKey("tenantId") }
            .map { it.get("tenantId") }
    }

    override fun getMetadata(): ConnectionFactoryMetadata {
        // If we don't override this method, it will try to determine the Dialect from the default
        // ConnectionFactory. This is a problem, because you don't want a "Default ConnectionFactory"
        // when you cannot resolve the Tenant.
        //
        // That's why we explicitly return a fixed PostgresqlConnectionFactoryMetadata. This class
        // is also defined within the r2dbc library, but it isn't exposed to public.
        return PostgresqlConnectionFactoryMetadata.INSTANCE
    }

}
