# QA Automation - Serenity Screenplay

Framework de automatización orientado a **Quality Engineering** para validaciones **Frontend** y **Backend/API**, desarrollado con Java 17, Serenity BDD, Screenplay Pattern, Cucumber, Selenium WebDriver y Screenplay REST.

---

# Ejecución rápida

## Comando principal

Para ejecutar la suite de regresión:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@regression"
```

Este es el comando recomendado para realizar una validación rápida del proyecto.

---

## Otras ejecuciones

### Frontend

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@web"
```

### Backend

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@api"
```

### Tag específico

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@TC_WEB_001"
```

Ejemplo Backend:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@TC_API_006"
```

### Chrome

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@web" "-Dwebdriver.driver=chrome"
```

### Logs técnicos

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@regression" "-Dexecution.logs=true"
```

---

# Ejecución mediante GitHub Actions

También es posible ejecutar las pruebas sin configurar el proyecto localmente.

Ingresar a:

```text
GitHub
→ Actions
→ Quality Engineering Pipeline
→ Run workflow
```

El pipeline permite seleccionar:

```text
Alcance
├── Regresión
├── Frontend
├── Backend
└── Tag específico

Navegador
├── Chrome
└── Edge

Logs detallados
├── false
└── true

Headless
├── true
└── false
```

Cuando se selecciona:

```text
Tag específico
```

puede ingresarse, por ejemplo:

```text
TC_WEB_001
```

o:

```text
@TC_WEB_001
```

El pipeline valida automáticamente que el tag exista antes de iniciar la ejecución.

---

# Reportes públicos

Después de una ejecución válida, el pipeline publica automáticamente los reportes mediante GitHub Pages.

## Dashboard

```text
https://josimar-hub995.github.io/qa-automation-serenity-screenplay/
```

## Dashboard Serenity

```text
https://josimar-hub995.github.io/qa-automation-serenity-screenplay/serenity/index.html
```

Cada nueva ejecución publicada actualiza GitHub Pages con el reporte correspondiente a esa ejecución.

---

# Requisitos previos

Para una ejecución local se requiere:

- Java 17
- Maven
- Git
- Google Chrome o Microsoft Edge
- Acceso a Internet para dependencias y WebDriver

Validar Java:

```powershell
java -version
```

Validar Maven:

```powershell
mvn -version
```

Validar Git:

```powershell
git --version
```

---

# Clonar el repositorio

```powershell
git clone https://github.com/josimar-hub995/qa-automation-serenity-screenplay.git
```

Ingresar al proyecto:

```powershell
cd qa-automation-serenity-screenplay
```

Ejecutar:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@regression"
```

---

# Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 17 | Lenguaje principal |
| Maven | Gestión de dependencias y ejecución |
| Serenity BDD | Automatización y reportes |
| Screenplay Pattern | Patrón de diseño |
| Cucumber | Escenarios BDD |
| Selenium WebDriver | Automatización Frontend |
| Screenplay REST | Automatización Backend/API |
| JUnit Platform | Motor de ejecución |
| Excel | Gestión de datos de prueba |
| GitHub Actions | Integración continua |
| GitHub Pages | Publicación de reportes |
| Chrome | Navegador soportado |
| Microsoft Edge | Navegador soportado |

---

# Características principales

El framework permite:

- Automatización Frontend.
- Automatización Backend/API.
- Ejecución de regresión.
- Ejecución individual mediante tags.
- Selección entre Chrome y Edge.
- Ejecución Headless.
- Logs resumidos o detallados.
- Gestión de datos mediante Excel.
- Control de ejecución mediante `EJECUTAR`.
- Evidencias automáticas.
- Dashboard personalizado.
- Dashboard Serenity BDD.
- Reportes Excel.
- Reportes Word por escenario.
- Reportes técnicos para errores.
- Publicación mediante GitHub Pages.
- Gestión segura de credenciales mediante GitHub Secrets.
- Validación automática de tags.
- Zona horaria configurada para Perú.

---

# Arquitectura del proyecto

La solución utiliza Screenplay Pattern para separar responsabilidades y facilitar mantenimiento y escalabilidad.

