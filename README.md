# Focus Planner

Focus Planner es una aplicación móvil de productividad desarrollada con Android (Jetpack Compose) y un backend en Spring Boot. Está diseñada para ayudar a los usuarios a gestionar sus tareas diarias de forma eficiente, integrando funcionalidades como gestión de tareas, temporizador Pomodoro, estadísticas visuales, sincronización con Google Calendar y personalización del perfil.

## Características principales

- Registro y login con verificación de correo electrónico
- Creación, edición, eliminación y filtrado de tareas
- Calendario mensual interactivo con tareas organizadas por fecha y estado
- Sincronización con Google Calendar (creación y eliminación de eventos)
- Estadísticas de uso y rendimiento del usuario con gráficos
- Temporizador Pomodoro con notificaciones y control de ciclos
- Exportación e importación de tareas
- Perfil de usuario editable con foto personalizada
- Sección de ajustes con opciones avanzadas

## Tecnologías utilizadas

### Frontend (Android)
- Kotlin
- Jetpack Compose
- Retrofit 2
- StateFlow
- SharedPreferences / DataStore
- Notificaciones locales

### Backend
- Java
- Spring Boot
- Spring Security con JWT personalizado (HS256)
- MySQL
- Swagger / OpenAPI
- Ngrok para pruebas remotas

### Servicios y APIs externas
- Google Calendar API (OAuth2)
- Envío de correo de verificación
- Postman y Swagger para documentación de endpoints

## Pruebas realizadas

- Pruebas funcionales de tareas: creación, edición, borrado y exportación
- Sincronización con Google Calendar verificada en múltiples casos
- Expiración y renovación de tokens JWT (pruebas de seguridad)
- Calendario visual: carga correcta por mes, colores por estado
- Rendimiento: prueba de carga de 100 tareas, respuesta media bajo 400ms
- Pomodoro completo sin pérdidas de rendimiento ni consumo excesivo de memoria
- Pruebas manuales en múltiples dispositivos Android
- Registro de logs y validación de errores

## Seguridad

- Autenticación con JWT y refresh tokens
- Verificación de cuenta mediante token enviado por correo
- Protección de rutas por usuario y validación de sesión
- Almacenamiento seguro de credenciales en backend

## Despliegue previsto

Para llevar el sistema a producción, se plantean los siguientes pasos:

1. Adquisición de un dominio propio (por ejemplo: focusplanner.app)
2. Hosting del backend (opciones: Google Cloud, Railway, Render)
3. Base de datos remota segura (Google Cloud SQL, PlanetScale)
4. Cumplimiento de requisitos de seguridad y permisos de Google Play
5. Publicación en Google Play Store con revisión del .apk y políticas de uso

## Estructura del proyecto

- Backend: Spring Boot con estructura MVC (controladores, servicios, modelos)
- Frontend: App Android modularizada por pantallas, ViewModel por cada vista, patrón MVVM
- Base de datos: MySQL con relaciones entre usuarios, tareas y tokens

## Documentación complementaria

Este proyecto se ha documentado como Trabajo de Fin de Grado e incluye los siguientes anexos:

- Anexo A: Fragmentos de código clave del backend (controladores y servicios)
- Anexo B: Estructura relacional de la base de datos
- Anexo C: Tabla de endpoints disponibles en la API
- Anexo D: Configuración de sincronización con Google Calendar
- Anexo E: Configuración de Retrofit y autenticación en el cliente Android

## Créditos y agradecimientos

Este proyecto ha sido desarrollado como parte del Trabajo de Fin de Grado en Desarrollo de Aplicaciones Multiplataforma. Se agradece especialmente al tutor Pedro por su orientación técnica, así como a los familiares y compañeros que colaboraron con pruebas y sugerencias durante el proceso de desarrollo.

## Licencia

Proyecto desarrollado con fines educativos. Su uso está permitido para aprendizaje o pruebas no comerciales. Para uso comercial, contactar con el autor.
