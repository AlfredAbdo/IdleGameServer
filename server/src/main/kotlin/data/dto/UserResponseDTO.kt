package alfredabdo.ktor.idlegame.data.dto

import alfredabdo.ktor.idlegame.data.Achievement
import alfredabdo.ktor.idlegame.data.GameItem
import alfredabdo.ktor.idlegame.data.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDTO(
    @SerialName("user") val user: User,
    @SerialName("gameItems") val gameItems: List<GameItem>,
    @SerialName("achievements") val achievements: List<Achievement>,
) {
}