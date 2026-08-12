package alfredabdo.ktor.idlegame.database

import alfredabdo.ktor.idlegame.data.*
import alfredabdo.ktor.idlegame.data.values.achievements
import alfredabdo.ktor.idlegame.data.values.gameItems
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.UIntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.json.json
import org.jetbrains.exposed.v1.r2dbc.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

class MainService(val database: R2dbcDatabase) {

    object Users : UIntIdTable("users") {
        val username = varchar("username", 100).uniqueIndex()
        val coins = double("coins").default(0.0)
        val activeAchievement = uinteger("active_achievement").nullable()
    }

    object GameItems : UIntIdTable("game_items") {
        val title = varchar("title", 255)
        val description = text("description")
        val baseFillRateMs = long("base_fill_rate_ms")
        val baseGain = double("base_gain")
        val costMultiplier = double("cost_multiplier")
        val fillRateMultiplier = double("fill_rate_multiplier")
        val gainMultiplier = double("gain_multiplier")
        val unlockAmount = double("unlock_amount").nullable()
        val baseUpgradeCost = double("base_upgrade_cost")


        suspend fun batchInsertItems(items: List<GameItem>) {
            batchInsert(items) { item ->
                this[id] = item.id
                this[title] = item.title
                this[description] = item.description
                this[baseFillRateMs] = item.baseFillRateMs
                this[baseGain] = item.baseGain
                this[costMultiplier] = item.upgradeMultipliers.costMultiplier
                this[fillRateMultiplier] = item.upgradeMultipliers.fillRateMultiplier
                this[gainMultiplier] = item.upgradeMultipliers.gainMultiplier
                this[unlockAmount] = item.unlockAmount
                this[baseUpgradeCost] = item.baseUpgradeCost
            }
        }
    }

    object Achievements : UIntIdTable("achievements") {
        private val json = Json {
            encodeDefaults = false
            explicitNulls = false
        }

        val description = text("description")
        val popupText = text("popup_text")
        val conditions = json<List<AchievementCondition>>("conditions", json)


        suspend fun batchInsertItems(items: List<Achievement>) {
            batchInsert(items) { item ->
                this[id] = item.id
                this[description] = item.description
                this[popupText] = item.popupText
                this[conditions] = item.conditions
            }
        }

        fun mapToEntity(result: ResultRow): Achievement = Achievement(
            result[id].value,
            result[description],
            result[popupText],
            result[conditions],
        )
    }

    object GameItemSaves : CompositeIdTable("game_item_saves") {
        val userId = reference("user_id", Users)
        val gameItemId = reference("game_item_id", GameItems)

        override val primaryKey = PrimaryKey(userId, gameItemId)

        val level = integer("level")
        val unlocked = bool("unlocked")
        val fillRateMs = long("fill_rate_ms")
        val gain = double("gain")
        val upgradeCost = double("upgrade_cost")
        val progress = double("progress").default(0.0)
    }


    suspend fun createSchema() {
        suspendTransaction(database) {
            SchemaUtils.create(Users, GameItems, Achievements, GameItemSaves, inBatch = true)

            if (GameItems.select(GameItems.id).limit(1).empty()) {
                GameItems.batchInsertItems(gameItems)
            }

            if (Achievements.select(Achievements.id).limit(1).empty()) {
                Achievements.batchInsertItems(achievements)
            }
        }
    }

    suspend fun getUserByUsername(username: String): User? = run {
        val existing = suspendTransaction(database) {
            Users.selectAll()
                .where { Users.username eq username }
                .firstOrNull()
        }

        existing?.let { getUser(it[Users.id].value) }
    }

    suspend fun createUserWithUsername(username: String): User = coroutineScope {
        val firstAchievementAsync = async {
            suspendTransaction(database) {
                Achievements.selectAll()
                    .orderBy(Achievements.id)
                    .limit(1)
                    .firstOrNull()
            }
        }
        val gameItemsAsync = async { getGameItems() }

        val firstAchievement = firstAchievementAsync.await()

        suspendTransaction(database) {
            Users.insert {
                it[Users.username] = username
                it[Users.activeAchievement] = firstAchievement?.get(Achievements.id)?.value
            }
        }.let { result ->
            User(
                result[Users.id].value,
                result[Users.username],
                result[Users.coins],
                gameItemsAsync.await().associate { item -> item.id to GameItemState.defaultUsing(item) },
                firstAchievement?.let { achievement ->
                    Achievement(
                        achievement[Achievements.id].value,
                        achievement[Achievements.description],
                        achievement[Achievements.popupText],
                        achievement[Achievements.conditions],
                    )
                },
            )
        }
    }

