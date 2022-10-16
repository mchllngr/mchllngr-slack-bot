package repository.absence

import model.user.UserId
import network.ApiClient
import util.logger.getLogger

interface AbsenceRepository {

    fun getUserAvailability(): Map<UserId, Availability>

    companion object {

        fun create(apiClient: ApiClient): AbsenceRepository = AbsenceRepositoryImpl(apiClient)
    }
}

class AbsenceRepositoryImpl(private val apiClient: ApiClient) : AbsenceRepository {

    private val logger = getLogger()

    override fun getUserAvailability(): Map<UserId, Availability> {
        // TODO #29 do real stuff

        val response = apiClient.getAbsences()
        logger.debug("response: $response")

        return emptyMap()
    }
}
