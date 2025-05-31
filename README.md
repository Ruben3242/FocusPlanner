# Focus Planner

**Focus Planner** es una aplicación móvil de productividad que permite gestionar tareas, sincronizarlas con Google Calendar, utilizar la técnica Pomodoro y visualizar estadísticas personalizadas. Está desarrollada en **Android (Jetpack Compose)** y conectada a un backend en **Spring Boot** con base de datos **MySQL**.

## Funcionalidades principales

- Registro e inicio de sesión con verificación por correo
- Gestión completa de tareas: crear, editar, eliminar y filtrar
- Calendario mensual interactivo con codificación visual por estado
- Temporizador Pomodoro configurable con notificaciones
- Sincronización de tareas con Google Calendar
- Perfil de usuario editable y estadísticas en tiempo real
- Exportación e importación de tareas
- Ajustes avanzados de cuenta y notificaciones

## Tecnologías utilizadas

### Frontend
- Kotlin
- Jetpack Compose
- Retrofit 2
- StateFlow
- Notifications
- DataStore / SharedPreferences

### Backend
- Java 17
- Spring Boot
- Spring Security + JWT personalizado
- MySQL
- Swagger / OpenAPI
- Ngrok (durante desarrollo local)

### APIs y servicios externos
- Google Calendar API (OAuth2)
- Verificación de cuenta por email

## Instalación local

### Requisitos

- Android Studio (Gira en API 33+)
- Java 17
- MySQL
- Postman o Swagger para pruebas de API

### Pasos

#### 1. Clona este repositorio

```bash
git clone https://github.com/tuusuario/focus-planner.git
cd focus-planner
