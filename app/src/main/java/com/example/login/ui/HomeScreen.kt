package com.example.login.ui

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.login.data.ApiClient
import com.example.login.data.User
import kotlinx.coroutines.launch

enum class HomeTab {
    DASHBOARD,
    PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    currentLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    onUserUpdated: (User) -> Unit,
    onSignOut: () -> Unit,
    apiClient: ApiClient = ApiClient
) {
    val strings = remember(currentLanguage) { AppStrings.get(currentLanguage) }
    var currentTab by remember { mutableStateOf(HomeTab.DASHBOARD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentTab == HomeTab.DASHBOARD) strings.dashboard else strings.myProfile,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
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
                            text = if (currentLanguage == Language.ES) "EN" else "ES",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = strings.signOut,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentTab == HomeTab.DASHBOARD,
                    onClick = { currentTab = HomeTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Home, contentDescription = strings.dashboard) },
                    label = { Text(strings.home) }
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.PROFILE,
                    onClick = { currentTab = HomeTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, contentDescription = strings.myProfile) },
                    label = { Text(strings.myProfile) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            when (currentTab) {
                HomeTab.DASHBOARD -> {
                    DashboardContent(
                        user = user,
                        strings = strings,
                        onGoToProfile = { currentTab = HomeTab.PROFILE }
                    )
                }
                HomeTab.PROFILE -> {
                    ProfileContent(
                        user = user,
                        strings = strings,
                        apiClient = apiClient,
                        onUserUpdated = onUserUpdated
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    user: User,
    strings: AppStrings,
    onGoToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Greeting Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${strings.hello}, ${user.name}!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilledTonalButton(
                    onClick = onGoToProfile,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.myProfile)
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: User,
    strings: AppStrings,
    apiClient: ApiClient,
    onUserUpdated: (User) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var editName by remember(user) { mutableStateOf(user.name) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var passVisible by remember { mutableStateOf(false) }

    var isSavingProfile by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }

    var profileSuccessMsg by remember { mutableStateOf<String?>(null) }
    var profileErrorMsg by remember { mutableStateOf<String?>(null) }

    var passSuccessMsg by remember { mutableStateOf<String?>(null) }
    var passErrorMsg by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 1: Edit Name
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.personalInfo,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedVisibility(
                    visible = profileSuccessMsg != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = profileSuccessMsg ?: "",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = profileErrorMsg != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = profileErrorMsg ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = editName,
                    onValueChange = {
                        editName = it
                        profileErrorMsg = null
                        profileSuccessMsg = null
                    },
                    label = { Text(strings.fullName) },
                    singleLine = true,
                    enabled = !isSavingProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        profileErrorMsg = null
                        profileSuccessMsg = null

                        if (editName.isBlank()) {
                            profileErrorMsg = strings.errFullName
                            return@Button
                        }

                        coroutineScope.launch {
                            isSavingProfile = true
                            val result = apiClient.updateProfile(user.id, editName)
                            isSavingProfile = false
                            if (result.isSuccess) {
                                val updatedUser = result.getOrNull()
                                if (updatedUser != null) {
                                    profileSuccessMsg = strings.successProfileUpdated
                                    onUserUpdated(updatedUser)
                                }
                            } else {
                                profileErrorMsg = result.exceptionOrNull()?.message ?: "Error al actualizar perfil"
                            }
                        }
                    },
                    enabled = !isSavingProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSavingProfile) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.saveChanges)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 2: Change Password
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.securitySection,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedVisibility(
                    visible = passSuccessMsg != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = passSuccessMsg ?: "",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = passErrorMsg != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = passErrorMsg ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Current password
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        passErrorMsg = null
                        passSuccessMsg = null
                    },
                    label = { Text(strings.currentPassword) },
                    visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isChangingPassword,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { passVisible = !passVisible }) {
                            Icon(
                                imageVector = if (passVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // New password
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passErrorMsg = null
                        passSuccessMsg = null
                    },
                    label = { Text(strings.newPassword) },
                    visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isChangingPassword,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm new password
                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = {
                        confirmNewPassword = it
                        passErrorMsg = null
                        passSuccessMsg = null
                    },
                    label = { Text(strings.confirmNewPassword) },
                    visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isChangingPassword,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                FilledTonalButton(
                    onClick = {
                        focusManager.clearFocus()
                        passErrorMsg = null
                        passSuccessMsg = null

                        if (currentPassword.isBlank()) {
                            passErrorMsg = strings.errPassword
                            return@FilledTonalButton
                        }
                        if (newPassword.isBlank()) {
                            passErrorMsg = strings.errPassword
                            return@FilledTonalButton
                        }
                        if (newPassword != confirmNewPassword) {
                            passErrorMsg = strings.errPasswordMatch
                            return@FilledTonalButton
                        }

                        coroutineScope.launch {
                            isChangingPassword = true
                            val result = apiClient.changePassword(user.id, currentPassword, newPassword)
                            isChangingPassword = false
                            if (result.isSuccess) {
                                passSuccessMsg = strings.successPasswordChanged
                                currentPassword = ""
                                newPassword = ""
                                confirmNewPassword = ""
                            } else {
                                val err = result.exceptionOrNull()
                                if (err?.message == "WRONG_OLD_PASSWORD") {
                                    passErrorMsg = strings.errCurrentPasswordWrong
                                } else {
                                    passErrorMsg = err?.message ?: "Error al cambiar contraseña"
                                }
                            }
                        }
                    },
                    enabled = !isChangingPassword,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isChangingPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.changePassword)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
