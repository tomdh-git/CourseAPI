package com.example.courseapi.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CourseController() {
    @GetMapping("/alive")
    fun alive(): String { return "alive" }
}