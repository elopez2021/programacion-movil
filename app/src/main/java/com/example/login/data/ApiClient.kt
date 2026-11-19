package com.example.login.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    /**
     * URL base de la API PHP en XAMPP.
     * Con "adb reverse tcp:80 tcp:80" conectado por USB, el teléfono usa "http://localhost/flashcards_api/".
     * Si se usa el emulador de Android Studio, se puede usar "http://10.0.2.2/flashcards_api/".
     */
    var BASE_URL = "http://localhost:8080/flashcards_api/"

    /**
     * Realiza una petición POST enviando y recibiendo JSON
     */
    private suspend fun postJson(endpoint: String, jsonBody: JSONObject): Result<JSONObject> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(BASE_URL + endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }

            // Escribir body JSON
            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val statusCode = connection.responseCode
            val inputStream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val responseText = BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                reader.readText()
            }

            val responseJson = if (responseText.isNotBlank()) {
                JSONObject(responseText)
            } else {
                JSONObject().put("success", false).put("message", "Respuesta vacía del servidor ($statusCode)")
            }

            val success = responseJson.optBoolean("success", false)
            if (success) {
                Result.success(responseJson)
            } else {
                val errorMsg = responseJson.optString("message", "Error del servidor ($statusCode)")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val friendlyError = if (e.message?.contains("ECONNREFUSED") == true || e.message?.contains("Failed to connect") == true) {
                "No se pudo conectar con Apache (XAMPP). Recuerda ejecutar: adb reverse tcp:80 tcp:80"
            } else {
                e.localizedMessage ?: "Error de conexión con la API"
            }
            Result.failure(Exception(friendlyError))
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Iniciar sesión contra flashcards_api/login.php
     */
    suspend fun login(identifier: String, password: String): Result<User> {
        val body = JSONObject().apply {
            put("identifier", identifier)
            put("password", password)
        }

        val result = postJson("login.php", body)
        return result.mapCatching { json ->
            val userObj = json.getJSONObject("user")
            User(
                id = userObj.getLong("id"),
                username = userObj.getString("username"),
                email = userObj.getString("email"),
                name = userObj.getString("name")
            )
        }
    }

    /**
     * Registrar usuario contra flashcards_api/register.php
     */
    suspend fun register(username: String, email: String, password: String, name: String): Result<User> {
        val body = JSONObject().apply {
            put("username", username)
            put("email", email)
            put("password", password)
            put("name", name)
        }

        val result = postJson("register.php", body)
        return result.mapCatching { json ->
            val userObj = json.getJSONObject("user")
            User(
                id = userObj.getLong("id"),
                username = userObj.getString("username"),
                email = userObj.getString("email"),
                name = userObj.getString("name")
            )
        }
    }

    /**
     * Recuperar clave contra flashcards_api/recover_password.php
     */
    suspend fun recoverPassword(email: String): Result<String> {
        val body = JSONObject().apply {
            put("email", email)
        }

        val result = postJson("recover_password.php", body)
        return result.mapCatching { json ->
            json.optString("message", "Instrucciones enviadas al correo")
        }
    }

    /**
     * Actualizar perfil contra flashcards_api/update_profile.php
     */
    suspend fun updateProfile(id: Long, name: String): Result<User> {
        val body = JSONObject().apply {
            put("id", id)
            put("name", name)
        }

        val result = postJson("update_profile.php", body)
        return result.mapCatching { json ->
            val userObj = json.getJSONObject("user")
            User(
                id = userObj.getLong("id"),
                username = userObj.getString("username"),
                email = userObj.getString("email"),
                name = userObj.getString("name")
            )
        }
    }

    /**
     * Cambiar contraseña contra flashcards_api/change_password.php
     */
    suspend fun changePassword(id: Long, oldPassword: String, newPassword: String): Result<String> {
        val body = JSONObject().apply {
            put("id", id)
            put("old_password", oldPassword)
            put("new_password", newPassword)
        }

        val result = postJson("change_password.php", body)
        return result.mapCatching { json ->
            json.optString("message", "Contraseña cambiada exitosamente")
        }
    }
}
