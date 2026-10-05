package com.example.contactos.controller

import com.example.contactos.entity.User
import com.example.contactos.form.RegistrationForm
import com.example.contactos.repository.UserRepository
import com.example.contactos.security.AppUserDetailsService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class RegistrationController(
    private val users: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userDetailsService: AppUserDetailsService,
) {

    private val securityContextRepository = HttpSessionSecurityContextRepository()

    @GetMapping("/register")
    fun formulario(model: Model): String {
        model.addAttribute("registrationForm", RegistrationForm())
        return "registration/register"
    }

    @PostMapping("/register")
    fun registrar(
        @Valid @ModelAttribute("registrationForm") form: RegistrationForm,
        binding: BindingResult,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): String {
        val email = form.email.trim().lowercase()

        if (!binding.hasFieldErrors("email") && users.existsByEmail(email)) {
            binding.rejectValue("email", "email.duplicado", "There is already an account with this email")
        }
        if (binding.hasErrors()) {
            return "registration/register"
        }

        val user = User()
        user.email = email
        user.name = form.name.trim()
        user.password = requireNotNull(passwordEncoder.encode(form.plainPassword)) {
            "El codificador de contraseñas no ha devuelto ningún hash"
        }
        users.save(user)

        iniciarSesion(user.email, request, response)
        return "redirect:/"
    }

    private fun iniciarSesion(email: String, request: HttpServletRequest, response: HttpServletResponse) {
        val details = userDetailsService.loadUserByUsername(email)
        val authentication = UsernamePasswordAuthenticationToken.authenticated(details, null, details.authorities)

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)

        if (request.getSession(false) != null) {
            request.changeSessionId()
        }
        securityContextRepository.saveContext(context, request, response)
    }
}
