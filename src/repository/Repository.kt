package com.peter.repository

import com.peter.data.Todo
import com.peter.data.User

interface Repository {
    suspend fun addUser(email: String,
                        displayName: String,
                        passwordHash: String): User?
    suspend fun findUser(userId: Int): User?
    suspend fun findUserByEmail(email: String): User?

    suspend fun addTodo(userId: Int, todo: String, done: Boolean): Todo?
    suspend fun getTodos(userId: Int): List<Todo>

    suspend fun updateTodo(todoId : Int, status : Boolean ) : Todo?

    suspend fun getTodoById(todoId: Int) : Todo?
}