```text
qa-automation-serenity-screenplay
│
├── .github
│   └── workflows
│       └── ...
│
├── reports
│   └── REPORT_...
│
├── src
│   └── test
│       ├── java
│       │   └── pe
│       │       └── com
│       │           └── challenge
│       │               └── automation
│       │                   ├── managers
│       │                   ├── models
│       │                   ├── reports
│       │                   ├── runners
│       │                   ├── utilities
│       │                   └── ...
│       │
│       └── resources
│           ├── config
│           ├── features
│           └── ...
│
├── serenity.properties
├── pom.xml
└── README.md
```

---

# Configuración de Serenity

La configuración principal se encuentra en:

```text
serenity.properties
```

```properties
serenity.project.name=QA Automation Senior
serenity.test.root=features
serenity.outputDirectory=target/site/serenity
serenity.take.screenshots=FOR_EACH_ACTION
serenity.report.encoding=UTF-8
serenity.console.colors=false

execution.logs=false

webdriver.driver=chrome
webdriver.autodownload=true
webdriver.timeouts.implicitlywait=5000
webdriver.wait.for.timeout=15000
```

La configuración del ambiente se encuentra en:

```text
src/test/resources/config/qa.properties
```

Las credenciales privadas no deben almacenarse directamente dentro de este archivo.

---

# Seguridad y credenciales

La API Key utilizada durante las pruebas Backend se almacena en:

```text
GitHub
→ Settings
→ Secrets and variables
→ Actions
→ Repository secrets
```

Secret utilizado:

```text
REQRES_API_KEY
```

Durante la ejecución del pipeline, la credencial se inyecta temporalmente dentro del runner.

La credencial:

- No se almacena directamente en el código.
- No debe incluirse en commits.
- No se publica mediante GitHub Pages.
- No debe almacenarse con su valor real en `qa.properties`.

---

# Alcances de ejecución

## Regresión

Tag:

```text
@regression
```

Comando:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@regression"
```

---

## Frontend

Tag:

```text
@web
```

Comando:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@web"
```

Con Chrome:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@web" "-Dwebdriver.driver=chrome"
```

Con Edge:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@web" "-Dwebdriver.driver=edge"
```

---

## Backend

Tag:

```text
@api
```

Comando:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@api"
```

Las ejecuciones exclusivamente Backend no necesitan navegador.

---

# Ejecución por tag específico

Cualquier tag existente dentro de los `.feature` puede ejecutarse directamente.

Frontend:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@TC_WEB_001"
```

Backend:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@TC_API_006"
```

Desde GitHub Actions también puede seleccionarse:

```text
Tag específico
```

e ingresar:

```text
TC_WEB_001
```

El pipeline agrega `@` cuando corresponda y verifica que el tag exista.

---

# Navegadores

Para Frontend se soportan:

```text
Chrome
Edge
```

Chrome:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@web" "-Dwebdriver.driver=chrome"
```

Edge:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@web" "-Dwebdriver.driver=edge"
```

---

# Modo Headless

Activar:

```text
-Dheadless.mode=true
```

Ejemplo:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@web" "-Dheadless.mode=true"
```

Desactivar:

```text
-Dheadless.mode=false
```

Ejemplo:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@web" "-Dheadless.mode=false"
```

---

# Logs

## Logs resumidos

```text
-Dexecution.logs=false
```

Ejemplo:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@regression" "-Dexecution.logs=false"
```

La consola prioriza:

```text
Progreso
Ejecutados
PASSED
FAILED
Resultado final
URLs de reportes
```

## Logs detallados

```text
-Dexecution.logs=true
```

Ejemplo:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@regression" "-Dexecution.logs=true"
```

---

# Gestión de datos mediante Excel

Los datos de prueba incluyen la columna:

```text
EJECUTAR
```

Cuando contiene:

```text
SI
```

el escenario continúa su ejecución.

Cuando contiene:

```text
NO
```

el framework omite el caso.

Ejemplo:

```text
Escenarios encontrados: 11

