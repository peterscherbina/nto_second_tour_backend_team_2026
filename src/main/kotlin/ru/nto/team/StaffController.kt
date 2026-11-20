package ru.nto.team

import org.springframework.web.bind.annotation.*

@RestController
class StaffController(private val service: StaffService) {
    @GetMapping("/api/{code}/auth")
    fun profile(@PathVariable code: String) = service.profile(code)
}
