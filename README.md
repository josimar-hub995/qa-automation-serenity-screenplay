# Automatización QA Senior Web y API

Proyecto ejecutable para ocho casos activos y tres plantillas de error de headers, construido con Java 17, Maven, Serenity BDD, Screenplay, Screenplay REST y Cucumber.

## Escenarios automatizados

| TAP | TAG de ejecución | Escenario | Tipo |
|---|---|---|---|
| `TAP - 001` | `@TC_WEB_001` | Validar que la página de inicio de Selenium cargue correctamente y muestre el título esperado. | Web |
| `TAP - 002` | `@TC_WEB_002` | Validar que la navegación al apartado Documentation funcione correctamente y cargue la página esperada. | Web |
| `TAP - 003` | `@TC_WEB_003` | Validar el flujo de búsqueda de WebDriver y que los resultados correspondan a lo buscado. | Web |
| `TAP - 004` | `@TC_WEB_004` | Validar el flujo de búsqueda de Selenium Grid y que los resultados correspondan a lo buscado. | Web |
| `TAP - 005` | `@TC_WEB_005` | Validar el flujo de búsqueda de BiDi y que los resultados correspondan a lo buscado. | Web |
| `TAP - 006` | `@TC_API_006` | Validar que la API de Reqres liste usuarios correctamente y muestre el contenido esperado. | API |
| `TAP - 007` | `@TC_API_007` | Validar la creación de un nuevo usuario mediante la API de Reqres y el contenido de la respuesta. | API |
| `TAP - 008` | `@TC_API_008` | Validar la actualización de datos de un usuario mediante la API de Reqres y el contenido de la respuesta. | API |

Los casos se adaptaron del documento `Prueba Técnica para QA (1).docx`. El TAP se almacena en Excel y el TAG de ejecución vive únicamente en el feature. Ambos son únicos y todos los escenarios comienzan con `Validar`.

El feature API incluye además `TC_API_009`, `TC_API_010` y `TC_API_011` como plantillas desactivadas para probar headers con `OMIT`, `EMPTY` e `INCORRECT`. Se activan desde la columna `EJECUTAR` del Excel después de ajustar el status y response esperados al contrato del ambiente.

## Requisitos

- JDK 17.
- Maven 3.9 o superior.
- Google Chrome para los escenarios Web.
- Acceso a Internet para Maven, WebDriver, Selenium.dev y Reqres.
- API key de Reqres para los escenarios API.

Reqres requiere actualmente el encabezado `x-api-key`. Genere una key desde su sitio y expóngala sin guardarla en el repositorio:

Linux o macOS:

```bash
export REQRES_API_KEY="su_api_key"
```

Windows PowerShell:

```powershell
$env:REQRES_API_KEY="su_api_key"
```

También se admite la propiedad de sistema `-Dreqres.api.key=su_api_key`.

## Ejecución

### Bandera central de logs

La bandera permanente se encuentra en el archivo raíz `serenity.properties`:

```properties
execution.logs=false
```

- `false`: muestra únicamente el progreso profesional, el resultado final y las URLs de reportes.
- `true`: agrega detalle por caso, logs técnicos de Serenity/Logback y trazas completas de errores.

La prioridad es: `-Dexecution.logs`, variable `EXECUTION_LOGS` y, finalmente, `serenity.properties`. Por ello puede cambiar el archivo una vez o sobrescribirlo temporalmente desde el comando.

### Salida resumida con avance en vivo

En Windows PowerShell, el script recomendado lee automáticamente la bandera central:

```powershell
.\run_tests.ps1 -Tags "@automation"
```

Con `execution.logs=false` se ocultan los logs técnicos y se conserva la información útil:

