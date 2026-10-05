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
            "A Coruña", "Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila", "Badajoz",
            "Barcelona", "Bizkaia", "Burgos", "Cáceres", "Cádiz", "Cantabria", "Castellón", "Ceuta",
            "Ciudad Real", "Córdoba", "Cuenca", "Gipuzkoa", "Girona", "Granada", "Guadalajara",
            "Huelva", "Huesca", "Illes Balears", "Jaén", "La Rioja", "Las Palmas", "León", "Lleida",
            "Lugo", "Madrid", "Málaga", "Melilla", "Murcia", "Navarra", "Ourense", "Palencia",
            "Pontevedra", "Salamanca", "Santa Cruz de Tenerife", "Segovia", "Sevilla", "Soria",
            "Tarragona", "Teruel", "Toledo", "Valencia", "Valladolid", "Zamora", "Zaragoza",
        )
    }
}
