# Etapa 5 - Análisis completo de los CSV reales

## 1. Alcance y metodología

Se analizaron los archivos reales presentes en la carpeta del proyecto:

- `data/semana_3/movimientos_financieros_diarios.csv`
- `data/semana_3/intereses_trimestrales.csv`
- `data/semana_3/estados_financieros_anuales.csv`

No se modificaron los CSV originales ni se tocaron archivos de Semana 8. Se leyeron todas las filas de cada archivo desde el proyecto actual.

No se creó un script temporal permanente. El análisis se ejecutó en línea con Python desde la terminal del proyecto para evitar tocar la estructura del repositorio.

---

## 2. Resumen general

| Archivo | Total de registros | Columnas encontradas |
|---|---:|---|
| movimientos_financieros_diarios.csv | 1000 | `id`, `fecha`, `monto`, `tipo` |
| intereses_trimestrales.csv | 1000 | `cuenta_id`, `nombre`, `saldo`, `edad`, `tipo` |
| estados_financieros_anuales.csv | 1000 | `cuenta_id`, `fecha`, `transaccion`, `monto`, `descripcion` |

Total analizado: 3000 registros.

---

## 3. Análisis de cada archivo

### 3.1 movimientos_financieros_diarios.csv

#### Columnas y registros
- Total de registros: 1000
- Columnas: `id`, `fecha`, `monto`, `tipo`

#### Campos vacíos por columna
| Columna | Vacíos |
|---|---:|
| id | 0 |
| fecha | 0 |
| monto | 168 |
| tipo | 0 |

#### Fechas inválidas o ambiguas
- Fechas inválidas o ambiguas detectadas: 49 invalidas + 185 ambiguas = 234 registros con riesgo de fecha.
- La ambigüedad se confirma en varios formatos como `11-12-2024`, `02/04/2024` y variantes con barra o guion. Este patrón requiere normalización antes del procesamiento.

#### Montos no numéricos, negativos y cero
| Indicador | Cantidad |
|---|---:|
| montos no numéricos | 0 |
| montos negativos | 125 |
| montos iguales a cero | 20 |
| montos vacíos | 168 |

#### Tipos de movimiento detectados
| Tipo | Cantidad |
|---|---:|
| debito | 354 |
| invalido | 317 |
| credito | 263 |
| desconocido | 66 |

#### Duplicados potenciales
- Registros completos duplicados: 0
- IDs duplicados: 0

#### Conclusión técnica
- Hay una cantidad importante de registros con fecha ambigua o no normalizada.
- El campo `monto` presenta vacíos, negativos y ceros.
- El campo `tipo` incluye valores claramente no esperados: `invalido` y `desconocido`.

#### Reglas que requieren aprobación del negocio
- Si un monto negativo es permitido para ciertos tipos de operación o si debe rechazarse por regla de negocio.
- Si el tipo `debito`/`credito` debe normalizarse respecto a `débito`/`crédito` o a una lista controlada.
- Cómo interpretar fechas ambiguas (`11-12-2024` o `02/04/2024`) si el negocio usa formato día-mes-año o mes-día-año.

---

### 3.2 intereses_trimestrales.csv

#### Columnas y registros
- Total de registros: 1000
- Columnas: `cuenta_id`, `nombre`, `saldo`, `edad`, `tipo`

#### Campos vacíos por columna
| Columna | Vacíos |
|---|---:|
| cuenta_id | 0 |
| nombre | 0 |
| saldo | 204 |
| edad | 171 |
| tipo | 0 |

#### Saldos vacíos o inválidos
| Indicador | Cantidad |
|---|---:|
| saldos vacíos | 204 |
| saldos no numéricos | 0 |
| saldos negativos | 0 |
| saldos iguales a cero | 0 |

#### Edades faltantes o fuera de rango
| Indicador | Cantidad |
|---|---:|
| edades vacías | 171 |
| edades fuera de rango (0 a 120) | 46 |
| edades con formato no numérico | 0 |

#### Tipos detectados
| Tipo | Cantidad |
|---|---:|
| -1 | 281 |
| ahorro | 236 |
| prestamo | 236 |
| hipoteca | 203 |
| unknown | 44 |

#### Cuentas repetidas
- Unidades únicas de `cuenta_id`: 50
- Número de `cuenta_id` repetidos: 50 claves con duplicados
- Total de filas adicionales por duplicado: 950

Esto significa que la misma cuenta aparece muchas veces en el archivo. Esto no puede asumirse automáticamente como error, porque la repetición puede representar historial, varios periodos o múltiples registros del mismo cliente. Debe confirmarse con el negocio antes de decidir si se consolidan, descartan o reubican.

#### Duplicados potenciales
- Filas completas duplicadas: 7
- Duplicados por cuenta repetida: sí, con patrón claro de repetición por `cuenta_id`

#### Conclusión técnica
- El archivo presenta un alto número de saldos y edades faltantes.
- El campo `tipo` incluye valores no esperados (`-1`, `unknown`).
- El patrón de cuentas repetidas requiere análisis de negocio antes de eliminar o consolidar registros.

#### Reglas que requieren aprobación del negocio
- Si las filas repetidas por `cuenta_id` representan varios trimestres, registros históricos o errores.
- Si el dato `edad` debe estar obligatorio o si se admite ausencia y posterior cálculo.
- Si `-1` y `unknown` deben considerarse valores inválidos o categorías de negocio aún no definidas.
- Si el `saldo` puede estar vacío antes de calcularlo o si debe existir siempre en la fuente.

---

### 3.3 estados_financieros_anuales.csv

#### Columnas y registros
- Total de registros: 1000
- Columnas: `cuenta_id`, `fecha`, `transaccion`, `monto`, `descripcion`

