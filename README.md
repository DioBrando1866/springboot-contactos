# Contactos — Spring Boot + Kotlin

Una agenda web de contactos con usuarios, hecha con **Spring Boot** y **Kotlin**.
Es la versión Java/Kotlin de un proyecto que antes estaba hecho con Symfony (PHP).

Este documento está escrito para alguien que **nunca ha usado Spring Boot**. Primero verás cómo
arrancar el proyecto, y después cómo funciona por dentro, pieza a pieza, usando el código real
de este repositorio como ejemplo.

---

## Índice

1. [Qué hace la aplicación](#1-qué-hace-la-aplicación)
2. [Cómo arrancarla](#2-cómo-arrancarla)
3. [Qué es Spring Boot](#3-qué-es-spring-boot)
4. [Estructura del proyecto](#4-estructura-del-proyecto)
5. [El viaje de una petición](#5-el-viaje-de-una-petición)
6. [Las piezas, una a una](#6-las-piezas-una-a-una)
7. [Caso completo: crear un contacto](#7-caso-completo-crear-un-contacto)
8. [Cómo modificar el proyecto](#8-cómo-modificar-el-proyecto)
9. [Problemas frecuentes](#9-problemas-frecuentes)
10. [Si vienes de Symfony](#10-si-vienes-de-symfony)
11. [Glosario](#11-glosario)

---

## 1. Qué hace la aplicación

- Te **registras** y haces **login** con email y contraseña.
- Una vez dentro puedes **ver, crear, editar y borrar contactos**.
- Cada contacto tiene nombre, teléfono, email y una provincia (de las 52 de España).
- Si no has iniciado sesión, cualquier página te redirige al login.

| Ruta | Qué hace | ¿Pública? |
|---|---|---|
| `GET /` | Lista de contactos | No |
| `GET /contacto/{codigo}` | Ficha de un contacto | No |
| `GET /contacto/nuevo` · `POST /contacto/nuevo` | Formulario de alta / guardar | No |
| `GET /contacto/editar/{codigo}` · `POST …` | Formulario de edición / guardar | No |
| `POST /contacto/borrar/{codigo}` | Borra un contacto | No |
| `GET /login` · `POST /login` | Formulario de acceso / comprobar credenciales | Sí |
| `POST /logout` | Cerrar sesión | No |
| `GET /register` · `POST /register` | Formulario de registro / crear usuario | Sí |
| `GET /page` | Devuelve un JSON de ejemplo | Sí |

---

## 2. Cómo arrancarla

### Requisitos

- **JDK 17 o superior** (Java). Comprueba con `java -version`.
- **Maven 3.9+**, o un IDE que lo traiga (IntelliJ IDEA y VS Code lo incluyen: basta abrir la carpeta como proyecto Maven).
- Una base de datos: **MySQL**, o nada (perfil `h2`, ver abajo).

### Opción A — Sin instalar nada (H2 en memoria)

La más rápida para probar. Los datos se pierden al parar la aplicación.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

En IntelliJ: *Run → Edit Configurations →* tu configuración *→ Active profiles:* `h2`.

### Opción B — Con MySQL en Docker

El fichero `compose.yaml` levanta un MySQL con usuario `root`, contraseña `sa` y base de datos `contactos`.

```bash
docker compose up -d
docker compose ps         # espera a que ponga "healthy" (20-40 s la primera vez)
mvn spring-boot:run
```

### Opción C — Con tu propio MySQL

Por defecto la aplicación intenta conectar a `127.0.0.1:3306` con `root` / `sa` y la base `contactos`
(se crea sola si no existe). Para usar otros datos no hace falta tocar el código; usa variables de entorno:

```bash
DB_URL="jdbc:mysql://127.0.0.1:3306/contactos?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true" \
DB_USER=mi_usuario DB_PASSWORD=mi_clave \
mvn spring-boot:run
```

### Probarla

Abre <http://localhost:8080>. Te llevará a `/login`. Pulsa **Registrarse**, crea tu usuario
(quedas con la sesión iniciada) y empieza a crear contactos.

> Para generar el wrapper de Maven (`mvnw`, que evita instalar Maven) ejecuta una vez: `mvn wrapper:wrapper`.

Si algo falla, mira la sección [Problemas frecuentes](#9-problemas-frecuentes).

---

## 3. Qué es Spring Boot

### El problema que resuelve

Para hacer una web en Java "a mano" necesitarías: un servidor web, una librería para atender rutas,
otra para hablar con la base de datos, otra para las plantillas HTML, otra para la seguridad… y
configurar cada una y hacer que cooperen entre sí. Eso era lento y tedioso.

**Spring** es un conjunto de librerías ("framework") que ya hacen todo eso y están pensadas para
encajar entre sí. **Spring Boot** es la capa que las configura por ti con valores sensatos, de modo
que una aplicación arranca con muy poco código.

Piensa en Spring como las piezas de una cocina, y en Spring Boot como la cocina ya montada:
sólo tienes que ponerte a cocinar.

### Las cuatro ideas que lo explican casi todo

**1. Starters: dependencias por temas.**
En el `pom.xml` no pides librerías sueltas, sino "paquetes temáticos":

| Starter | Para qué sirve |
|---|---|
| `spring-boot-starter-webmvc` | Recibir peticiones web (servidor Tomcat incluido) |
| `spring-boot-starter-thymeleaf` | Generar páginas HTML con plantillas |
| `spring-boot-starter-data-jpa` | Hablar con la base de datos usando objetos |
| `spring-boot-starter-security` | Login, contraseñas, permisos |
| `spring-boot-starter-validation` | Validar formularios (campo obligatorio, longitud…) |

Cada starter arrastra todas las librerías que necesita. Con una línea tienes una capacidad entera.

**2. Servidor embebido.**
No instalas Tomcat ni despliegas nada. La propia aplicación **contiene** el servidor web y lo arranca.
Por eso ejecutas un programa normal (`main`) y ya está escuchando en el puerto 8080.

**3. Autoconfiguración.**
Spring Boot mira qué librerías hay en tu proyecto y se configura solo. Si ve el driver de MySQL y una
URL en `application.yml`, crea la conexión a la base de datos. Si ve Thymeleaf, busca plantillas en
`src/main/resources/templates`. Tú sólo cambias lo que quieras personalizar.

**4. Inyección de dependencias (la idea más importante).**
En vez de crear tú los objetos que necesitas (`val repo = ContactoRepository()`), **los pides** y Spring
te los entrega ya creados. Mira este constructor real:

```kotlin
@Controller
class PageController(
    private val contactos: ContactoRepository,   // <- Spring me lo da, yo no lo creo
)
```

Al arrancar, Spring crea un objeto de cada clase marcada (`@Controller`, `@Service`, `@Component`,
`@Configuration`…). A esos objetos gestionados por Spring se les llama **beans**. Cuando un bean
necesita a otro, lo declara en su constructor y Spring lo conecta. Esto se llama *inyección de dependencias*
y evita tener objetos creándose unos a otros por todo el código.

### Las anotaciones (`@Algo`)

Spring funciona con **anotaciones**: etiquetas que pones encima de clases o funciones para decirle a
Spring qué son o qué hacer con ellas. No ejecutan código por sí mismas; Spring las lee al arrancar.

| Anotación | Significado |
|---|---|
| `@SpringBootApplication` | "Aquí empieza la aplicación; busca mis clases a partir de este paquete" |
| `@Controller` | "Esta clase atiende peticiones web" |
| `@GetMapping("/ruta")` / `@PostMapping("/ruta")` | "Esta función atiende GET/POST a esa ruta" |
| `@Entity` | "Esta clase representa una tabla de la base de datos" |
| `@Service`, `@Component` | "Crea un objeto de esta clase y guárdalo para inyectarlo donde se pida" |
| `@Configuration` + `@Bean` | "Aquí se configuran cosas; lo que devuelva esta función es un bean" |
| `@Valid` | "Valida este objeto antes de ejecutar la función" |

### Kotlin en cinco minutos

El código está en **Kotlin**, un lenguaje moderno que funciona sobre la máquina virtual de Java.
Lo mínimo para leerlo:

```kotlin
val nombre = "Ana"          // valor que no cambia (como const)
var edad = 30               // variable que sí puede cambiar
var apellido: String? = null // el "?" significa "puede ser null"; sin "?" nunca lo es

fun saludar(nombre: String): String {   // función: el tipo va DESPUÉS del nombre
    return "Hola $nombre"               // $nombre inserta la variable en el texto
}

class Persona(val nombre: String)       // clase con un constructor en la propia cabecera

provincia?.nombre                       // "si provincia no es null, dame su nombre; si no, null"
x ?: "valor por defecto"                // "si x es null, usa esto" (operador Elvis)
```

No hay `;` al final de cada línea y los tipos se infieren casi siempre.

---

## 4. Estructura del proyecto

```
contactos-springboot/
├── pom.xml                  ← Maven: dependencias y cómo se compila
├── compose.yaml             ← Docker: levanta un MySQL de desarrollo
├── README.md                ← este documento
└── src/
    ├── main/
    │   ├── kotlin/com/example/contactos/     ← TODO el código Kotlin
    │   │   ├── ContactosApplication.kt       ← punto de entrada (main)
    │   │   ├── config/                       ← configuración
    │   │   │   ├── SecurityConfig.kt         ·  reglas de seguridad y login
    │   │   │   └── ProvinciaSeeder.kt        ·  carga las 52 provincias al arrancar
    │   │   ├── controller/                   ← reciben las peticiones web
    │   │   │   ├── PageController.kt         ·  inicio ( / ) y /page
    │   │   │   ├── ContactoController.kt     ·  CRUD de contactos
    │   │   │   ├── SecurityController.kt     ·  pantalla de login
    │   │   │   └── RegistrationController.kt ·  registro de usuarios
    │   │   ├── entity/                       ← clases = tablas de la base de datos
    │   │   │   ├── Contacto.kt
    │   │   │   ├── Provincia.kt
    │   │   │   └── User.kt
    │   │   ├── repository/                   ← acceso a la base de datos
    │   │   │   ├── ContactoRepository.kt
    │   │   │   ├── ProvinciaRepository.kt
    │   │   │   └── UserRepository.kt
    │   │   ├── form/                         ← datos de los formularios + validaciones
    │   │   │   ├── ContactoForm.kt
    │   │   │   └── RegistrationForm.kt
    │   │   └── security/
    │   │       └── AppUserDetailsService.kt  ← cómo se busca a un usuario al hacer login
    │   └── resources/
    │       ├── application.yml               ← configuración (base de datos, etc.)
    │       ├── application-h2.yml            ← configuración extra del perfil "h2"
    │       ├── static/css/estilos.css        ← ficheros estáticos (CSS, imágenes…)
    │       └── templates/                    ← páginas HTML (Thymeleaf)
    │           ├── fragments/base.html       ·  estructura común de todas las páginas
    │           ├── inicio.html, contacto.html, nuevo.html, editar.html, error.html
    │           ├── partials/                 ·  trozos reutilizables
    │           ├── registration/register.html
    │           └── security/login.html
    └── test/                                 ← pruebas automáticas
```

Regla útil: **todo lo que es lógica va en `kotlin/`; todo lo que es visual o configuración va en `resources/`.**

El `pom.xml` también incluye el plugin de Kotlin con "all-open" y "no-arg". Kotlin hace las clases
cerradas por defecto, pero Spring e Hibernate necesitan poder extenderlas; esos plugins las abren
automáticamente. Es configuración que no tendrás que tocar.

---

## 5. El viaje de una petición

Cuando escribes `http://localhost:8080/contacto/5` en el navegador ocurre esto:

```
 Navegador
    │  GET /contacto/5
    ▼
 Tomcat (servidor embebido)
    ▼
 Filtros de Spring Security ── ¿has iniciado sesión? ── no ──► redirige a /login
    │ sí
    ▼
 DispatcherServlet  ("el recepcionista": decide qué función atiende la ruta)
    ▼
 ContactoController.ficha(5)       ← tu código
    │   pide el contacto a…
    ▼
 ContactoRepository.findById(5)  ──►  Hibernate  ──►  SQL  ──►  Base de datos
    │   ◄───────────────── objeto Contacto ─────────────────────
    │   lo mete en el "modelo" y devuelve el nombre de la vista: "contacto"
    ▼
 Thymeleaf: abre templates/contacto.html, rellena los huecos con el modelo
    ▼
 HTML final  ──►  Navegador
```

Es el patrón **MVC** (Modelo-Vista-Controlador):

- **Controlador** (`controller/`): recibe la petición y decide qué hacer.
- **Modelo**: los datos que el controlador prepara (`model.addAttribute("contacto", contacto)`).
- **Vista** (`templates/`): el HTML que muestra esos datos.

---

## 6. Las piezas, una a una

### 6.1 Punto de entrada

```kotlin
@SpringBootApplication
class ContactosApplication

fun main(args: Array<String>) {
    runApplication<ContactosApplication>(*args)
}
```

`main` arranca Spring. `@SpringBootApplication` le indica que busque beans en este paquete y sus
subpaquetes (por eso todo el código debe vivir dentro de `com.example.contactos`).

### 6.2 Entidades: clases que son tablas

Una **entidad** es una clase cuyos objetos se guardan como filas de una tabla. Esto es **JPA**
(el estándar de Java) y su implementación **Hibernate**: tú trabajas con objetos y Hibernate escribe el SQL.

```kotlin
@Entity
@Table(name = "contacto")
class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null            // clave primaria, autoincremental

    @Column(nullable = false)
    var nombre: String = ""

    @Column(nullable = false, length = 15)
    var telefono: String = ""

    @ManyToOne
    @JoinColumn(name = "provincia_id")
    var provincia: Provincia? = null   // relación: muchos contactos → una provincia
}
```

- `@Id` marca la clave primaria; `@GeneratedValue` hace que la base de datos la genere.
- `@Column` ajusta la columna (obligatoria, longitud…).
- `@ManyToOne` expresa una relación: **muchos** contactos pertenecen a **una** provincia.
  En la base de datos eso es una columna `provincia_id`.

**No hay que escribir `CREATE TABLE`.** Con `ddl-auto: update` (en `application.yml`) Hibernate crea
y actualiza las tablas leyendo estas clases al arrancar.

### 6.3 Repositorios: acceder a los datos sin escribir SQL

```kotlin
interface ContactoRepository : JpaRepository<Contacto, Long>
```

Esa única línea te regala, sin implementar nada: `findAll()`, `findById(id)`, `save(x)`, `delete(x)`,
`count()`… Spring Data genera la implementación al arrancar.

Además puede **deducir consultas del nombre de la función**:

```kotlin
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?        // SELECT … WHERE email = ?
    fun existsByEmail(email: String): Boolean    // ¿existe alguno con ese email?
}

interface ProvinciaRepository : JpaRepository<Provincia, Long> {
    fun findAllByOrderByNombreAsc(): List<Provincia>   // todas, ordenadas por nombre
}
```

Se lee casi como inglés: `find` + `By` + `Email`. Spring interpreta el nombre y crea la consulta.

### 6.4 Controladores: reciben las peticiones

```kotlin
@Controller
class ContactoController(
    private val contactos: ContactoRepository,
    private val provincias: ProvinciaRepository,
) {

    @GetMapping("/contacto/{codigo}")
    fun ficha(@PathVariable("codigo") codigo: Long, model: Model): String {
        val contacto = contactos.findById(codigo)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado el contacto") }
        model.addAttribute("contacto", contacto)
        return "contacto"
    }
}
```

Línea a línea:

- `@GetMapping("/contacto/{codigo}")`: esta función atiende `GET /contacto/lo-que-sea`.
- `@PathVariable("codigo") codigo: Long`: el trozo `{codigo}` de la URL llega como parámetro,
  ya convertido a número. Si escribes `/contacto/abc`, Spring responde 400 solo.
- `model: Model`: la "bandeja" donde dejas datos para la plantilla. Spring te la pasa.
- `return "contacto"`: **no devuelve HTML**, devuelve el *nombre* de la plantilla
  (`templates/contacto.html`). Thymeleaf se encarga del resto.
- Si no existe, lanza un 404 con un mensaje, que muestra `templates/error.html`.
- `return "redirect:/"` (en otras funciones) significa "ve a la portada" en lugar de mostrar una vista.

### 6.5 Formularios y validación

Los datos de un formulario se reciben en una clase sencilla, con las reglas de validación escritas
como anotaciones:

```kotlin
class ContactoForm {
    @field:NotBlank(message = "El nombre no puede estar vacío")
    @field:Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    var nombre: String = ""

    @field:NotBlank(message = "El email no puede estar vacío")
    @field:Email(message = "El email no es válido")
    var email: String = ""

    var provinciaId: Long? = null      // el id de la provincia elegida en el desplegable
}
```

(El `@field:` es una particularidad de Kotlin: indica que la anotación va en el campo.)

En el controlador, `@Valid` activa esas reglas y `BindingResult` guarda los errores:

```kotlin
@PostMapping("/contacto/nuevo")
fun guardarNuevo(
    @Valid @ModelAttribute("contactoForm") form: ContactoForm,   // datos del formulario, validados
    binding: BindingResult,                                       // resultado de la validación
    model: Model,
): String {
    if (binding.hasErrors()) {
        // hay errores: volvemos a mostrar el formulario (con los mensajes)
        return "nuevo"
    }
    // …todo correcto: convertir el form en entidad y guardar
    return "redirect:/"
}
```

Se usa una clase `Form` aparte de la entidad para no mezclar "lo que escribe el usuario" con "lo que se
guarda", y porque el formulario trabaja con el `id` de la provincia mientras la entidad guarda el objeto entero.

### 6.6 Plantillas Thymeleaf: el HTML

Son ficheros HTML normales con atributos `th:` que Thymeleaf sustituye en el servidor:

```html
<ul>
  <li th:each="contacto : ${contactos}">
    <a th:href="@{/contacto/{id}(id=${contacto.id})}" th:text="${contacto.nombre}">Nombre</a>
  </li>
</ul>
```

| Atributo | Qué hace |
|---|---|
| `th:text="${x}"` | Pone el valor de `x` como texto (escapado: seguro frente a inyección de HTML) |
| `th:each="c : ${lista}"` | Repite el elemento por cada objeto de la lista |
| `th:if="${cond}"` | Muestra el elemento sólo si se cumple |
| `th:href="@{/ruta}"` / `th:action` | Construye URLs correctamente |
| `th:field="*{nombre}"` | Enlaza un `<input>` con un campo del formulario (valor y errores) |
| `th:errors="*{nombre}"` | Muestra los mensajes de validación de ese campo |

`${...}` lee del **modelo** que el controlador rellenó. El texto "Nombre" del ejemplo es sólo un
marcador que se reemplaza; así la plantilla se puede abrir en un navegador sin servidor.

**Estructura común.** `fragments/base.html` define el esqueleto (cabecera, Bootstrap, contenedor).
Cada página lo reutiliza e indica qué título y contenido pone dentro:

```html
<html th:replace="~{fragments/base :: layout(~{::title}, ~{::section})}">
```

Y `partials/_formulario.html` es el formulario de contacto, reutilizado tanto por "nuevo" como por "editar".

### 6.7 Seguridad

Spring Security protege toda la aplicación **antes** de que la petición llegue a tus controladores.
La configuración está en `SecurityConfig.kt`:

```kotlin
http
    .authorizeHttpRequests { requests ->
        requests
            .requestMatchers("/login", "/register", "/page", "/error", "/css/**", ...).permitAll()
            .anyRequest().authenticated()      // todo lo demás exige haber iniciado sesión
    }
    .formLogin { form -> form.loginPage("/login") ... }
    .logout { logout -> logout.logoutUrl("/logout") ... }
```

Cómo funciona el login:

1. Envías el formulario de `/login` (campos `_username` y `_password`).
2. Spring Security llama a `AppUserDetailsService.loadUserByUsername(email)`, que busca el usuario en la base de datos.
3. Compara la contraseña que escribiste con el **hash** guardado. Las contraseñas **nunca se guardan en texto plano**:
   se guardan cifradas con *bcrypt* (`PasswordEncoder`).
4. Si coincide, crea la **sesión** (una cookie en tu navegador) y ya no te pide login hasta que cierres sesión.

**CSRF.** Todos los formularios `POST` llevan un campo oculto con un *token*. Thymeleaf lo añade solo
cuando usas `th:action`. Sirve para que una web maliciosa no pueda hacer que tu navegador envíe
formularios en tu nombre. Por eso **borrar y cerrar sesión son botones `POST`** y no enlaces.

### 6.8 Configuración: `application.yml`

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://127.0.0.1:3306/contactos?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true}
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:sa}
  jpa:
    hibernate:
      ddl-auto: update
```

La sintaxis `${DB_USER:root}` significa: "usa la variable de entorno `DB_USER`; si no existe, `root`".
Así el mismo código sirve en cualquier máquina sin editarlo.

**Perfiles.** `application-h2.yml` sólo se carga si activas el perfil `h2`. Pisa la configuración
anterior y usa una base de datos H2 en memoria. Es la forma de tener "configuraciones alternativas".

Orden de prioridad (de mayor a menor): argumentos de línea de comandos → variables de entorno →
`application-{perfil}.yml` → `application.yml`. Por eso una variable `DB_PASSWORD` definida en tu IDE
gana siempre al valor del fichero.

### 6.9 Datos iniciales

`ProvinciaSeeder` implementa `ApplicationRunner`: Spring ejecuta su método `run` justo al terminar
de arrancar. Si la tabla de provincias está vacía, inserta las 52 de España.

---

## 7. Caso completo: crear un contacto

Para ver cómo encajan las piezas, sigue el alta de un contacto:

1. **Pulsas "Nuevo contacto"** → el navegador pide `GET /contacto/nuevo`.
2. **Seguridad** comprueba que tienes sesión. Si no, te manda a `/login`.
3. **`ContactoController.nuevo()`** crea un `ContactoForm` vacío y carga la lista de provincias
   (`provincias.findAllByOrderByNombreAsc()`); ambos van al modelo. Devuelve `"nuevo"`.
4. **Thymeleaf** pinta `nuevo.html` → `partials/_formulario.html`, con el desplegable de provincias.
5. **Rellenas y envías** → el navegador hace `POST /contacto/nuevo` (con el token CSRF).
6. **`ContactoController.guardarNuevo()`** recibe los datos en un `ContactoForm`. `@Valid` aplica
   las reglas (nombre obligatorio, email válido…).
   - **Si hay errores:** vuelve a mostrar el formulario con los mensajes en rojo (`th:errors`).
   - **Si todo está bien:** busca la `Provincia` elegida, crea un `Contacto`, copia los datos y llama a
     `contactos.save(contacto)`.
7. **Hibernate** traduce el `save` a un `INSERT INTO contacto …`.
8. El controlador devuelve `redirect:/` → el navegador pide `GET /`, que lista los contactos,
   ya con el nuevo.

> ¿Por qué un redirect tras guardar? Para que, si recargas la página, no se reenvíe el formulario
> y crees el contacto dos veces (patrón *Post/Redirect/Get*).

---

## 8. Cómo modificar el proyecto

### Añadir un campo a los contactos (por ejemplo, `direccion`)

1. **Entidad** (`entity/Contacto.kt`): añade `@Column var direccion: String = ""`.
   Con `ddl-auto: update`, Hibernate añade la columna la próxima vez que arranques.
2. **Formulario** (`form/ContactoForm.kt`): añade `var direccion: String = ""` (y validaciones si quieres)
   y cópialo en `ContactoForm.desde(...)`.
3. **Controlador** (`ContactoController.copiarDatos`): `contacto.direccion = form.direccion.trim()`.
4. **Plantillas**: un `<input th:field="*{direccion}">` en `partials/_formulario.html` y mostrarlo en
   `partials/_contacto.html`.

### Añadir una página nueva

1. Crea `templates/acerca.html` (copia la estructura de `error.html`).
2. En un controlador:
   ```kotlin
   @GetMapping("/acerca")
   fun acerca(): String = "acerca"
   ```
3. Por defecto exigirá login (por `.anyRequest().authenticated()`). Si debe ser pública, añade `"/acerca"`
   a la lista de `permitAll()` en `SecurityConfig`.

Los cambios en plantillas se ven al recargar el navegador (la caché de Thymeleaf está desactivada);
los cambios en código Kotlin requieren reiniciar la aplicación.

### Ejecutar las pruebas

```bash
mvn test
```

`ContactosApplicationTests` arranca la aplicación entera con H2 y comprueba que no falla. Es una prueba
mínima ("¿se puede arrancar?"), un buen punto de partida para añadir más.

---

## 9. Problemas frecuentes

**`Access denied for user 'root'@'localhost'`**
La aplicación llega al MySQL, pero la contraseña no coincide. Comprueba:

- Que la contraseña de `application.yml` (`DB_PASSWORD`) es la misma que `MYSQL_ROOT_PASSWORD` en `compose.yaml`.
- Que no tienes definida en tu IDE una variable de entorno `DB_PASSWORD` / `DB_URL` antigua (tiene prioridad).
- Que no estás conectando a **otro MySQL** que ya tengas instalado en el mismo puerto.
- `MYSQL_ROOT_PASSWORD` **sólo se aplica la primera vez** que se crea el volumen. Si la cambias después, haz
  `docker compose down -v` (borra los datos del contenedor) y `docker compose up -d`.
- El estado `healthy` de Docker sólo indica que MySQL está vivo, **no** que la contraseña sea correcta. Para
  probarla: `docker exec -it contactos-springboot-database-1 mysql -uroot -pTU_CLAVE -e "SELECT 1"`.
- El healthcheck de `compose.yaml` también lleva la contraseña (`-psa`); si la cambias, cámbiala ahí.

**`address already in use` al hacer `docker compose up` (puerto 3306)**
Ya hay un MySQL local usando el 3306. Cambia el mapeo en `compose.yaml` a `"3307:3306"` y apunta la
aplicación al nuevo puerto (`jdbc:mysql://127.0.0.1:3307/contactos?...`).

**`permission denied while trying to connect to the docker API`**
Tu usuario no está en el grupo `docker`: `sudo usermod -aG docker $USER` y cierra sesión / vuelve a entrar
(o usa `sudo docker compose …`).

**`Unable to determine Dialect without JDBC metadata`**
Es la consecuencia del error de conexión anterior: Hibernate no pudo conectar y por eso no sabe qué base
de datos es. Arregla la conexión y desaparece.

**Puerto 8080 ocupado**
Cambia el puerto con `server.port: 8081` en `application.yml`, o arranca con `-Dserver.port=8081`.

**La página de login se ve, pero no me deja entrar tras registrarme**
Los emails se guardan en minúsculas y la búsqueda también; si no entra, comprueba que escribes la misma
contraseña con la que te registraste (mínimo 6 caracteres).

**Quiero empezar con la base de datos vacía**
En MySQL: `docker compose down -v && docker compose up -d`. En H2: basta con reiniciar la aplicación.

---

## 10. Si vienes de Symfony

| Symfony | Spring Boot + Kotlin |
|---|---|
| `composer.json` | `pom.xml` |
| `.env` (`DATABASE_URL`) | `application.yml` (`DB_URL`, `DB_USER`, `DB_PASSWORD`) |
| `src/Controller/*` con `#[Route]` | `controller/*.kt` con `@GetMapping` / `@PostMapping` |
| `src/Entity/*` (Doctrine) | `entity/*.kt` (JPA / Hibernate) |
| `src/Repository/*` | `repository/*.kt` (Spring Data JPA) |
| `src/Form/*FormType` + Validator | `form/*.kt` (Bean Validation) |
| Twig (`templates/*.twig`) | Thymeleaf (`templates/*.html`) |
| `security.yaml` | `SecurityConfig.kt` + `AppUserDetailsService.kt` |
| Autowiring de servicios | Inyección de dependencias por constructor |
| Migraciones de Doctrine | `ddl-auto: update` + `ProvinciaSeeder.kt` |
| `public/css/*` | `src/main/resources/static/css/*` |

### Diferencias respecto al proyecto original

- **Borrar y Logout usan POST** (con token CSRF) en lugar de enlaces GET. Es lo seguro; visualmente son botones.
- **La tabla de usuarios se llama `app_user`** en lugar de `user` (palabra reservada en varias bases de datos).
  El nombre del usuario ahora **se guarda** (en Symfony el campo `name` no se persistía).
- **Provincias precargadas** (las 52 de España) en el primer arranque; en el original la tabla quedaba vacía.
- Se quitó el campo "Name" del login: era obligatorio en el HTML pero Symfony nunca lo usaba.
- Los emails se guardan en minúsculas.
- El esquema lo crea Hibernate (`ddl-auto: update`). Es cómodo para aprender, pero en producción se usa
  una herramienta de migraciones como Flyway o Liquibase.

---

## 11. Glosario

| Término | Significado |
|---|---|
| **Framework** | Conjunto de librerías que ya resuelven los problemas habituales y marcan la forma de organizar el código |
| **Bean** | Objeto creado y gestionado por Spring, que puede inyectarse en otras clases |
| **Inyección de dependencias** | Pedir los objetos que necesitas en el constructor en vez de crearlos tú |
| **Starter** | Dependencia de Maven que agrupa todo lo necesario para una capacidad (web, seguridad, base de datos…) |
| **Autoconfiguración** | Spring Boot configura solo lo que detecta en el proyecto |
| **MVC** | Modelo-Vista-Controlador: separar datos, presentación y lógica de petición |
| **Controlador** | Clase con funciones que atienden rutas web |
| **Modelo (`Model`)** | Bandeja de datos que el controlador entrega a la plantilla |
| **Vista / plantilla** | Fichero HTML con huecos que se rellenan con el modelo |
| **Entidad** | Clase cuyos objetos se almacenan como filas de una tabla |
| **ORM** | Herramienta que traduce objetos ↔ tablas (aquí, Hibernate) |
| **JPA** | Estándar de Java para ORM; Hibernate lo implementa |
| **Repositorio** | Interfaz para leer y guardar entidades sin escribir SQL |
| **DTO / Form** | Objeto que transporta datos de un formulario, separado de la entidad |
| **Perfil** | Conjunto de configuración alternativo que se activa por nombre (`h2`) |
| **CSRF** | Ataque que fuerza a tu navegador a enviar peticiones en tu nombre; se evita con un token en los formularios |
| **Hash / bcrypt** | Huella irreversible de la contraseña; se guarda en lugar de la contraseña real |
| **Sesión** | Estado del usuario en el servidor, identificado por una cookie |
| **Maven** | Herramienta que descarga dependencias, compila y empaqueta el proyecto (`pom.xml`) |
| **Tomcat** | Servidor web que viene incluido dentro de la aplicación |
| **H2** | Base de datos que vive en memoria, ideal para pruebas |

---

## Para seguir aprendiendo

- Documentación oficial: <https://spring.io/projects/spring-boot>
- Guías cortas "Getting Started": <https://spring.io/guides>
- Thymeleaf: <https://www.thymeleaf.org/documentation.html>
- Kotlin: <https://kotlinlang.org/docs/getting-started.html>
