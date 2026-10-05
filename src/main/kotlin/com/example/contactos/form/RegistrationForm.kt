package com.example.contactos.form

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class RegistrationForm {

    @field:NotBlank(message = "Please enter your name")
    @field:Size(max = 255, message = "Your name is too long")
    var name: String = ""

    @field:NotBlank(message = "Please enter an email")
    @field:Email(message = "Please enter a valid email")
    @field:Size(max = 180, message = "Your email is too long")
    var email: String = ""

    @field:Size(min = 6, max = 4096, message = "Your password must be between {min} and {max} characters long")
    var plainPassword: String = ""

    @field:AssertTrue(message = "You should agree to our terms.")
    var agreeTerms: Boolean = false
}
