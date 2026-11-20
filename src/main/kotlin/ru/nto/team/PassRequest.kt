package ru.nto.team

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "pass_request")
open class PassRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    open var host: Staff,
    @Column(name = "full_name", nullable = false, length = 150)
    open var fullName: String,
    @Column(nullable = false, length = 12)
    open var phone: String,
    @Column(length = 150)
    open var organization: String?,
    @Column(nullable = false, length = 200)
    open var purpose: String,
    @Column(name = "valid_from", nullable = false)
    open var validFrom: LocalDateTime,
    @Column(name = "valid_to", nullable = false)
    open var validTo: LocalDateTime,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    open var status: RequestStatus,
    @Column(length = 500)
    open var comment: String?,
    @Column(name = "created_at", nullable = false)
    open var createdAt: LocalDateTime,
    @Column(name = "updated_at", nullable = false)
    open var updatedAt: LocalDateTime,
)
