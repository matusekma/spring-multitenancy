package com.example.multitenancy.repository.admin

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TenantRepository  : CoroutineCrudRepository<Tenant, Int>

@Table("tenants")
data class Tenant(@Id val id: Int, val name: String)