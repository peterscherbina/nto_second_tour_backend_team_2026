package ru.nto.team

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class StaffService(private val repository: StaffRepository) {
    fun authenticate(code: String): Staff {
        if (!code.matches(Regex("[A-Za-z0-9]{4}"))) throw ApiException(400, "Invalid code format")
        return repository.findByCode(code) ?: throw ApiException(401, "Unauthorized")
    }

    fun profile(code: String): ProfileDto = ProfileDto(authenticate(code).fullName)
}
