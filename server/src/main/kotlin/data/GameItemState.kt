package alfredabdo.ktor.idlegame.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class GameItemState(
    @SerialName("level") val level: Int,
    @SerialName("unlocked") val unlocked: Boolean,
    @SerialName("fillRateMs") val fillRateMs: Long,
    @SerialName("gain") val gain: Double,
    @SerialName("upgradeCost") val upgradeCost: Double,
    @SerialName("progress") val progress: Double,
) {
    companion object {
        fun defaultUsing(item: GameItem) = GameItemState(
            1,
            item.unlockAmount == null,
            item.baseFillRateMs,
            item.baseGain,
            item.baseUpgradeCost,
            0.0,
        )
    }
}