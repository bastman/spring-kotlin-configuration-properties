package com.example.demo.rest

import com.example.demo.util.spring.binder.jmespath
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiController(
        private val env: Environment
) {
    @GetMapping("/api/environment")
    fun environment(@RequestParam jq: String): JqResponse {
        val data: Any? = env.jmespath(jq)
        return JqResponse(data = data)
    }

}

data class JqResponse(val data: Any?)