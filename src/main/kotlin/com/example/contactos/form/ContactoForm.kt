package com.example.contactos.form

import com.example.contactos.entity.Contacto
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class ContactoForm {

    @field:NotBlank(message = "El nombre no puede estar vacío")
    @field:Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    var nombre: String = ""

    @field:NotBlank(message = "El teléfono no puede estar vacío")
    @field:Size(max = 15, message = "El teléfono no puede superar los 15 caracteres")
    var telefono: String = ""

    @field:NotBlank(message = "El email no puede estar vacío")
    @field:Email(message = "El email no es válido")
    @field:Size(max = 255, message = "El email no puede superar los 255 caracteres")
    var email: String = ""

    var provinciaId: Long? = null
    var paisId: Long? = null

    companion object {
        fun desde(contacto: Contacto): ContactoForm {
            val form = ContactoForm()
            form.nombre = contacto.nombre
            form.telefono = contacto.telefono
            form.email = contacto.email
            form.provinciaId = contacto.provincia?.id
            form.paisId = contacto.pais?.id
            return form
        }
    }
}
