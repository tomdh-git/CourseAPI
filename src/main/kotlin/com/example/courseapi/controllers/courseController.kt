package com.example.courseapi.controllers

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"]) // allow all origins
class CourseController() {
    @GetMapping("/alive")
    fun alive(): String { return "alive" }
}