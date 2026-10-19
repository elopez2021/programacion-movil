# 📱 Aplicación Android: Login, Registro, Recuperar Clave y Mi Perfil

Una aplicación móvil Android nativa, simple y elegante, desarrollada con **Kotlin**, **Jetpack Compose** (Material 3) y base de datos nativa **SQLite** (`SQLiteOpenHelper`).

---

## ✨ Funcionalidades Principales

1. **Autenticación Completa con SQLite**:
   - **Iniciar Sesión**: Autenticación segura mediante usuario y contraseña (con hash SHA-256).
   - **Registrarse**: Creación de nuevas cuentas (Nombre completo, usuario, contraseña y confirmación) con validación de campos y unicidad de usuario.
   - **Recuperar Clave**:
     - Permite ingresar el nombre de usuario y definir una nueva contraseña.
     - Valida la existencia del usuario en la base de datos SQLite y actualiza su contraseña de forma segura.
   - **Cerrar Sesión**: Limpia el estado de la sesión y retorna a la pantalla de login con transiciones animadas.

2. **Mi Perfil**:
   - Barra de navegación inferior sencilla ("Inicio" y "Mi Perfil").
   - Muestra avatar, identificador único (ID de SQLite) y nombre de usuario.
   - **Editar datos personales**: Permite actualizar el nombre completo en la base de datos SQLite con confirmación visual.
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
│   │   │   │   └── DatabaseHelper.kt         # SQLiteOpenHelper (login, registro, recuperación y perfil)
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

## 🚀 Cómo Ejecutar

### Opción 1: En Android Studio
1. Abre **Android Studio**.
2. Selecciona la carpeta del proyecto (`E:\programacion movil\login`).
3. Presiona el botón verde **Run ▶** con tu emulador o dispositivo físico conectado.

### Opción 2: Compilación por consola
```bash
.\gradlew.bat assembleDebug
```
El archivo APK se genera en:
`app\build\outputs\apk\debug\app-debug.apk`
