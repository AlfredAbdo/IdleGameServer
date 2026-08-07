package alfredabdo.ktor.idlegame

import alfredabdo.ktor.idlegame.data.dto.PostSaveDTO
import alfredabdo.ktor.idlegame.data.dto.PostUserDTO
import alfredabdo.ktor.idlegame.data.dto.UserResponseDTO
import alfredabdo.ktor.idlegame.database.MainService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun Application.configureRouting() {
    val mainService: MainService = dependencies.resolve()

    routing {
        staticResources("/static", "static")

        route("/api") {

            //TODO have the MainService handle everything at once for the related apis (instead of fetching twice some values from the tables),
            // or have the conditions checked by the server (no offline) and don't return the static values.

            get("/user") {
                val userId = call.requireHeader("userId").toUInt() //this version does not show the header in the Endpoints tool window

                coroutineScope {
                    val user = async { mainService.getUser(userId) }
                    val items = async { mainService.getGameItems() }
                    val achievements = async { mainService.getAchievements() }

                    call.respond(
                        HttpStatusCode.OK,
                        UserResponseDTO(user.await(), items.await(), achievements.await()),
                    )
                }
            }

            post("/user") {
                val body = call.receive<PostUserDTO>()

                coroutineScope {
                    val items = async { mainService.getGameItems() }
                    val achievements = async { mainService.getAchievements() }

                    val user = mainService.getUserByUsername(body.username)
                    if (user != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            UserResponseDTO(user, items.await(), achievements.await()),
                        )
                    } else {
                        val user = mainService.createUserWithUsername(body.username)
                        call.respond(
                            HttpStatusCode.Created,
                            UserResponseDTO(user, items.await(), achievements.await()),
                        )
                    }
                }
            }

            post("/save") {
                val userId = call.requireHeader("userId").toUInt()
                val body = call.receive<PostSaveDTO>()

                mainService.save(
                    userId = userId,
                    coins = body.coins,
                    itemStates = body.states,
                    activeAchievementId = body.activeAchievementId,
                )

                call.respond(HttpStatusCode.OK, mapOf("message" to "Saved successfully"))
            }
        }
    }
}