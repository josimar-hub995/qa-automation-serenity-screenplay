# Reportes generados

Esta carpeta se completa durante la ejecución. Cada corrida crea una carpeta con el formato:

```text
REPORT_###_yyyyMMdd_HHmmss
```

No se mezclan evidencias de diferentes corridas ni de diferentes escenarios.

Dentro de `resumen` se generan `dashboard.html`, `execution_summary.json` y
`resultado_ejecucion.xlsx`. Dentro de `word` se crea un documento individual por
cada escenario ejecutado. La tabla principal del dashboard muestra TAP, escenario,
resultado esperado, estado `PASSED`/`FAILED` y duración, sin columnas TAG ni
Reportes. Los Word conservan toda la trazabilidad del caso y de cada paso.

Los casos Web leen `DataTest.xlsx`; los casos API leen `ApiDataTest.xlsx`. Al hacer
clic en una fila API del dashboard se despliegan headers enviados (con secretos
enmascarados), parámetros, request body, status HTTP y response body.

Si un escenario falla, su carpeta incluye `error-report.html` con el paso exacto,
la causa, el stack trace y la última evidencia. Al finalizar, la consola imprime
la URL local del dashboard, la URL Jenkins cuando existe `BUILD_URL` y las URLs de
los reportes técnicos de error.
