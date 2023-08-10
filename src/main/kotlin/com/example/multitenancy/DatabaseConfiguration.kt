package com.example.multitenancy

import com.example.multitenancy.repository.admin.TenantRepository
import io.r2dbc.pool.PoolingConnectionFactoryProvider
import io.r2dbc.pool.PoolingConnectionFactoryProvider.MAX_SIZE
import io.r2dbc.pool.PoolingConnectionFactoryProvider.MIN_IDLE
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.SCHEMA
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.*
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import java.util.concurrent.ConcurrentHashMap


@Configuration
@EnableR2dbcRepositories(
    basePackages = ["com.example.multitenancy.repository.admin"],
    entityOperationsRef = "adminR2dbcEntityOperations"
)
class AdminDatabaseConfiguration {

    @Bean
    fun adminConnectionFactory(): ConnectionFactory {
        return PoolingConnectionFactoryProvider().create(
            builder()
                .option(DRIVER, "pool")
                .option(PROTOCOL, "postgresql")
                .option(HOST, "localhost")
                .option(PORT, 5432)
                .option(USER, "wallet")
                .option(PASSWORD, "wallet")
                .option(DATABASE, "wallet")
                .option(SCHEMA, "admin")
                .option(MIN_IDLE, 1)
                .option(MAX_SIZE, 10)
                .build()
        )
    }


    @Bean
    fun adminR2dbcEntityTemplate(adminConnectionFactory: ConnectionFactory): R2dbcEntityTemplate {
        return R2dbcEntityTemplate(adminConnectionFactory)
    }

    @Bean
    fun adminTransactionManager(adminConnectionFactory: ConnectionFactory): R2dbcTransactionManager {
        return R2dbcTransactionManager(adminConnectionFactory)
    }

    @Bean
    fun adminR2dbcEntityOperations(@Qualifier("adminConnectionFactory") connectionFactory: ConnectionFactory): R2dbcEntityOperations {
        val databaseClient = DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .build()
        return R2dbcEntityTemplate(databaseClient, PostgresDialect.INSTANCE)
    }
}

@Configuration
@EnableR2dbcRepositories(
    basePackages = ["com.example.multitenancy.repository.main"],
    entityOperationsRef = "mainR2dbcEntityOperations"
)
class MainDatabaseConfiguration {

    @Bean
    fun mainConnectionFactory(
        tenantRepository: TenantRepository
    ): PostgresTenantConnectionFactory {
        val tenants = runBlocking {
            tenantRepository.findAll().map { it.name }.toList()
        }
        val tenantConnectionFactoryMap = tenants.associateWith { PostgresTenantConnectionFactory.createTenantConnectionFactory(it) }
        return PostgresTenantConnectionFactory(ConcurrentHashMap(tenantConnectionFactoryMap), tenantRepository)
    }

    @Bean
    fun mainR2dbcEntityOperations(@Qualifier("mainConnectionFactory") connectionFactory: ConnectionFactory): R2dbcEntityOperations {
        val databaseClient = DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .build()
        return R2dbcEntityTemplate(databaseClient, PostgresDialect.INSTANCE)
    }
}