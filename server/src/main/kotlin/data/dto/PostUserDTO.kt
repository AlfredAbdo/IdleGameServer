package alfredabdo.ktor.idlegame.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostUserDTO(
    @SerialName("username") val username: String,
)