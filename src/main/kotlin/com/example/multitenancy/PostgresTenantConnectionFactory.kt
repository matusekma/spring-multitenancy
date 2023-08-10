package com.example.multitenancy

import com.example.multitenancy.repository.admin.TenantRepository
import io.r2dbc.pool.PoolingConnectionFactoryProvider
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

class PostgresTenantConnectionFactory(
    private val connectionFactories: ConcurrentHashMap<String, ConnectionFactory>,
    private val tenantRepository: TenantRepository
) : ConnectionFactory{

    companion object {
        fun createTenantConnectionFactory(tenantName: String) =
            PoolingConnectionFactoryProvider().create(
                ConnectionFactoryOptions.builder()
                    .option(ConnectionFactoryOptions.DRIVER, "pool")
                    .option(ConnectionFactoryOptions.PROTOCOL, "postgresql")
                    .option(ConnectionFactoryOptions.HOST, "localhost")
                    .option(ConnectionFactoryOptions.PORT, 5432)
                    .option(ConnectionFactoryOptions.USER, "wallet")
                    .option(ConnectionFactoryOptions.PASSWORD, "wallet")
                    .option(ConnectionFactoryOptions.DATABASE, "wallet")
                    .option(PostgresqlConnectionFactoryProvider.SCHEMA, tenantName)
                    .option(PoolingConnectionFactoryProvider.MIN_IDLE, 1)
                    .option(PoolingConnectionFactoryProvider.MAX_SIZE, 10)
                    .build()
            )
    }

    internal class PostgresqlConnectionFactoryMetadata private constructor() : ConnectionFactoryMetadata {
        override fun getName(): String {
            return NAME
        }

        companion object {
            val INSTANCE = PostgresqlConnectionFactoryMetadata()
            const val NAME = "PostgreSQL"
        }
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

    override fun create(): Mono<Connection> {
        return mono { determineTargetConnectionFactory().create().awaitSingle() }
    }

    private suspend fun determineCurrentLookupKey(): String? {
        return Mono
            .deferContextual { data ->
                Mono.just(data)
            }
            .filter { it.hasKey("tenantId") }
            .mapNotNull { it.get("tenantId") as String? }
            .awaitSingle()
    }

    private suspend fun determineTargetConnectionFactory(): ConnectionFactory {
        val key = determineCurrentLookupKey() ?: throw IllegalStateException("No lookup key!")
        val connectionFactory = connectionFactories[key]
        return if (connectionFactory != null) {
            connectionFactory
        } else {
            val tenant = tenantRepository.findByName(key)
            if (tenant != null) {
                val newConnectionFactory = createTenantConnectionFactory(tenant.name)
                connectionFactories[tenant.name] = newConnectionFactory
                newConnectionFactory
            } else throw IllegalStateException("Tenant not found!")
        }
    }
}
