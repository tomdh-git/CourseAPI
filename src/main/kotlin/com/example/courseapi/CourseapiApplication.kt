package com.example.courseapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
@SpringBootApplication
class CourseapiApplication

fun main(args: Array<String>) {
    println("Running on port: " + System.getenv("PORT"));
    runApplication<CourseapiApplication>(*args)
}
