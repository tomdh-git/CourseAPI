package com.example.courseapi.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.HttpTimeout
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpClientConfig {
    @Bean
    fun httpClient(): HttpClient = HttpClient(CIO) {
        install(HttpCookies)
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
            connectTimeoutMillis = 30_000
        }
        expectSuccess = false
        followRedirects = true
        engine {
            maxConnectionsCount = 100
            endpoint {
                maxConnectionsPerRoute = 20
                pipelineMaxSize = 20
                keepAliveTime = 60000
                connectTimeout = 5000
                connectAttempts = 3
            }
            https {
                trustManager = null
            }
        }
    }
}