    suspend fun getGameItems(): List<GameItem> = suspendTransaction(database) {
        GameItems.selectAll()
            .map {
                GameItem(
                    it[GameItems.id].value,
                    it[GameItems.title],
                    it[GameItems.description],
                    it[GameItems.baseFillRateMs],
                    it[GameItems.baseGain],
                    GameItem.UpgradeMultipliers(
                        it[GameItems.costMultiplier],
                        it[GameItems.fillRateMultiplier],
                        it[GameItems.gainMultiplier],
                    ),
                    it[GameItems.unlockAmount],
                    it[GameItems.baseUpgradeCost],
                )
            }
            .toList()
    }

    @Throws(NoSuchElementException::class)
    suspend fun getUser(userId: UInt): User = coroutineScope {
        val user = getUserRow(userId)

        val gameItemsAsync = async { getGameItems() }
        val gameItemSavesAsync = async {
            suspendTransaction(database) {
                GameItemSaves.selectAll()
                    .where { GameItemSaves.userId eq userId }
                    .associateBy { it[GameItemSaves.gameItemId].value }
            }
        }
        val activeAchievementAsync = user[Users.activeAchievement]?.let { id -> async { getAchievement(id) } }

        val gameItems = gameItemsAsync.await()
        val gameItemSaves = gameItemSavesAsync.await()
        val activeAchievement = activeAchievementAsync?.await()

        User(
            user[Users.id].value,
            user[Users.username],
            user[Users.coins],
            gameItems.associate { item ->
                val save = gameItemSaves[item.id]?.let {
                    GameItemState(
                        it[GameItemSaves.level],
                        it[GameItemSaves.unlocked],
                        it[GameItemSaves.fillRateMs],
                        it[GameItemSaves.gain],
                        it[GameItemSaves.upgradeCost],
                        it[GameItemSaves.progress],
                    )
                } ?: GameItemState.defaultUsing(item)

                item.id to save
            },
            activeAchievement,
        )
    }

    suspend fun getAchievements(): List<Achievement> = suspendTransaction(database) {
        Achievements.selectAll()
            .map(Achievements::mapToEntity)
            .toList()
    }

    suspend fun getAchievement(id: UInt): Achievement? = suspendTransaction(database) {
        Achievements.selectAll()
            .where { Achievements.id eq id }
            .map(Achievements::mapToEntity)
            .firstOrNull()
    }

    suspend fun nextAchievement(id: UInt): Achievement? = suspendTransaction(database) {
        Achievements.selectAll()
            .where { Achievements.id greater id }
            .orderBy(Achievements.id)
            .map(Achievements::mapToEntity)
            .firstOrNull()
    }

    suspend fun save(
        userId: UInt,
        coins: Double,
        itemStates: Map<UInt, GameItemState>,
        activeAchievementId: UInt?,
    ) = suspendTransaction(database) {
        GameItemSaves.batchUpsert(itemStates.toList()) { (itemId, save) ->
            this[GameItemSaves.userId] = userId
            this[GameItemSaves.gameItemId] = itemId
            this[GameItemSaves.level] = save.level
            this[GameItemSaves.unlocked] = save.unlocked
            this[GameItemSaves.fillRateMs] = save.fillRateMs
            this[GameItemSaves.gain] = save.gain
            this[GameItemSaves.upgradeCost] = save.upgradeCost
            this[GameItemSaves.progress] = save.progress
        }

        Users.update(where = { Users.id eq userId }) {
            it[Users.coins] = coins
            it[Users.activeAchievement] = activeAchievementId
        }
    }

    suspend fun delete(userId: UInt): String = suspendTransaction(database) {
        GameItemSaves.deleteWhere { GameItemSaves.userId eq userId }

        val username = Users.select(Users.username)
            .where { Users.id eq userId }
            .map { it[Users.username] }
            .single()

        Users.deleteWhere { Users.username eq username }

        username
    }


    @Throws(NoSuchElementException::class)
    private suspend fun getUserRow(userId: UInt) = suspendTransaction(database) {
        Users.selectAll()
            .where { Users.id eq userId }
            .first()
    }
}