```text
================================================================================================================
                                      QA AUTOMATION | EJECUCION DE PRUEBAS
================================================================================================================
 Casos seleccionados : 8
 Modo de consola      : RESUMIDO
----------------------------------------------------------------------------------------------------------------
 [PROGRESO] [###---------------------]  13% | Completados 1/8 | PASSED 1 | FAILED 0 | Pendientes 7
 ...
 [PROGRESO] [########################] 100% | Completados 8/8 | PASSED 8 | FAILED 0 | Pendientes 0
----------------------------------------------------------------------------------------------------------------
 RESULTADO FINAL: PASSED | Ejecutados 8 | PASSED 8 | FAILED 0
================================================================================================================

================================================================================================================
                                 QA AUTOMATION · REPORTE DE EJECUCION
================================================================================================================
 ESCENARIOS:        8   |   PASSED: 8   |   FAILED: 0
----------------------------------------------------------------------------------------------------------------
 DASHBOARD GENERAL
 Local:
file:///C:/.../reports/REPORT_005_20260919_093241/resumen/dashboard.html
================================================================================================================
```

La URL `file:///...` corresponde al dashboard HTML de la computadora que ejecutó las pruebas. En Jenkins se imprime adicionalmente una URL `http(s)://.../artifact/.../dashboard.html`, que sí se abre desde cualquier equipo con acceso al pipeline.

Si PowerShell restringe la ejecución de scripts:

```powershell
powershell -ExecutionPolicy Bypass -File .\run_tests.ps1 -Tags "@automation"
```

El comando Maven equivalente es:

```powershell
mvn -q clean verify "-Dcucumber.filter.tags=@automation"
```

`-q` silencia los mensajes propios de Maven. La bandera de `serenity.properties` controla los logs de las pruebas, pero Maven configura su propia consola antes de leer el proyecto.

### Salida detallada

Cambie `execution.logs=true` en `serenity.properties` y ejecute:

```powershell
.\run_tests.ps1 -Tags "@automation"
```

También puede activar el detalle solo para una ejecución, sin modificar el archivo:

```powershell
mvn clean verify "-Dcucumber.filter.tags=@automation" "-Dexecution.logs=true"
```

Quite `-q` cuando también quiera ver el ciclo de vida completo de Maven. El contador y las URLs del dashboard aparecen en ambos modos.

### Filtros de ejecución

Todos los casos Web y API:

```bash
mvn -q clean verify "-Dcucumber.filter.tags=@automation"
```

Por tipo:

```bash
mvn -q clean verify "-Dcucumber.filter.tags=@web"
mvn -q clean verify "-Dcucumber.filter.tags=@api"
```

Por TAG de ejecución o módulo:

```bash
mvn -q clean verify "-Dcucumber.filter.tags=@TC_WEB_001"
mvn -q clean verify "-Dcucumber.filter.tags=@TC_WEB_004"
mvn -q clean verify "-Dcucumber.filter.tags=@search"
mvn -q clean verify "-Dcucumber.filter.tags=@TC_API_007"
mvn -q clean verify "-Dcucumber.filter.tags=@api_cases"
```

Modo headless:

```bash
mvn -q clean verify "-Dcucumber.filter.tags=@automation" -Dheadless.mode=true
```

También se incluyen `run_all.bat` y `run_all.sh`. Ambos verifican que Maven y `REQRES_API_KEY` estén disponibles y leen la bandera de `serenity.properties`. La variable `EXECUTION_LOGS=true|false` la sobrescribe temporalmente. El script PowerShell conserva además `-Logs true|false` como override opcional.

## Convención de Gherkin

Las palabras reservadas se mantienen en inglés: `Feature`, `Background`, `Scenario Outline`, `Given`, `When`, `Then`, `And` y `Examples`. Los títulos y pasos funcionales están en español.

Todo `Scenario` o `Scenario Outline` debe comenzar con la palabra `Validar` seguida del contexto que se comprobará:

```gherkin
Feature: Búsqueda de información en Selenium

  @TC_WEB_003 @web @automation @regression @happyPath @xc-DataTest @search
  Scenario Outline: Validar el flujo de búsqueda de WebDriver y los resultados correspondientes con los datos <datos>
    Given que el usuario accede al sitio web de Selenium
    When el usuario realiza una búsqueda usando los datos "<datos>"
    Then deben mostrarse resultados relacionados usando los datos "<datos>"

    Examples:
      | datos |
      | 1     |
```

Los tags se colocan inmediatamente antes de cada `Scenario Outline`, tal como en el ejemplo. El framework valida la regla de nomenclatura al iniciar cada escenario y también cuando lee `ESCENARIO` desde Excel.

## Excel por casos y escenarios

