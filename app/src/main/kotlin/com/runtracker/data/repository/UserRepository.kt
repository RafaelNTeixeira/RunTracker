package com.runtracker.data.repository

import com.runtracker.data.db.dao.UserDao
import com.runtracker.data.db.entity.UserEntity
import com.runtracker.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val prefs: UserPreferences
) {
    val loggedInUsername: Flow<String?> = prefs.loggedInUsername

    suspend fun register(username: String, email: String, password: String): Result<Unit> {
        if (username.isBlank() || email.isBlank() || password.isBlank())
            return Result.failure(Exception("All fields are required"))
        if (password.length < 6)
            return Result.failure(Exception("Password must be at least 6 characters"))
        if (userDao.usernameExists(username) > 0)
            return Result.failure(Exception("Username already taken"))
        userDao.insert(UserEntity(username = username, email = email, passwordHash = hash(password)))
        prefs.setLoggedIn(username)
        return Result.success(Unit)
    }

    suspend fun login(username: String, password: String): Result<Unit> {
        val user = userDao.getByUsername(username)
            ?: return Result.failure(Exception("User not found"))
        if (user.passwordHash != hash(password))
            return Result.failure(Exception("Incorrect password"))
        prefs.setLoggedIn(username)
        return Result.success(Unit)
    }

    suspend fun logout() = prefs.logout()

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
