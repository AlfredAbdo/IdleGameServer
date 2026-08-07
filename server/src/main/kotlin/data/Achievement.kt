package alfredabdo.ktor.idlegame.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*

@Serializable
data class Achievement(
    @SerialName("id") val id: UInt,
    @SerialName("description") val description: String,
    @SerialName("popupText") val popupText: String,
    @SerialName("conditions") val conditions: List<AchievementCondition>,
)