EJECUTAR=SI: 8
EJECUTAR=NO: 3
```

El resultado funcional será:

```text
Ejecutados: 8
PASSED: 8
FAILED: 0
```

Los escenarios configurados con `EJECUTAR=NO` no se contabilizan como fallos en el Dashboard personalizado.

---

# GitHub Actions

Workflow:

```text
Quality Engineering Pipeline
```

Ruta:

```text
GitHub
→ Actions
→ Quality Engineering Pipeline
→ Run workflow
```

El formulario permite configurar:

| Parámetro | Opciones |
|---|---|
| Alcance | Regresión / Frontend / Backend / Tag específico |
| Tag específico | Tag personalizado |
| Navegador | Chrome / Edge |
| Logs | true / false |
| Headless | true / false |

El campo `Tag específico` solamente es utilizado cuando el alcance seleccionado es:

```text
Tag específico
```

En los demás alcances su contenido es ignorado.

---

# Flujo del pipeline

```text
Checkout
    ↓
Java 17
    ↓
Limpieza
    ↓
Configuración de ejecución
    ↓
Validación del tag
    ↓
Validación de configuración
    ↓
Validación de Secrets
    ↓
Inyección temporal de credenciales
    ↓
Validación del navegador
    ↓
Maven
    ↓
Ejecución Cucumber
    ↓
Dashboard personalizado
    ↓
Dashboard Serenity
    ↓
Validación del reporte generado
    ↓
Artifacts
    ↓
GitHub Pages
```

---

# Dashboard personalizado

Se genera dentro de:

```text
reports/REPORT_.../resumen/dashboard.html
```

Incluye:

- Total de escenarios.
- PASSED.
- FAILED.
- Pass Rate.
- Duración.
- Cobertura.
- Detalle por escenario.
- Resultados esperados.
- Request API.
- Response API.
- HTTP Status.
- Headers sanitizados.
- Análisis de errores.

El Dashboard utiliza el logo del proyecto como favicon de la pestaña del navegador.

---

# Dashboard Serenity

Serenity genera su reporte en:

```text
target/site/serenity/index.html
```

---

# Validación antes de publicar

Antes de actualizar GitHub Pages se verifica:

```text
reports/REPORT_*
```

El pipeline exige que exista únicamente el reporte correspondiente a la ejecución actual.

Además valida:

```text
execution_summary.json
```

Si se ejecuta:

```text
Frontend
```

el reporte no debe contener escenarios `TC_API_*`.

Si se ejecuta:

```text
Backend
```

el reporte no debe contener escenarios `TC_WEB_*`.

Una inconsistencia evita que se publique un reporte incorrecto.

---

# Artifacts

Cada ejecución genera:

```text
quality-validation-report
github-pages
```

## quality-validation-report

Contiene las evidencias descargables.

## github-pages

Es utilizado por GitHub para publicar los archivos HTML mediante Pages.

No representa una segunda ejecución de pruebas.

---

# Zona horaria

El pipeline utiliza:

```text
America/Lima
```

Java también recibe:

```text
-Duser.timezone=America/Lima
```

De esta forma, la fecha y hora del reporte se mantienen alineadas con Perú.

---

# Ejemplo de resultado

```text
QA AUTOMATION | EJECUCION DE PRUEBAS

100% | Completados 8/8 | PASSED 8 | FAILED 0

QA AUTOMATION · REPORTE DE EJECUCION

ESCENARIOS: 8
PASSED: 8
FAILED: 0

RESULTADO FINAL: PASSED
```

---

# Repositorio

```text
https://github.com/josimar-hub995/qa-automation-serenity-screenplay
```

## Dashboard

```text
https://josimar-hub995.github.io/qa-automation-serenity-screenplay/
```

## Dashboard Serenity

```text
https://josimar-hub995.github.io/qa-automation-serenity-screenplay/serenity/index.html
```

---

# Quality Engineering

La solución busca mantener:

- Arquitectura mantenible.
- Separación de responsabilidades.
- Ejecuciones parametrizables.
- Trazabilidad.
- Gestión segura de credenciales.
- Evidencias automatizadas.
- Integración continua.
- Publicación automática de resultados.
- Escalabilidad para nuevos escenarios y servicios.