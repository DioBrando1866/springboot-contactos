package com.example.contactos.repository

import com.example.contactos.entity.Provincia
import org.springframework.data.jpa.repository.JpaRepository

interface ProvinciaRepository : JpaRepository<Provincia, Long> {

    fun findAllByOrderByNombreAsc(): List<Provincia>
}
