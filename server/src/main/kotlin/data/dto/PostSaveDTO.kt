package alfredabdo.ktor.idlegame.data.dto

import alfredabdo.ktor.idlegame.data.GameItemState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostSaveDTO(
    @SerialName("coins") val coins: Double,
    @SerialName("states") val states: Map<UInt, GameItemState>,
    @SerialName("activeAchievementId") val activeAchievementId: UInt?,
)