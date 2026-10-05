package com.example.contactos.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "contacto")
class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var nombre: String = ""

    @Column(nullable = false, length = 15)
    var telefono: String = ""

    @Column(nullable = false)
    var email: String = ""

    @ManyToOne
    @JoinColumn(name = "provincia_id")
    var provincia: Provincia? = null
}
