package com.example.contactos.config

import com.example.contactos.entity.Pais
import com.example.contactos.entity.Provincia
import com.example.contactos.repository.PaisRepository
import com.example.contactos.repository.ProvinciaRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class PaisSeeder(
    private val paises: PaisRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (paises.count() > 0) return

        paises.saveAll(
            NOMBRES.map { nombre -> Pais().apply { this.nombre = nombre } },
        )
    }

    private companion object {
        val NOMBRES = listOf(
            "España", "París", "Alemania",
        )
    }
}