#### Campos vacíos por columna
| Columna | Vacíos |
|---|---:|
| cuenta_id | 0 |
| fecha | 0 |
| transaccion | 0 |
| monto | 48 |
| descripcion | 184 |

#### Fechas inválidas o ambiguas
- Fechas inválidas o ambiguas detectadas: 197
- El patrón muestra varias fechas con formato no estándar y ambigüedad en el orden día/mes/año.

#### Montos no numéricos, negativos y cero
| Indicador | Cantidad |
|---|---:|
| montos no numéricos | 0 |
| montos negativos | 232 |
| montos iguales a cero | 116 |
| montos vacíos | 48 |

#### Transacciones detectadas
| Transaccion | Cantidad |
|---|---:|
| compra | 327 |
| deposito | 293 |
| retiro | 278 |
| pago | 51 |
| depósito | 51 |

#### Duplicados potenciales
- Registros completos duplicados: 0
- `cuenta_id` repetida: 20 claves con duplicados, con 980 filas adicionales respecto a las claves únicas

#### Conclusión técnica
- El archivo presenta montos negativos y ceros en cantidad relevante.
- La columna `descripcion` tiene muchos valores vacíos.
- Hay variaciones de texto para transacciones aparentemente equivalentes (`deposito` y `depósito`), lo que indica la necesidad de normalización de vocabulario.

#### Reglas que requieren aprobación del negocio
- Si las cuentas repetidas representan historiales o registros recurrentes de cierre.
- Si el monto negativo o cero puede ocurrir en transacciones específicas o debe rechazarse completamente.
- Si `deposito` y `depósito` deben unificarse como una misma categoría antes de cualquier procesamiento.

---

## 4. Distinguir errores confirmados de decisiones de negocio

### 4.1 Errores confirmados por los datos
Estos problemas aparecen de manera cuantificada en los archivos reales analizados:

- vacíos en columnas críticas;
- fechas con formatos distintos y ambiguos;
- montos vacíos, negativos y cero en varios registros;
- valores de tipo no reconocidos o inconsistentes;
- `descripcion` vacía en estados anuales;
- cuentas repetidas en los archivos de intereses y estados anuales;
- variantes de texto equivalentes para la misma operación (`deposito` y `depósito`).

### 4.2 Decisiones que requieren aprobación del negocio
Estas son condiciones que no pueden resolverse solo con la inspección técnica:

- si un monto negativo o cero es válido para alguna operación financiera;
- si una cuenta repetida debe consolidarse, mantenerse o revisarse manualmente;
- si la fecha debe interpretarse como día-mes-año, mes-día-año o año-mes-día;
- si los tipos `invalido`, `unknown`, `-1` representan errores de carga o categorías aún no definidas;
- si `depósito` y `deposito` deben normalizarse como una sola transacción;
- si los saldos vacíos son el resultado de un dato faltante o de un cálculo que aún no se ha ejecutado;
- qué criterio se usará para rechazar o retener registros con errores de calidad.

No se inventan políticas contables ni fórmulas financieras. La diferencia entre validación técnica y decisión de negocio quedó explicitada en este análisis.

---

## 5. Propuesta de casos de prueba para Spring Batch

### 5.1 Casos válidos
- fecha con formato unificado y coherente;
- tipo reconocido y canónico;
- monto numérico, positivo y no vacío;
- cuenta_id con referencia estable;
- descripción presente en estados anuales;
- registros únicos sin duplicación de clave natural.

### 5.2 Casos inválidos
- fecha con formato ambiguo como `11-12-2024`;
- monto vacío;
- monto `0` cuando la regla de negocio todavía no lo autoriza;
- monto negativo para una operación que requiere positivo;
- tipo `invalido` o `unknown`;
- saldo vacío en intereses;
- edad fuera de rango;
- cuenta repetida sin criterio de negocio aprobado;
- archivo con descripción vacía cuando se requiere información administrativa.

Estos casos son útiles como base para pruebas de validación y de rechazo en la próxima etapa de batch, pero no implementan jobs ni cálculo financiero.

---

## 6. Conclusión de la Etapa 5

La Etapa 5 confirma que los tres CSV reales sí existen dentro del proyecto y fueron leídos completos. Se cuantificaron los problemas de calidad de los datos y se distinguieron los casos confirmados por los datos de los que requieren aprobación del negocio.

La evidencia actual demuestra que los archivos legacy presentan un nivel claro de ruido operacional, pero no se concluye ninguna regla financiera ni se implementa cálculo contable; solo se prepara el análisis técnico para la siguiente etapa de diseño de validación y futuros jobs Spring Batch.

---

## 7. Archivos modificados

- `ETAPA_5_ANALISIS_DATOS.md`

Se mantuvo intacto el contenido de los CSV originales y no se modificó ninguna parte de Semana 8.

## 8. Resultado concreto de la revisión

- `movimientos_financieros_diarios.csv`: 1000 registros, 234 con riesgo de fecha, 125 montos negativos, 20 ceros, 168 vacíos en monto, 383 tipos no esperados.
- `intereses_trimestrales.csv`: 1000 registros, 204 saldos vacíos, 171 edades vacías, 46 edades fuera de rango, 325 tipos no esperados, 50 cuentas repetidas.
- `estados_financieros_anuales.csv`: 1000 registros, 197 fechas ambiguas, 232 montos negativos, 116 ceros, 184 descripciones vacías, 20 cuentas repetidas.

Estos resultados quedaron documentados en este archivo y son la base para la siguiente etapa, sin asumir reglas contables ni financieras aún no validadas por el negocio.

Si me autorizas, continuaré con la siguiente etapa de diseño y preparación de reglas de validación para Spring Batch.
