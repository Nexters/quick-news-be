package com.nexters.admin.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class HealthCheckController {
    @GetMapping
    fun healthCheck(): ResponseEntity<Map<String, String>> = ResponseEntity.ok(mapOf("status" to "UP"))
}
