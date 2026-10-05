package com.example.contactos.config

import com.example.contactos.entity.Provincia
import com.example.contactos.repository.ProvinciaRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ProvinciaSeeder(
    private val provincias: ProvinciaRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (provincias.count() > 0) return

        provincias.saveAll(
            NOMBRES.map { nombre -> Provincia().apply { this.nombre = nombre } },
        )
    }

    private companion object {
        val NOMBRES = listOf(
            "Castellón", "Valencia", "Alicante",
        )
    }
}