Los escenarios Web continúan usando:

```text
src/test/resources/data/DataTest.xlsx
```

Cada fila representa un escenario ejecutable. Las columnas comunes son:

- `EJECUTAR`: primera columna; `SÍ` habilita la fila y `NO` la omite.
- `ID_FILA`: identificador técnico de la fila; el feature lo referencia mediante la columna limpia `datos` de `Examples`.
- `TAP`: identificador único y trazable del caso con formato `TAP - 001`. No se usa como filtro de Cucumber.
- `ESCENARIO`: única descripción funcional; siempre comienza con `Validar`.
- `RESULTADO_ESPERADO`: resultado que aparecerá en los reportes.

Ninguno de los dos libros contiene una columna `TAG`. Las etiquetas `@TC_WEB_*` y `@TC_API_*` se declaran exclusivamente en los features para filtrar la ejecución.

| Hoja Web | Datos adicionales |
|---|---|
| `HOME` | Caso de carga y título |
| `DOCUMENTATION` | Caso de navegación |
| `SEARCH` | `VALOR_BUSQUEDA` |

La URL, los selectores lógicos y otros valores estables del ambiente continúan en `config/qa.properties`.

### Excel exclusivo para APIs

El flujo API activo usa un libro independiente:

```text
src/test/resources/data/ApiDataTest.xlsx
```

Su hoja `API_CASES` contiene las columnas:

```text
ID_FILA, EJECUTAR, ITERACION, TAP, CAPA, VERSION, API,
ESCENARIO, RESULTADO_ESPERADO, CREDENCIALES, ENDPOINT, HEADERS,
HEADER_ERROR_CONFIG, PARAMS, REQUEST_TYPE, BODY,
ESTADO_HTTP_ESPERADO, RESPUESTA_ESPERADA
```

- `ENDPOINT`, `PARAMS`, `REQUEST_TYPE`, `BODY`, el status y el response esperado se leen por fila.
- `HEADERS` es un objeto JSON. Los nombres y valores de los headers de negocio no están codificados en Java.
- Un valor como `${reqres.api.key}` se resuelve desde `-Dreqres.api.key` o `REQRES_API_KEY` sin guardar el secreto en el Excel ni en Git.
- Puede añadir un header nuevo directamente en el JSON de `HEADERS`; el builder genérico lo enviará sin cambios de código.
- `HEADER_ERROR_CONFIG` admite varias operaciones separadas por punto y coma: `OMIT:nombre`, `EMPTY:nombre` e `INCORRECT:nombre=valor`.
- Las filas 1 a 3 están activas. Las filas 4 a 6 son plantillas de error con `EJECUTAR=NO`.

La interpretación de errores vive en `configurations/HeaderErrorConfiguration`, la construcción de headers en `builders/ApiHeaderBuilder` y el armado de la petición en `builders/ApiRequestBuilder`. Los Step Definitions solo coordinan el caso.

## Selección dinámica del Excel

La fuente se determina mediante tags:

| Tag | Selección |
|---|---|
| `@xc-DataTest` | Libro `DataTest.xlsx` |
| `@home` | Hoja `HOME` |
| `@documentation` | Hoja `DOCUMENTATION` |
| `@search` | Hoja `SEARCH` |
| `@xc-ApiDataTest` | Libro `ApiDataTest.xlsx` |
| `@api_cases` | Hoja `API_CASES` |

Para un futuro libro `UsuariosQA.xlsx` con hoja `USUARIO`, los tags serían `@xc-UsuariosQA @usuario`. No se incluye una hoja vacía porque los casos actuales no requieren credenciales de usuario.

## JSON y Step Definitions

`.vscode/settings.json` enlaza los features con el glue Java para la extensión oficial de Cucumber:

```json
{
  "cucumber.features": [
    "src/test/resources/features/**/*.feature"
  ],
  "cucumber.glue": [
    "src/test/java/pe/com/challenge/automation/stepdefinitions/**/*.java",
    "src/test/java/pe/com/challenge/automation/hooks/**/*.java"
  ]
}
```

`src/test/resources/junit-platform.properties` configura el glue y el plugin de Serenity para Cucumber JVM.
Además, cada ejecución genera el JSON estándar de Cucumber en `target/cucumber-reports/cucumber.json`.

