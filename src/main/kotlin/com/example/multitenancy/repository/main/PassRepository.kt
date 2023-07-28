package com.example.multitenancy.repository.main

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PassRepository : CoroutineCrudRepository<Pass, Int>

@Table("passes")
data class Pass(@Id val id: Int, val name: String)