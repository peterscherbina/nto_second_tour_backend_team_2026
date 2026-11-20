package ru.nto.team

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Service
@Transactional
class RequestService(
    private val staffService: StaffService,
    private val repository: PassRequestRepository,
    private val clock: Clock,
) {
    private val zone = ZoneId.of("Europe/Moscow")

    @Transactional(readOnly = true)
    fun list(code: String, status: String?): List<RequestListDto> {
        val host = staffService.authenticate(code)
        val filter = status?.let { value ->
            RequestStatus.entries.find { it.name == value } ?: throw ApiException(400, "Invalid status")
        }
        val requests = if (filter == null) {
            repository.findAllByHostIdOrderByValidFromAscIdAsc(host.id)
        } else {
            repository.findAllByHostIdAndStatusOrderByValidFromAscIdAsc(host.id, filter)
        }
        return requests.map { it.toListDto() }
    }

    @Transactional(readOnly = true)
    fun details(code: String, id: Long): RequestDetailsDto = ownRequest(code, id).toDetailsDto()

    fun create(code: String, body: CreateRequestBody): RequestDetailsDto {
        val host = staffService.authenticate(code)
        val now = LocalDateTime.ofInstant(clock.instant(), zone)
        val input = RequestValidation.create(body, now)
        if (repository.existsByHostIdAndPhoneAndStatusInAndValidFromLessThanAndValidToGreaterThan(
                host.id, input.phone, listOf(RequestStatus.PENDING, RequestStatus.APPROVED),
                input.validTo, input.validFrom,
            )) {
            throw ApiException(409, "Duplicate request")
        }
        val timestamp = now.truncatedTo(ChronoUnit.SECONDS)
        return repository.save(
            PassRequest(
                host = host, fullName = input.fullName, phone = input.phone,
                organization = input.organization, purpose = input.purpose,
                validFrom = input.validFrom, validTo = input.validTo,
                status = RequestStatus.PENDING, comment = null,
                createdAt = timestamp, updatedAt = timestamp,
            ),
        ).toDetailsDto()
    }

    fun cancel(code: String, id: Long): RequestDetailsDto {
        val request = ownRequest(code, id)
        requirePending(request)
        request.status = RequestStatus.CANCELLED
        request.updatedAt = timestamp()
        return repository.save(request).toDetailsDto()
    }

    fun decide(id: Long, body: DecisionBody): RequestDetailsDto {
        val request = repository.findById(id).orElseThrow { ApiException(404, "Request not found") }
        val decision = RequestValidation.decision(body)
        requirePending(request)
        request.status = decision.status
        request.comment = decision.comment
        request.updatedAt = timestamp()
        return repository.save(request).toDetailsDto()
    }

    private fun ownRequest(code: String, id: Long): PassRequest {
        val host = staffService.authenticate(code)
        return repository.findByIdAndHostId(id, host.id) ?: throw ApiException(404, "Request not found")
    }

    private fun requirePending(request: PassRequest) {
        if (request.status != RequestStatus.PENDING) throw ApiException(409, "Invalid status transition")
    }

    private fun timestamp(): LocalDateTime = LocalDateTime.ofInstant(clock.instant(), zone).truncatedTo(ChronoUnit.SECONDS)
}
