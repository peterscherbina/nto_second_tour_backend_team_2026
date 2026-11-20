package ru.nto.team

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

data class ValidatedRequest(
    val fullName: String,
    val phone: String,
    val organization: String?,
    val purpose: String,
    val validFrom: LocalDateTime,
    val validTo: LocalDateTime,
)

data class ValidatedDecision(val status: RequestStatus, val comment: String?)

object RequestValidation {
    private val dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss", Locale.ROOT)
        .withResolverStyle(ResolverStyle.STRICT)
    private val datePattern = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}")
    private val phonePattern = Regex("\\+7[0-9]{10}")

    fun create(body: CreateRequestBody, now: LocalDateTime): ValidatedRequest {
        val errors = linkedMapOf<String, String>()
        val fullName = body.fullName?.trim().orEmpty()
        val phone = body.phone?.trim().orEmpty()
        val organization = body.organization?.trim()?.takeIf { it.isNotEmpty() }
        val purpose = body.purpose?.trim().orEmpty()
        if (fullName.length !in 5..150) errors["fullName"] = "ФИО должно содержать от 5 до 150 символов"
        if (!phonePattern.matches(phone)) errors["phone"] = "Телефон должен иметь формат +7 и 10 цифр"
        if (organization != null && organization.length > 150) errors["organization"] = "Организация: не более 150 символов"
        if (purpose.length !in 5..200) errors["purpose"] = "Цель должна содержать от 5 до 200 символов"
        val from = parse(body.validFrom?.trim())
        val to = parse(body.validTo?.trim())
        if (from == null) {
            errors["validFrom"] = "Укажите дату и время начала в формате yyyy-MM-dd'T'HH:mm:ss"
        } else if (from.isBefore(now)) {
            errors["validFrom"] = "Начало не может быть раньше текущего времени"
        }
        if (to == null) {
            errors["validTo"] = "Укажите дату и время окончания в формате yyyy-MM-dd'T'HH:mm:ss"
        } else if (from != null && (!to.isAfter(from) || to.toLocalDate() != from.toLocalDate())) {
            errors["validTo"] = "Окончание должно быть позже начала в тот же день"
        }
        if (errors.isNotEmpty()) throw FieldValidationException(errors)
        return ValidatedRequest(fullName, phone, organization, purpose, from!!, to!!)
    }

    fun decision(body: DecisionBody): ValidatedDecision {
        val errors = linkedMapOf<String, String>()
        val status = when (body.status?.trim()) {
            "APPROVED" -> RequestStatus.APPROVED
            "REJECTED" -> RequestStatus.REJECTED
            else -> null
        }
        if (status == null) errors["status"] = "Допустимы APPROVED и REJECTED"
        val comment = if (status == RequestStatus.REJECTED) body.comment?.trim() else null
        if (status == RequestStatus.REJECTED && (comment == null || comment.length !in 1..500)) {
            errors["comment"] = "Причина должна содержать от 1 до 500 символов"
        }
        if (errors.isNotEmpty()) throw FieldValidationException(errors)
        return ValidatedDecision(status!!, comment)
    }

    private fun parse(value: String?): LocalDateTime? {
        if (value == null || !datePattern.matches(value)) return null
        return try {
            LocalDateTime.parse(value, dateFormat).takeIf { it.year in 1..9999 }
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
