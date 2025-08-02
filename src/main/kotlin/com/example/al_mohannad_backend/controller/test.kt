package com.example.al_mohannad_backend.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestController {

    @GetMapping
    fun test(): String {
        println("✅ تم الوصول إلى /test من العميل")
        return "Hello from Spring Boot 👋"
    }
}
