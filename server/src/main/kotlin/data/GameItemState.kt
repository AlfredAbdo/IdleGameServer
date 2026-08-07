package alfredabdo.ktor.idlegame.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class GameItemState(
    @SerialName("level") val level: Int,
    @SerialName("unlocked") val unlocked: Boolean,
    @SerialName("fillRate") val fillRate: Duration,
    @SerialName("gain") val gain: Double,
    @SerialName("upgradeCost") val upgradeCost: Double,
    @SerialName("progress") val progress: Double,
) {
    companion object {
        fun defaultUsing(item: GameItem) = GameItemState(
            1,
            item.unlockAmount == null,
            item.baseFillRate,
            item.baseGain,
            item.baseUpgradeCost,
            0.0,
        )
    }
}