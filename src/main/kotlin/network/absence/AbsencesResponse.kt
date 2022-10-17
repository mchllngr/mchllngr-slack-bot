package network.absence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AbsencesResponse(
    @SerialName("data") val list: List<Data>
) {

    @Serializable
    data class Data(
        val reasonId: String,
        val assignedTo: AssignedTo
    ) {

        @Serializable
        data class AssignedTo(
            val email: String
        )
    }
}
