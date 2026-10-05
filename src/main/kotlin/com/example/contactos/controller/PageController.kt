package com.example.contactos.controller

import com.example.contactos.repository.ContactoRepository
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.security.Principal

@Controller
class PageController(
    private val contactos: ContactoRepository,
) {

    @GetMapping("/page", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun page(): String =
        """{"message":"Welcome to your new controller!","path":"PageController.kt"}"""

    @GetMapping("/")
    fun indice(model: Model, principal: Principal): String {
        model.addAttribute("contactos", contactos.findAll())
        model.addAttribute("username", principal.name)
        return "inicio"
    }
}
