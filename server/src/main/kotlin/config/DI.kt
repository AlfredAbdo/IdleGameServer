package alfredabdo.ktor.idlegame.config

import alfredabdo.ktor.idlegame.database.MainService
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

// The contents of the `install` function will be used for the project template
fun Application.configureDependencies() {
    dependencies {
        provide {
            R2dbcDatabase.connect(
                url = "r2dbc:h2:file:///./h2",
                user = "root",
                password = "",
            )
        }

        provide {
            MainService(resolve()).also {
                it.createSchema()
            }
        }
    }
}
