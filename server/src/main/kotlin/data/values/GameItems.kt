package alfredabdo.ktor.idlegame.data.values

import alfredabdo.ktor.idlegame.data.GameItem
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

val gameItems: List<GameItem>
    get() = listOf(
        GameItem(
            1u,
            "Pocket change",
            "Rely on your parents, ...",
            2.seconds.inWholeMilliseconds,
            10.0,
            GameItem.UpgradeMultipliers(1.5, 1.2, 1.3),
            0.0,
            20.0,
        ),
        GameItem(
            2u,
            "Work as an employee",
            "Become a developer in a company.",
            5.seconds.inWholeMilliseconds,
            90.0,
            GameItem.UpgradeMultipliers(1.5, 1.2, 1.3),
            100.0,
            200.0,
        ),
        GameItem(
            3u,
            "Freelancer",
            "Work as a developer freelancer, with no one above you :).",
            30.seconds.inWholeMilliseconds,
            500.0,
            GameItem.UpgradeMultipliers(1.5, 1.2, 1.3),
            500.0,
            700.0,
        ),
        GameItem(
            4u,
            "Create a company",
            "Create your own company, and give orders to other developers.",
            2.minutes.inWholeMilliseconds,
            2_000.0,
            GameItem.UpgradeMultipliers(1.5, 1.2, 1.3),
            3_000.0,
            4_000.0,
        ),
        GameItem(
            5u,
            "Invest in real estate",
            "Buy some lands and make easy money.",
            10.minutes.inWholeMilliseconds,
            10_000.0,
            GameItem.UpgradeMultipliers(1.5, 1.2, 1.3),
            20_000.0,
            40_000.0,
        ),
        GameItem(
            6u,
            "Crypto mining",
            "Buy crypto-mining machines and harness the power of the crypto-currency; hopefully they will not lose their value :(.",
            30.minutes.inWholeMilliseconds,
            30_000.0,
            GameItem.UpgradeMultipliers(1.5, 1.2, 1.3),
            60_000.0,
            120_000.0,
        ),
        GameItem(
            7u,
            "Sell trading cards",
            "Spend your time buying and selling all the trading cards for maximum profit! You don't care about the games using them, just their value.",
            1.hours.inWholeMilliseconds,
            120_000.0,
            GameItem.UpgradeMultipliers(1.8, 1.3, 1.5),
            240_000.0,
            480_000.0,
        ),
    )