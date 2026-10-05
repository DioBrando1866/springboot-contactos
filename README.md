# Contactos — Spring Boot + Kotlin

Clon del proyecto Symfony `symfony-contactos` reescrito entero con **Spring Boot 4.1**, **Kotlin**,
**Spring MVC + Thymeleaf**, **Spring Security** y **Spring Data JPA**.

## Requisitos

- JDK 17 o superior
- Maven 3.9+ (o IntelliJ IDEA / VS Code, que ya lo traen: basta con abrir la carpeta como proyecto Maven)
- MySQL 8 **o** Docker (para levantar MySQL) **o** nada (perfil H2 en memoria)

## Arranque

### Opción A — MySQL (igual que el proyecto original)

Los valores por defecto son los del `.env` de Symfony: `root` / `sa` / base de datos `contactos` en `127.0.0.1:3306`.

```bash
docker compose up -d          # opcional: levanta un MySQL con esos mismos datos
mvn spring-boot:run
```

Si tu MySQL tiene otros datos, no hace falta tocar código:

```bash
DB_URL="jdbc:mysql://localhost:3306/contactos?createDatabaseIfNotExist=true" DB_USER=mi_usuario DB_PASSWORD=mi_clave mvn spring-boot:run
```

### Opción B — Sin instalar nada (H2 en memoria)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

Los datos se pierden al parar la aplicación.

Después abre <http://localhost:8080>. Te redirigirá a `/login`; pulsa **Registrarse** para crear tu usuario
(queda con la sesión iniciada automáticamente).

> Para generar el wrapper de Maven (`mvnw`) ejecuta una vez: `mvn wrapper:wrapper`.

## Equivalencias con el proyecto Symfony

| Symfony | Spring Boot + Kotlin |
|---|---|
| `src/Controller/PageController` (`/`, `/page`) | `controller/PageController.kt` |
| `src/Controller/ContactoController` | `controller/ContactoController.kt` |
| `src/Controller/SecurityController` (`/login`, `/logout`) | `controller/SecurityController.kt` + `config/SecurityConfig.kt` |
| `src/Controller/RegistrationController` | `controller/RegistrationController.kt` |
| `src/Entity/*` (Doctrine) | `entity/*.kt` (JPA/Hibernate) |
| `src/Repository/*` | `repository/*.kt` (Spring Data JPA) |
| `src/Form/*FormType` + Validator | `form/*.kt` (Bean Validation) |
| `config/packages/security.yaml` | `config/SecurityConfig.kt` + `security/AppUserDetailsService.kt` |
| `migrations/*` | `spring.jpa.hibernate.ddl-auto=update` + `config/ProvinciaSeeder.kt` |
| `templates/*.twig` | `src/main/resources/templates/*.html` (Thymeleaf) |
| `public/css/estilos.css` | `src/main/resources/static/css/estilos.css` |
| `.env` (`DATABASE_URL`) | `application.yml` (`DB_URL`, `DB_USER`, `DB_PASSWORD`) |

Rutas: `/`, `/contacto/nuevo`, `/contacto/{codigo}`, `/contacto/editar/{codigo}`, `/contacto/borrar/{codigo}`,
`/login`, `/logout`, `/register`, `/page`.

## Diferencias respecto al original

- **Borrar y Logout usan POST** (con token CSRF) en lugar de enlaces GET. Es la práctica segura; visualmente son botones.
- **Tabla `app_user`** en lugar de `user` (palabra reservada en varias bases de datos). El nombre del usuario
  ahora **se guarda** (en Symfony el campo `name` no se persistía).
- **Provincias precargadas** (las 52 de España) al primer arranque; en el original la tabla quedaba vacía.
- Se ha quitado el campo "Name" del login: era obligatorio en el HTML pero Symfony nunca lo usaba.
- Los emails se guardan en minúsculas.
- El esquema lo crea Hibernate (`ddl-auto=update`); es cómodo para un proyecto de aprendizaje, pero para
  producción conviene Flyway o Liquibase.
