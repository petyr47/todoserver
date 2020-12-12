package com.peter.routes

import com.peter.API_VERSION
import com.peter.auth.JwtService
import com.peter.auth.MySession
import com.peter.repository.Repository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

const val USERS = "$API_VERSION/users"
const val USER_LOGIN = "$USERS/login"
const val USER_CREATE = "$USERS/create"


@KtorExperimentalLocationsAPI
@Location(USER_LOGIN)
class UserLoginRoute

@KtorExperimentalLocationsAPI
@Location(USER_CREATE)
class UserCreateRoute

const val TODOS = "$API_VERSION/todos"

@KtorExperimentalLocationsAPI
@Location(TODOS)
class TodoRoute



@KtorExperimentalLocationsAPI
// 1
fun Route.users(
    db: Repository,
    jwtService: JwtService,
    hashFunction: (String) -> String
) {
    post<UserCreateRoute> { // 2
        val signupParameters = call.receive<Parameters>() // 3
        val password = signupParameters["password"] // 4
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields")
        val displayName = signupParameters["displayName"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields")
        val email = signupParameters["email"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields")
        val hash = hashFunction(password) // 5
        try {
            val newUser = db.addUser(email, displayName, hash) // 6
            newUser?.userId?.let {
                call.sessions.set(MySession(it))
                call.respond(mapOf("token" to jwtService.generateToken(newUser),"user" to newUser.responseUserData()))
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems creating User")
        }
    }


    post<UserLoginRoute> { // 1
        val signinParameters = call.receive<Parameters>()
        val password = signinParameters["password"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields")
        val email = signinParameters["email"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields")
        val hash = hashFunction(password)
        try {
            val currentUser = db.findUserByEmail(email) // 2
            currentUser?.userId?.let {
                if (currentUser.passwordHash == hash) { // 3
                    call.sessions.set(MySession(it)) // 4
                    call.respond(mapOf("token" to jwtService.generateToken(currentUser),"user" to currentUser.responseUserData())) // 5
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest, "Problems retrieving User") // 6
                }
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
        }
    }
}


@KtorExperimentalLocationsAPI
fun Route.todos(db: Repository) {
    authenticate("jwt") { // 1
        post<TodoRoute> { // 2
            val todosParameters = call.receive<Parameters>()
            val todo = todosParameters["todo"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing Todo")
            val done = todosParameters["done"] ?: "false"
            // 3
            val user = call.sessions.get<MySession>()?.let {
                db.findUser(it.userId)
            }
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, "Problems retrieving User")
                return@post
            }

            try {
                // 4
                val currentTodo = db.addTodo(
                    user.userId, todo, done.toBoolean())
                currentTodo?.id?.let {
                    call.respond(HttpStatusCode.OK, currentTodo)
                }
            } catch (e: Throwable) {
                application.log.error("Failed to add todo", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Saving Todo")
            }
        }


        get<TodoRoute> {
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
                return@get
            }
            try {
                val todos = db.getTodos(user.userId)
                call.respond(todos)
            } catch (e: Throwable) {
                application.log.error("Failed to get Todos", e)
                call.respond(HttpStatusCode.BadRequest, "Problems getting Todos")
            }
        }
    }


}
