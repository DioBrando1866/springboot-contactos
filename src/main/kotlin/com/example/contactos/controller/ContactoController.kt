package com.example.contactos.controller

import com.example.contactos.entity.Contacto
import com.example.contactos.entity.Provincia
import com.example.contactos.form.ContactoForm
import com.example.contactos.repository.ContactoRepository
import com.example.contactos.repository.ProvinciaRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.server.ResponseStatusException

@Controller
class ContactoController(
    private val contactos: ContactoRepository,
    private val provincias: ProvinciaRepository,
) {
    @GetMapping("/contacto/nuevo")
    fun nuevo(model: Model): String {
        model.addAttribute("contactoForm", ContactoForm())
        model.addAttribute("provincias", provincias.findAllByOrderByNombreAsc())
        return "nuevo"
    }

    @PostMapping("/contacto/nuevo")
    fun guardarNuevo(
        @Valid @ModelAttribute("contactoForm") form: ContactoForm,
        binding: BindingResult,
        model: Model,
    ): String {
        val provincia = resolverProvincia(form, binding)
        if (binding.hasErrors()) {
            model.addAttribute("provincias", provincias.findAllByOrderByNombreAsc())
            return "nuevo"
        }

        val contacto = Contacto()
        copiarDatos(form, contacto, provincia)
        contactos.save(contacto)
        return "redirect:/"
    }

    @GetMapping("/contacto/{codigo}")
    fun ficha(@PathVariable("codigo") codigo: Long, model: Model): String {
        val contacto = contactos.findById(codigo)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado el contacto") }
        model.addAttribute("contacto", contacto)
        return "contacto"
    }

    @GetMapping("/contacto/editar/{codigo}")
    fun editar(@PathVariable("codigo") codigo: Long, model: Model): String {
        val contacto = contactos.findById(codigo).orElse(null) ?: return "contacto"

        model.addAttribute("codigo", codigo)
        model.addAttribute("contactoForm", ContactoForm.desde(contacto))
        model.addAttribute("provincias", provincias.findAllByOrderByNombreAsc())
        return "editar"
    }

    @PostMapping("/contacto/editar/{codigo}")
    fun guardarEdicion(
        @PathVariable("codigo") codigo: Long,
        @Valid @ModelAttribute("contactoForm") form: ContactoForm,
        binding: BindingResult,
        model: Model,
    ): String {
        val contacto = contactos.findById(codigo).orElse(null) ?: return "contacto"

        val provincia = resolverProvincia(form, binding)
        if (binding.hasErrors()) {
            model.addAttribute("codigo", codigo)
            model.addAttribute("provincias", provincias.findAllByOrderByNombreAsc())
            return "editar"
        }

        copiarDatos(form, contacto, provincia)
        contactos.save(contacto)
        return "redirect:/"
    }

    @PostMapping("/contacto/borrar/{codigo}")
    fun borrar(@PathVariable("codigo") codigo: Long): String {
        val contacto = contactos.findById(codigo)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado el contacto") }

        try {
            contactos.delete(contacto)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Error al borrar el contacto", e)
        }
        return "redirect:/"
    }

    private fun resolverProvincia(form: ContactoForm, binding: BindingResult): Provincia? {
        val id = form.provinciaId ?: return null
        val provincia = provincias.findById(id).orElse(null)
        if (provincia == null) {
            binding.rejectValue("provinciaId", "provincia.invalida", "La provincia seleccionada no existe")
        }
        return provincia
    }

    private fun copiarDatos(form: ContactoForm, contacto: Contacto, provincia: Provincia?) {
        contacto.nombre = form.nombre.trim()
        contacto.telefono = form.telefono.trim()
        contacto.email = form.email.trim()
        contacto.provincia = provincia
    }
}
