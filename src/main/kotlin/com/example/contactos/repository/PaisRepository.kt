package com.example.contactos.repository

import com.example.contactos.entity.Pais
import org.springframework.data.jpa.repository.JpaRepository

interface PaisRepository : JpaRepository<Pais, Long> {

    fun findAllByOrderByNombreAsc(): List<Pais>
}
