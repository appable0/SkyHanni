package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class ChocolateFactoryUpgrade(
    val slotIndex: Int,
    val level: Int,
    val price: Long?,
    val extraPerSecond: Double? = null,
    val effectiveCost: Double? = null,
    val isRabbit: Boolean = false,
    val isPrestige: Boolean = false,
) {
    private var chocolateAmountType = ChocolateAmount.CURRENT
    val isMaxed = price == null
    val canAffordAt: SimpleTimeMark
    val totalPaybackPeriod: Duration

    init {
        if (isPrestige) {
            chocolateAmountType = ChocolateAmount.PRESTIGE
        }
        val canAffordIn = chocolateAmountType.timeUntilGoal(price ?: 0)
        canAffordAt = when {
            canAffordIn.isInfinite() -> SimpleTimeMark.farFuture()
            else -> SimpleTimeMark.now() + canAffordIn
        }
        val timeUntilAffordable = canAffordAt.timeUntil().coerceAtLeast(0.seconds)
        totalPaybackPeriod = timeUntilAffordable + (effectiveCost ?: 0.0).seconds
    }

    fun canAfford(): Boolean {
        if (price == null) return false
        return chocolateAmountType.chocolate() >= price
    }

    fun formattedTimeUntilGoal(): String {
        return chocolateAmountType.formattedTimeUntilGoal(price ?: 0)
    }

    fun stackTip(): String {
        return when {
            level == 0 -> ""
            isMaxed -> "§a✔"

            isRabbit -> when (level) {
                in (0..9) -> ""
                in (10..74) -> "§a"
                in (75..124) -> "§9"
                in (125..174) -> "§5"
                in (175..199) -> "§6"
                in (200..219) -> "§d"
                in (220..225) -> "§b"
                else -> "§c"
            } + level

            else -> "$level"
        }
    }

    fun getValidUpgradeIndex(): Int {
        return when (slotIndex) {
            in ignoredSlotIndexes -> -1
            else -> slotIndex
        }
    }

    companion object {
        var ignoredSlotIndexes = listOf<Int>()

        fun updateIgnoredSlots() {
            ignoredSlotIndexes = listOf(
                ChocolateFactoryAPI.prestigeIndex,
                ChocolateFactoryAPI.handCookieIndex,
                ChocolateFactoryAPI.shrineIndex,
                ChocolateFactoryAPI.barnIndex,
            )
        }
    }
}