## Reportes

Cada corrida genera una carpeta correlativa con número, palabra `REPORT`, fecha y hora:

```text
reports/
└── REPORT_001_20260918_143015/
    ├── resumen/
    │   ├── assets/
    │   ├── dashboard.html
    │   ├── execution_summary.json
    │   └── resultado_ejecucion.xlsx
    ├── serenity/
    │   └── index.html
    ├── word/
    │   ├── TAP_001_REPORT_001.docx
    │   ├── TAP_002_REPORT_001.docx
    │   ├── ... un Word por cada escenario ejecutado
    │   └── TAP_008_REPORT_001.docx
    └── escenarios/
        ├── TC_WEB_001_.../       # datos, logs, resultados y capturas por paso
        ├── TC_WEB_002_.../
        ├── TC_WEB_003_.../
        ├── TC_WEB_004_.../
        ├── TC_WEB_005_.../
        ├── TC_API_006_.../
        ├── TC_API_007_.../
        └── TC_API_008_.../
```

Cada Word individual, `dashboard.html`, el consolidado Excel y el JSON muestran:

- TAP y escenario en español, sin una descripción funcional duplicada.
- Resultado esperado.
- Datos, libro y hoja utilizados.
- Estado normalizado exclusivamente como `PASSED` o `FAILED`, y duración.
- Página o servicio consumido, navegador/canal, paso exacto del fallo y mensaje técnico.

Cada Word comienza con una tabla profesional del caso (TAP, escenario, resultado esperado,
navegador, página/servicio, Excel, hoja y datos). Después presenta cada paso Gherkin en el formato
`19-09-2026 | 11:23:01 AM | Given/When/Then ...`, seguido de su captura centrada. Si un paso falla,
incluye la última captura disponible, la causa y el stack trace.

El dashboard contiene gráficos de estado, cobertura y duración. Su título, subtítulo y gráfica de canal se adaptan al filtro ejecutado: una corrida solo Web no muestra información API, una corrida solo API no muestra información Web y una corrida mixta muestra ambos canales. La tabla principal muestra únicamente `TAP`, `Escenario`, `Resultado esperado`, `Estado` y `Duración`; no incluye columnas `TAG` ni `Reportes`. Al hacer clic en cualquier parte de una fila API se despliega el método, endpoint, headers enviados con secretos enmascarados, parámetros, request body, status HTTP y response body. Cuando existe un `FAILED`, se crea además `error-report.html` dentro de la carpeta del escenario y su URL se imprime en consola.

Los escenarios Web guardan capturas por paso y consola del navegador. Los escenarios API guardan
`api_exchange.json`, `api_response.json`, logs y el detalle de Serenity. Las subcarpetas opcionales se crean bajo demanda;
por ejemplo, un escenario API no genera carpetas vacías de capturas o consola del navegador.

### Publicación en Jenkins

El `Jenkinsfile` incluido archiva todos los reportes y publica `dashboard.html` mediante HTML Publisher. En la consola del pipeline se muestran dos direcciones distintas:

- `URL LOCAL`: ruta `file:///...` dentro del agente que ejecutó Maven.
- `URL PIPELINE`: ruta HTTP basada en `BUILD_URL`, accesible desde el navegador según los permisos de Jenkins.

El pipeline requiere el plugin **HTML Publisher**. Además de la URL impresa, Jenkins mostrará el acceso **Dashboard QA** en la página de la ejecución.

## Arquitectura

```text
Feature y tags
  -> DataSourceManager
  -> DataTestManager
  -> modelo tipado del escenario
  -> Step Definition
  -> Locator (Target) para Web / builders y configuraciones para API
  -> Task Web o petición API genérica
  -> Question
  -> Word individual, dashboard HTML, reporte de fallo, Excel, JSON y Serenity
```

Los Step Definitions no abren archivos ni conocen Apache POI. El libro, la hoja, los datos, el TAP, el escenario y el resultado esperado se resuelven antes de ejecutar las validaciones. El TAG se conserva solamente en el feature. Los `Target` Web se concentran en `src/test/java/pe/com/challenge/automation/locators`, una separación compatible con Screenplay.
