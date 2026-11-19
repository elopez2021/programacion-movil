package com.example.login.ui

import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.login.R
import com.example.login.data.ApiClient
import com.example.login.data.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}

@Composable
fun LoginScreen(
    currentLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    onLoginSuccess: (User) -> Unit,
    apiClient: ApiClient = ApiClient
) {
    val context = LocalContext.current
    val strings = remember(currentLanguage) { AppStrings.get(currentLanguage) }
    val coroutineScope = rememberCoroutineScope()

    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Form fields
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var recoveryEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // Configuración de Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val googleEmail = account.email
            val googleName = account.displayName ?: account.givenName ?: "Usuario Google"

            if (!googleEmail.isNullOrBlank()) {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null
                    val apiResult = apiClient.googleLogin(googleEmail, googleName)
                    isLoading = false
                    if (apiResult.isSuccess) {
                        val loggedUser = apiResult.getOrNull()
                        if (loggedUser != null) {
                            onLoginSuccess(loggedUser)
                        }
                    } else {
                        errorMessage = apiResult.exceptionOrNull()?.message ?: "Error al conectar cuenta con el servidor"
                    }
                }
            } else {
                errorMessage = "No se pudo obtener el correo de la cuenta de Google seleccionada"
            }
        } catch (e: ApiException) {
            // Código 12501 significa que el usuario cerró el diálogo sin seleccionar
            if (e.statusCode != 12501) {
                errorMessage = "Google Sign-In: Error código ${e.statusCode}"
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Error al iniciar con Google"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Language Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            onLanguageChange(if (currentLanguage == Language.ES) Language.EN else Language.ES)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLanguage == Language.ES) "EN 🇺🇸" else "ES 🇪🇸",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Top Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (authMode) {
                            AuthMode.REGISTER -> Icons.Default.PersonAdd
                            AuthMode.FORGOT_PASSWORD -> Icons.Default.MailOutline
                            AuthMode.LOGIN -> Icons.Default.Lock
                        },
                        contentDescription = "Auth Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Subtitle
                Text(
                    text = when (authMode) {
                        AuthMode.REGISTER -> strings.createAccount
                        AuthMode.FORGOT_PASSWORD -> strings.recoverPassword
                        AuthMode.LOGIN -> strings.welcomeBack
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (authMode) {
                        AuthMode.REGISTER -> strings.registerSubtitle
                        AuthMode.FORGOT_PASSWORD -> strings.recoverSubtitle
                        AuthMode.LOGIN -> strings.signInSubtitle
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
                    textAlign = TextAlign.Center
                )

                // Error Message banner
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Success Message banner
                AnimatedVisibility(
                    visible = successMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = successMessage ?: "",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Form fields based on AuthMode
                when (authMode) {
                    AuthMode.REGISTER -> {
                        // Full Name field
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                errorMessage = null
                            },
                            label = { Text(strings.fullName) },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = "Name")
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            label = { Text(strings.email) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email")
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Username field
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                errorMessage = null
                            },
                            label = { Text(strings.username) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Username")
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text(strings.password) },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = "Password")
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Confirm Password field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            label = { Text(strings.confirmPassword) },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = "Confirm Password")
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    AuthMode.LOGIN -> {
                        // Username or Email field
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                errorMessage = null
                            },
                            label = { Text(strings.username) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Username")
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text(strings.password) },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = "Password")
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        // Recuperar clave button / link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    authMode = AuthMode.FORGOT_PASSWORD
                                    errorMessage = null
                                    successMessage = null
                                    recoveryEmail = ""
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = strings.forgotPassword,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    AuthMode.FORGOT_PASSWORD -> {
                        // Only Email field
                        OutlinedTextField(
                            value = recoveryEmail,
                            onValueChange = {
                                recoveryEmail = it
                                errorMessage = null
                                successMessage = null
                            },
                            label = { Text(strings.email) },
                            placeholder = { Text("ejemplo@correo.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email")
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button (Sign In / Register / Send Recovery Instructions)
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        errorMessage = null
                        successMessage = null

                        when (authMode) {
                            AuthMode.REGISTER -> {
                                if (fullName.isBlank()) {
                                    errorMessage = strings.errFullName
                                    return@Button
                                }
                                if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                    errorMessage = strings.errEmail
                                    return@Button
                                }
                                if (username.isBlank()) {
                                    errorMessage = strings.errUsername
                                    return@Button
                                }
                                if (password.isBlank()) {
                                    errorMessage = strings.errPassword
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    errorMessage = strings.errPasswordMatch
                                    return@Button
                                }

                                coroutineScope.launch {
                                    isLoading = true
                                    val result = apiClient.register(username, email, password, fullName)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        val registeredUser = result.getOrNull()
                                        if (registeredUser != null) {
                                            onLoginSuccess(registeredUser)
                                        } else {
                                            successMessage = strings.successAccountCreated
                                            authMode = AuthMode.LOGIN
                                            password = ""
                                            confirmPassword = ""
                                        }
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: strings.errRegisterFailed
                                    }
                                }
                            }

                            AuthMode.LOGIN -> {
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = strings.errCredentials
                                    return@Button
                                }

                                coroutineScope.launch {
                                    isLoading = true
                                    val result = apiClient.login(username, password)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        val user = result.getOrNull()
                                        if (user != null) {
                                            onLoginSuccess(user)
                                        }
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: strings.errInvalidLogin
                                    }
                                }
                            }

                            AuthMode.FORGOT_PASSWORD -> {
                                if (recoveryEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(recoveryEmail.trim()).matches()) {
                                    errorMessage = strings.errEmail
                                    return@Button
                                }

                                coroutineScope.launch {
                                    isLoading = true
                                    val result = apiClient.recoverPassword(recoveryEmail)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        successMessage = result.getOrNull() ?: strings.successEmailSent
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Error al solicitar recuperación"
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = when (authMode) {
                                AuthMode.REGISTER -> strings.register
                                AuthMode.FORGOT_PASSWORD -> strings.sendInstructions
                                AuthMode.LOGIN -> strings.signIn
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Google Sign-In button (only shown on Login and Register modes)
                if (authMode != AuthMode.FORGOT_PASSWORD) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "  ${strings.orContinueWith}  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            errorMessage = null
                            successMessage = null
                            // Desconectar sesión previa para que siempre aparezca el selector de cuentas
                            googleSignInClient.signOut().addOnCompleteListener {
                                val signInIntent = googleSignInClient.signInIntent
                                googleLauncher.launch(signInIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = strings.continueWithGoogle,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode switch links
                when (authMode) {
                    AuthMode.LOGIN -> {
                        TextButton(
                            onClick = {
                                authMode = AuthMode.REGISTER
                                errorMessage = null
                                successMessage = null
                                password = ""
                                confirmPassword = ""
                            },
                            enabled = !isLoading
                        ) {
                            Text(
                                text = strings.dontHaveAccount,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    AuthMode.REGISTER -> {
                        TextButton(
                            onClick = {
                                authMode = AuthMode.LOGIN
                                errorMessage = null
                                successMessage = null
                                password = ""
                                confirmPassword = ""
                            },
                            enabled = !isLoading
                        ) {
                            Text(
                                text = strings.alreadyHaveAccount,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    AuthMode.FORGOT_PASSWORD -> {
                        TextButton(
                            onClick = {
                                authMode = AuthMode.LOGIN
                                errorMessage = null
                                successMessage = null
                                recoveryEmail = ""
                            },
                            enabled = !isLoading
                        ) {
                            Text(
                                text = strings.backToLogin,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
