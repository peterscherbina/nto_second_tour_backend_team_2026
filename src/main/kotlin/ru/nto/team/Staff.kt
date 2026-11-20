package ru.nto.team

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "staff")
open class Staff(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long = 0,
    @Column(nullable = false, unique = true, length = 4)
    open var code: String = "",
    @Column(name = "full_name", nullable = false, length = 150)
    open var fullName: String = "",
)
