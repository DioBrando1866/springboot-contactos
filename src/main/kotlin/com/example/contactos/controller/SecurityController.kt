package com.example.contactos.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.security.Principal

@Controller
class SecurityController {
    @GetMapping("/login")
    fun login(principal: Principal?, model: Model): String {
        model.addAttribute("username", principal?.name)
        return "security/login"
    }
}
