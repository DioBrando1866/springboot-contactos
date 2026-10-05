package com.example.contactos.repository

import com.example.contactos.entity.Contacto
import org.springframework.data.jpa.repository.JpaRepository

interface ContactoRepository : JpaRepository<Contacto, Long>
