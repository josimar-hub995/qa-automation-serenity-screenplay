# Plan de pruebas Web y API

## Objetivo

Validar las seis funcionalidades de la prueba técnica sobre Selenium.dev y Reqres mediante ocho casos automatizados legibles, repetibles y trazables.

## Alcance

- Carga de la página principal.
- Validación del título de la página.
- Navegación al apartado Documentation.
- Validación del encabezado y URL de Documentation.
- Apertura de la búsqueda.
- Búsqueda con tres conjuntos de datos externos.
- Validación de resultados relacionados.
- Listado de usuarios mediante Reqres.
- Creación de un usuario mediante Reqres.
- Actualización de un usuario mediante Reqres.

## Fuera de alcance de esta entrega

- Pruebas de carga, accesibilidad y seguridad.
- Compatibilidad completa entre navegadores; la ejecución principal usa Chrome.

## Criterios de entrada

- JDK 17 y Maven disponibles.
- Chrome instalado.
- Selenium.dev accesible.
- API key de Reqres disponible como `REQRES_API_KEY`.
- Archivos `DataTest.xlsx` y `ApiDataTest.xlsx` válidos.
- Cada feature declara un tag `@xc-<libro>` y un tag que coincide con la hoja requerida.

## Criterios de salida

- Todos los escenarios ejecutados.
- Reporte Serenity generado.
- Carpeta `REPORT_...` creada con evidencias independientes por escenario.
- Un reporte Word por escenario, dashboard HTML y consolidado Excel generados.

## Riesgos

- Cambios de DOM o de la herramienta DocSearch de Selenium.dev.
- Interrupción de Internet o descarga del controlador.
- Diferencias visuales por versión de Chrome.
- Cambios de contrato, autenticación o límites de consumo de Reqres.

## Trazabilidad

| Requisito | Caso automatizado | Evidencia |
|---|---|---|
| Home y título | TAP - 001 | Capturas, log, JSON y Serenity |
| Documentation | TAP - 002 | Capturas, log, JSON y Serenity |
| Búsqueda WebDriver | TAP - 003 | Capturas, log, JSON y Serenity |
| Búsqueda Selenium Grid | TAP - 004 | Capturas, log, JSON y Serenity |
| Búsqueda BiDi | TAP - 005 | Capturas, log, JSON y Serenity |
| Listado de usuarios | TAP - 006 | Respuesta JSON, log y Serenity |
| Creación de usuario | TAP - 007 | Respuesta JSON, log y Serenity |
| Actualización de usuario | TAP - 008 | Respuesta JSON, log y Serenity |

## Convenciones de automatización

- Los keywords Gherkin y las anotaciones Cucumber se escriben en inglés; los títulos y pasos funcionales se escriben en español.
- Todos los nombres de escenario comienzan con `Validar`.
- Los tags se declaran inmediatamente antes de cada `Scenario Outline`.
- La etiqueta `@TC_...` utilizada por Cucumber vive únicamente en el feature; los Excel no contienen una columna `TAG`.
- `TAP` es el identificador único del caso con formato `TAP - 001`.
- La columna `datos` de `Examples` envía el identificador técnico necesario para recuperar el registro correcto del Excel.
- `@xc-DataTest` selecciona `DataTest.xlsx`; `@home`, `@documentation` y `@search` seleccionan la hoja Web correspondiente.
- `@xc-ApiDataTest` selecciona `ApiDataTest.xlsx` y `@api_cases` selecciona la hoja `API_CASES`.
- Los headers API, sus valores y las mutaciones `OMIT`, `EMPTY` e `INCORRECT` se controlan desde el Excel; los Steps no contienen esa lógica.
- El Excel conserva una única descripción en `ESCENARIO`, el resultado esperado y los datos variables; la configuración estable del ambiente se mantiene en `qa.properties`.
- Los `Target` Web se mantienen en el paquete `locators`, permitido por el patrón Screenplay.
- Una hoja `USUARIO` solo se incorpora cuando un caso necesite datos de usuario y se selecciona con `@usuario`.
