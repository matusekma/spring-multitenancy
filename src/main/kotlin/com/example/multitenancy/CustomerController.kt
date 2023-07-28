package com.example.multitenancy

import com.example.multitenancy.repository.main.Pass
import com.example.multitenancy.repository.main.PassRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

@RestController
class PassController(val passRepository: PassRepository) {

    @GetMapping("/passes/{passId}")
    suspend fun getPass(
        @RequestParam tenantId: String,
        @PathVariable passId: Int
    ): Pass = passRepository.findById(passId) ?: throw RuntimeException("not found")
}
