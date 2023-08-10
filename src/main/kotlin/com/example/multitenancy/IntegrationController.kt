package com.example.multitenancy

import com.example.multitenancy.repository.admin.Tenant
import com.example.multitenancy.repository.admin.TenantRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.await
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class IntegrationController(
    @Qualifier("adminR2dbcEntityOperations") val adminR2dbcEntityOperations: R2dbcEntityOperations,
    val adminTransactionManager: R2dbcTransactionManager,
    val tenantRepository: TenantRepository
) {

    data class TenantRequest(val name: String)

    @PostMapping("/integrations")
    suspend fun integrateTenant(@RequestBody tenantRequest: TenantRequest) {
        TransactionalOperator.create(adminTransactionManager).executeAndAwait {
            tenantRepository.save(Tenant(name = tenantRequest.name))
            adminR2dbcEntityOperations.databaseClient.sql(
                "CREATE SCHEMA if not exists ${tenantRequest.name}"
            ).await()



            adminR2dbcEntityOperations.databaseClient.sql("CREATE TABLE if not exists ${tenantRequest.name}.passes (id serial primary key , name text)")
                .await()
            adminR2dbcEntityOperations.databaseClient.sql("INSERT INTO ${tenantRequest.name}.passes (name) VALUES ('pass${tenantRequest.name}')")
                .await()
        }

    }
}