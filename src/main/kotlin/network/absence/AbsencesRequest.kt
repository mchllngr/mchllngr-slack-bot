package network.absence


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AbsencesRequest(
    val skip: Int,
    val limit: Int,
    val filter: Filter,
    val relations: List<String>
) {

    @Serializable
    data class Filter(
        @SerialName("assignedTo:user._id") val assignedToUser: User,
        val start: Start,
        val end: End
    ) {

        @Serializable
        data class User(
            val email: Emails
        ) {

            @Serializable
            data class Emails(
                @SerialName("\$in") val list: List<String>
            )
        }

        @Serializable
        data class Start(
            @SerialName("\$lte") val lte: String
        )

        @Serializable
        data class End(
            @SerialName("\$gte") val gte: String
        )
    }
}
