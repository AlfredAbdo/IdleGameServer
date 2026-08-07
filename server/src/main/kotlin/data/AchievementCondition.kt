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
data class AchievementCondition(
    @SerialName("coins") val coinsCondition: ValueComparison<Double>?,
    @SerialName("item") val itemsCondition: ItemConditionData?,
) {
    companion object {
        private const val ANY_INDEX = -1
        private const val ALL_INDEX = -2

        fun coins(valueComparison: DoubleValueComparison) =
            AchievementCondition(coinsCondition = ValueComparisonBuilder.valueComparison(), itemsCondition = null)

        fun item(index: Int, condition: ItemConditionDataBuilder.() -> ItemConditionData) =
            AchievementCondition(coinsCondition = null, itemsCondition = ItemConditionDataBuilder(index).condition())

        fun anyItem(condition: ItemConditionDataBuilder.() -> ItemConditionData) = item(ANY_INDEX, condition)
        fun allItems(condition: ItemConditionDataBuilder.() -> ItemConditionData) = item(ALL_INDEX, condition)
    }

    @Serializable
    data class ItemConditionData(
        @SerialName("index") val index: Int,
        @SerialName("level") val level: ValueComparison<Int>? = null,
        @SerialName("unlocked") val unlocked: Boolean? = null,
        @SerialName("fillRateMs") val fillRateMs: ValueComparison<Double>? = null,
        @SerialName("gain") val gain: ValueComparison<Double>? = null,
        @SerialName("upgradeCost") val upgradeCost: ValueComparison<Double>? = null,
    ) {
        companion object {
            fun level(index: Int, condition: ValueComparison<Int>) = ItemConditionData(index, level = condition)
            fun unlocked(index: Int, condition: Boolean = true) = ItemConditionData(index, unlocked = condition)
            fun fillRate(index: Int, condition: ValueComparison<Double>) = ItemConditionData(index, fillRateMs = condition)
            fun gain(index: Int, condition: ValueComparison<Double>) = ItemConditionData(index, gain = condition)
            fun upgradeCost(index: Int, condition: ValueComparison<Double>) = ItemConditionData(index, upgradeCost = condition)
        }
    }

    @Serializable(ValueComparison.Serializer::class)
    sealed class ValueComparison<T : Number>(open val value: T) {
        data class LessThan<T : Number>(override val value: T) : ValueComparison<T>(value)
        data class LessThanOrEqual<T : Number>(override val value: T) : ValueComparison<T>(value)
        data class Equals<T : Number>(override val value: T) : ValueComparison<T>(value)
        data class GreaterThan<T : Number>(override val value: T) : ValueComparison<T>(value)
        data class GreaterThanOrEqual<T : Number>(override val value: T) : ValueComparison<T>(value)


        object Serializer : KSerializer<ValueComparison<*>> {
            override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ValueComparison") {
                element("lt", PrimitiveSerialDescriptor("lt", PrimitiveKind.DOUBLE), isOptional = true)
                element("leq", PrimitiveSerialDescriptor("leq", PrimitiveKind.DOUBLE), isOptional = true)
                element("eq", PrimitiveSerialDescriptor("eq", PrimitiveKind.DOUBLE), isOptional = true)
                element("gt", PrimitiveSerialDescriptor("gt", PrimitiveKind.DOUBLE), isOptional = true)
                element("geq", PrimitiveSerialDescriptor("geq", PrimitiveKind.DOUBLE), isOptional = true)
            }

            override fun serialize(encoder: Encoder, value: ValueComparison<*>) {
                encoder.encodeStructure(descriptor) {
                    val number = value.value.toDouble()
                    when (value) {
                        is LessThan -> encodeDoubleElement(descriptor, 0, number)
                        is LessThanOrEqual -> encodeDoubleElement(descriptor, 1, number)
                        is Equals -> encodeDoubleElement(descriptor, 2, number)
                        is GreaterThan -> encodeDoubleElement(descriptor, 3, number)
                        is GreaterThanOrEqual -> encodeDoubleElement(descriptor, 4, number)
                    }
                }
            }

            override fun deserialize(decoder: Decoder): ValueComparison<*> {
                return decoder.decodeStructure(descriptor) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> LessThan(decodeDoubleElement(descriptor, index))
                        1 -> LessThanOrEqual(decodeDoubleElement(descriptor, index))
                        2 -> Equals(decodeDoubleElement(descriptor, index))
                        3 -> GreaterThan(decodeDoubleElement(descriptor, index))
                        4 -> GreaterThanOrEqual(decodeDoubleElement(descriptor, index))
                        CompositeDecoder.DECODE_DONE -> null
                        else -> throw SerializationException("Unknown element index: $index")
                    }
                } ?: throw SerializationException("Missing required comparison operator in JSON object")
            }
        }
    }


    private typealias IntValueComparison = ValueComparisonBuilder.() -> ValueComparison<Int>
    private typealias DoubleValueComparison = ValueComparisonBuilder.() -> ValueComparison<Double>

    object ValueComparisonBuilder {
        fun <T : Number> lt(value: T) = ValueComparison.LessThan(value)
        fun <T : Number> leq(value: T) = ValueComparison.LessThanOrEqual(value)
        fun <T : Number> eq(value: T) = ValueComparison.Equals(value)
        fun <T : Number> gt(value: T) = ValueComparison.GreaterThan(value)
        fun <T : Number> geq(value: T) = ValueComparison.GreaterThanOrEqual(value)
    }

    class ItemConditionDataBuilder(private val index: Int) {
        fun level(condition: IntValueComparison) = ItemConditionData(index, level = ValueComparisonBuilder.condition())
        fun unlocked(condition: Boolean = true) = ItemConditionData(index, unlocked = condition)
        fun fillRate(condition: DoubleValueComparison) = ItemConditionData(index, fillRateMs = ValueComparisonBuilder.condition())
        fun gain(condition: DoubleValueComparison) = ItemConditionData(index, gain = ValueComparisonBuilder.condition())
        fun upgradeCost(condition: DoubleValueComparison) = ItemConditionData(index, upgradeCost = ValueComparisonBuilder.condition())
    }
}