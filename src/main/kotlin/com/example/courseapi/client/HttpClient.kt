package com.example.courseapi.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.cookies.HttpCookies
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpClientConfig {
    @Bean
    fun httpClient(): HttpClient = HttpClient(CIO) {
        install(HttpCookies)
        expectSuccess = false
        followRedirects = true
        engine {
            requestTimeout = 10000
            endpoint {
                keepAliveTime = 60000
                connectTimeout = 5000
            }
        }
    }
}