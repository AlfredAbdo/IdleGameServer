package alfredabdo.ktor.idlegame.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.uri
import io.ktor.server.response.*

fun Application.configureStatuses() {
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { code ->
            call.respondText(text = "404: Cannot find relative url: ${call.request.uri}" , status = code)
        }
        status(HttpStatusCode.Unauthorized) { code ->
            call.respondText(text = "401: Not allowed to interact with relative url: ${call.request.uri}" , status = code)
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
}
