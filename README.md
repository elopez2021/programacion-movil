# 📱 Aplicación Android: Login, Registro, Recuperar Clave y Mi Perfil

Una aplicación móvil Android nativa, simple y moderna, desarrollada con **Kotlin**, **Jetpack Compose** (Material 3) y conectada a una API backend en **PHP con MySQL (phpMyAdmin)** (`flashcards_api`).

---

## ✨ Funcionalidades Principales

1. **Autenticación Completa con API REST y Base de Datos Normalizada (3NF)**:
   - **Tabla catálogo `auth_providers`**: Contiene los métodos (`1: local`, `2: google`).
   - **Clave Foránea (`FOREIGN KEY`) en `users`**: Cada usuario tiene un único `auth_provider_id` vinculado.
   - **Contraseña `NULL` para usuarios de Google**: Las cuentas de Google no guardan contraseñas locales; la autenticación la gestiona Google.
   - **Exclusividad de método**: Si un usuario se registró con Google, solo puede entrar con Google; si se registró con contraseña, solo entra con su clave.
   - **Iniciar Sesión**: Autenticación segura mediante usuario o correo y contraseña (hash SHA-256).
   - **Registrarse**: Creación de nuevas cuentas con validación de campos y duplicados.
   - **Google Sign-In**: Diálogo nativo de Google Play Services con registro/login automático en MySQL.
   - **Recuperar Clave**: Solicitud de instrucciones al correo registrado.
   - **Cerrar Sesión**: Limpia el estado de la sesión y retorna a la pantalla de login con transiciones animadas.

2. **Mi Perfil**:
   - Barra de navegación inferior ("Inicio" y "Mi Perfil").
   - Muestra avatar, nombre completo, correo electrónico y `@usuario`.
   - **Editar datos personales**: Actualiza el nombre completo en MySQL con confirmación visual.
   - **Cambiar contraseña**: Permite actualizar la clave verificando primero la contraseña actual del usuario.

3. **Soporte Bilingüe (Español / Inglés)**:
   - Selector de idioma en tiempo real (ES / EN) en la barra superior.

---

## 🛠️ Estructura del Proyecto

```
login/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/login/
│   │   │   ├── MainActivity.kt               # Punto de entrada y gestión del estado de sesión
│   │   │   ├── data/
│   │   │   │   ├── User.kt                   # Modelo de datos del usuario
│   │   │   │   └── ApiClient.kt              # Cliente HTTP asíncrono para flashcards_api
│   │   │   └── ui/
│   │   │       ├── LoginScreen.kt            # Compose UI: Login, Registro y Recuperar Clave
│   │   │       ├── HomeScreen.kt             # Compose UI: Dashboard y Mi Perfil
│   │   │       ├── Strings.kt                # Textos localizados (Español / English)
│   │   │       └── theme/                    # Colores, Tipografía y Tema Material 3
│   │   └── res/                              # Iconos y recursos XML
│   └── build.gradle                          # Configuración del módulo de la app
├── build.gradle                              # Configuración raíz
└── settings.gradle                           # Repositorios y módulos
```

---

## 🚀 Conexión con la API Local por Cable USB

1. Inicia **Apache** y **MySQL** en tu panel de XAMPP.
2. Conecta tu teléfono por USB con la **Depuración USB** activada.
3. Ejecuta en la terminal de tu PC:
   ```bash
   adb reverse tcp:80 tcp:80
   ```
4. Presiona **Run ▶** en Android Studio.
