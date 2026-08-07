package alfredabdo.ktor.idlegame.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("userId") val userId: UInt,
    @SerialName("username") val username: String,
    @SerialName("coins") val coins: Double,
    @SerialName("states") val states: Map<UInt, GameItemState>,
    @SerialName("activeAchievement") val activeAchievement: Achievement?,
)