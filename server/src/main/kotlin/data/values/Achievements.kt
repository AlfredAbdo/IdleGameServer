package alfredabdo.ktor.idlegame.data.values

import alfredabdo.ktor.idlegame.data.Achievement
import alfredabdo.ktor.idlegame.data.AchievementCondition

val achievements: List<Achievement>
    get() = listOf(
        Achievement(
            1u,
            "Purchase your first source of income!",
            "3, 2, 1, Go!",
            listOf(
                AchievementCondition.item(0) { unlocked() },
            ),
        ),
        Achievement(
            2u,
            "Get at least 1,000 coins",
            "Your first thousand.",
            listOf(
                AchievementCondition.coins { geq(1_000.0) },
            ),
        ),
        Achievement(
            3u,
            "Get any item duration to $infiniteAnimationThreshold or below",
            "NOT THE EYES!",
            listOf(
                AchievementCondition.anyItem { fillRate { leq(infiniteAnimationThreshold.inWholeMilliseconds) } },
            ),
        ),
        Achievement(
            4u,
            "Get at least 1,000,000 coins",
            "You're a millionaire, Harry!",
            listOf(
                AchievementCondition.coins { geq(1_000_000.0) },
            ),
        ),
        Achievement(
            5u,
            "Get at least 1B coins",
            "When you have nothing else to do (thank you for playing the game).",
            listOf(
                AchievementCondition.coins { geq(1_000_000_000.0) },
            ),
        ),
    )