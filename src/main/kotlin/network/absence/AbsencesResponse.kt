package network.absence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AbsencesResponse(
    @SerialName("data") val list: List<Data>
) {

    @Serializable
    data class Data(
        val reasonId: String? = null,
        val assignedTo: AssignedTo? = null
    ) {

        @Serializable
        data class AssignedTo(
            val email: String
        )
    }
}
