# Etapa 3 - Documentación de migración del sistema bancario legado

Este documento corresponde a la Etapa 3 del proyecto Semana 9 y se limita a la documentación técnica y de negocio. No se implementan endpoints, jobs, integraciones reales ni despliegues. Toda la información se fundamenta en la estructura actual del repositorio y en la referencia de datos descrita en el README.

## 1. Alcance y base del proyecto

La base del proyecto es un monorepo Java 17 con Spring Boot 3.1.6 y Spring Cloud 2022.0.5, organizado en nueve módulos funcionales:

- `platform/config-server`
- `platform/eureka-server`
- `services/clientes`
- `services/cuentas`
- `services/pagos`
- `batch/batch-jobs`
- `bff/web`
- `bff/mobile`
- `bff/atm`

La estructura actual confirma que los módulos están en fase base: contienen POM, clase de arranque y configuración inicial, pero aún no existen reglas financieras, persistencia implementada ni flujos funcionales definitivos. Por tanto, esta documentación se centra en la estrategia de migración y la preparación del diseño antes de la ejecución técnica.

---

## 2. Cinco procesos críticos para migrar desde el sistema bancario legado

### 2.1 Carga, validación y limpieza de archivos legacy
La primera etapa es convertir los archivos CSV legacy en entradas confiables para la nueva plataforma. La evidencia documentada en el README indica que existen problemas reales en los datos: fechas inconsistentes, campos vacíos, montos inválidos, tipos desconocidos, duplicados y rangos fuera de la norma.

Relevancia:
- evita propagación de errores de negocio en clientes, cuentas y pagos;
- permite establecer una línea base confiable antes de cualquier cálculo financiero;
- define la calidad mínima que debe cumplir cada dato antes de pasar al siguiente proceso.

### 2.2 Normalización de entidades financieras y de referencia
Los datos del legado no están completamente alineados con un modelo único de negocio. El archivo de movimientos no identifica claramente la cuenta asociada, y el archivo de intereses no ofrece una regla explícita de tasa o fórmula mensual. Esto obliga a normalizar una identidad de cliente, cuenta, movimiento y periodo antes de cualquier procesamiento.

Relevancia:
- permite mapear los datos del sistema legado a los dominios de clientes, cuentas y pagos;
- reduce ambigüedad entre entidades y evita conflictos de integración;
- crea una base común para auditoría y trazabilidad.

### 2.3 Reconciliación de saldos e intereses
El README señala que no se deben calcular intereses ni saldos hasta confirmar reglas. En un sistema bancario, esta validación es crítica porque cualquier cálculo incorrecto afecta la percepción del cliente, la contabilidad y los procesos de pago.

Relevancia:
- asegura que la migración no reproduzca errores del sistema legado;
- prepara la validación de consistencia contable;
- define la confianza mínima para cualquier operación financiera posterior.

### 2.4 Orquestación y propagación de eventos de negocio
La arquitectura del proyecto contempla Spring Cloud, Kafka y servicios desacoplados. La migración debe incluir una capa de eventos para comunicar cambios de estado entre clientes, cuentas y pagos y para soportar procesos batch y BFF channel-oriented.

Relevancia:
- reduce acoplamiento entre servicios;
- permite registrar eventos de negocio en tiempo real y batch;
- mejora trazabilidad y facilita pruebas de integración.

### 2.5 Gobernanza de calidad, auditoría y validación final
La migración no debe centrarse solo en mover datos; debe incluir una capa de auditoría y revisión de resultados para confirmar que todos los procesos de transformación cumplen con las reglas del negocio y la evidencias esperadas.

Relevancia:
- permite validar la integridad del ejercicio antes del despliegue;
- facilita la demostración documental a la evaluación;
- reduce riesgo de errores operativos antes de entrar en producción.

---

## 3. Arquitectura propuesta y justificación de tres requisitos de negocio prioritarios

### 3.1 Arquitectura propuesta
La arquitectura recomendada para la migración es una solución basada en microservicios con una capa de configuración, descubrimiento, batch y presentación por canal.

Esquema conceptual:

- `platform/config-server`: centraliza configuraciones y perfiles.
- `platform/eureka-server`: resuelve nombres de servicio y habilita comunicación dinámica.
- `services/clientes`: administra información de clientes y perfiles.
- `services/cuentas`: administra cuentas, movimientos y estados contables.
- `services/pagos`: orquesta transferencias, pagos y validaciones funcionales.
- `batch/batch-jobs`: ejecuta cargas, validación y reconciliación desde archivos legacy.
- `bff/web`, `bff/mobile`, `bff/atm`: expone servicios para los distintos canales de interacción.

La arquitectura prioriza:

1. separación de dominios por negocio;
2. desacople del procesamiento batch del negocio en línea;
3. uso de Spring Cloud para descubrimiento y configuración;
4. canales especializados según tipo de cliente y operación.

### 3.2 Requisito de negocio 1: trazabilidad completa de movimientos y saldos
Es fundamental que cada operación financiera pueda rastrearse desde el archivo legacy, pasando por validación, normalización, cálculo y publicación del evento correspondiente.

Justificación:
- el README reconoce problemas de calidad en los CSV y ausencia de reglas definitivas para intereses y saldos;
- un banco necesita evidencia de origen y consistencia para cada movimiento;
- la trazabilidad reduce errores regulatorios y mejora la resolución de incidentes.

### 3.3 Requisito de negocio 2: integridad y seguridad de las operaciones de pago
La nueva plataforma debe garantizar que un pago no se procese si no existe la cuenta, el cliente o las condiciones mínimas de validación. Esto es prioritario en cualquier flujo financiero y es consistente con la separación de servicios y con la dependencia de seguridad y OAuth2 ya prevista en los POM.

Justificación:
- así se evitan inconsistencias en transacciones críticas;
- mejora la confiabilidad del flujo de negocio;
- permite proteger los servicios con autenticación, autorización y validación de entrada.

### 3.4 Requisito de negocio 3: disponibilidad y continuidad operativa para migración gradual
La migración debe hacerse sin interrumpir el negocio y permitir carga progresiva de datos, validación y reconciliación antes de la puesta en producción del sistema nuevo. La arquitectura con batch + servicios + BFF se adapta a esta necesidad.

Justificación:
- el proyecto está en fase de migración desde un sistema legado, no en reemplazo inmediato total;
- requiere validación controlada, reintentos seguros y evidencia operativa;
- favorece una estrategia incremental y comprobable.

---

## 4. Responsabilidad de cada uno de los nueve módulos

### 4.1 `platform/config-server`
Responsabilidad: centralizar la configuración de todos los servicios y mantener perfiles de entorno. Debe ser la primera capa de coordinación para conectividad, secretos locales y propiedades compartidas.

### 4.2 `platform/eureka-server`
Responsabilidad: ofrecer registro y descubrimiento de servicios. Permite que los microservicios se encuentren sin depender de IPs o endpoints fijos.

### 4.3 `services/clientes`
Responsabilidad: gestionar información del cliente, validaciones de identidad, perfiles básicos y referencias de negocio asociadas a la relación con la cuenta y los movimientos.

### 4.4 `services/cuentas`
Responsabilidad: administrar cuentas, saldos, estados contables y lógica financiera de la cuenta. Es el dominio clave para la migración del legado de datos bancarios.

### 4.5 `services/pagos`
Responsabilidad: orquestar pagos y movimientos entre cuentas, validaciones de negocio, y transmisión de eventos hacia los demás servicios o consumidores.

### 4.6 `batch/batch-jobs`
Responsabilidad: ejecutar procesos batch de carga, validación, transformación y conciliación de archivos legacy. Es la capa de etapa de migración que conecta con datos CSV y el estado del sistema legado.

### 4.7 `bff/web`
Responsabilidad: proveer el front de canal web, con adaptación de contratos y orquestación para clientes que operan desde web.

### 4.8 `bff/mobile`
Responsabilidad: exponer el canal móvil con el mismo modelo de negocio, pero adaptado a una experiencia distinta y a una capa de cliente más ligera.

### 4.9 `bff/atm`
Responsabilidad: atender operaciones del canal ATM, con reglas y flujos específicos del negocio de cajero automático y transacciones rápidas.

---

## 5. Plan de implementación y relación con requisitos, módulos, pruebas y evidencias

Se recomienda seguir la siguiente secuencia de trabajo para mantener trazabilidad y evidencias durante la evaluación.

| Requisito de evaluación | Objetivo | Módulos involucrados | Pruebas sugeridas | Evidencias esperadas |
|---|---|---|---|---|
| R1. Definir procesos críticos de migración | Identificar y priorizar la carga, validación, reconciliación y gobernanza del legado | `batch/batch-jobs`, `services/cuentas`, `services/pagos`, `services/clientes` | pruebas de validación de CSV, pruebas unitarias de reglas de normalización, smoke tests de batch | documentos de flujo, capturas de validación, reporte de datos limpios/importados |
| R2. Proponer arquitectura de migración | Diseñar la separación por dominio y canales | `platform/config-server`, `platform/eureka-server`, `services/*`, `bff/*` | pruebas de descubrimiento y configuración, validación de estructura de módulos | pantallas o artefactos de arquitectura, diagrama de componentes |
| R3. Explicar responsabilidad de los nueve módulos | Justificar el rol de cada módulo dentro del proyecto | todos los módulos | revisión de estructura, validación de dependencias y nombres de artefactos | lista de módulos y asignaciones documentadas |
| R4. Preparar plan de implementación | Definir entregas por dominio y por fase | `services/clientes`, `services/cuentas`, `services/pagos`, `batch/batch-jobs`, `bff/*` | plan de fases, pruebas de regresión por dominio, checklist de evidencias | plan de implementación, matriz de trazabilidad |
| R5. Documentar calidad de datos y decisiones pendientes | Registrar riesgos y decisiones no confirmadas | `batch/batch-jobs`, `services/cuentas`, `services/pagos` | validación de calidad de archivos, pruebas de tolerancia a errores | inventario de problemas CSV, log de decisiones pendientes |

### Secuencia recomendada
1. Fase 1: validación de calidad de datos legado y definición de reglas de entrada.
2. Fase 2: modelado de dominios y contratos de entidad para clientes, cuentas y pagos.
3. Fase 3: implementación base de servicios y configuración del ecosistema Spring Cloud.
4. Fase 4: desarrollo de jobs batch con validación y auditoría.
5. Fase 5: integración de BFF por canal y pruebas end-to-end.
6. Fase 6: validación final, evidencias y cierre.

Esta secuencia respeta la intención del README: la plataforma aún no tiene negocio funcional implementado ni despliegue confirmado, por lo que la prioridad es calidad, trazabilidad y definición del sistema antes de la ejecución real.

---

## 6. Problemas de calidad de los CSV y decisiones financieras pendientes

### 6.1 Problemas de calidad detectados
El README identifica los siguientes problemas en los archivos legacy:

- fechas con distintos formatos;
- campos vacíos;
- montos inválidos;
- tipos desconocidos;
- duplicados;
- valores fuera de rango;
- archivo de intereses trimestral sin especificar tasa ni fórmula mensual;
- archivo de movimientos sin identificación clara de la cuenta asociada;
- falta de una referencia explícita para el tratamiento de intereses, saldos y periodos monetarios.

### 6.2 Impacto operativo
Estos problemas pueden provocar:

- diferencias de saldo incontables;
- errores de conciliación;
- decisiones de negocio basadas en datos inconsistentes;
- fallas en la validación de pagos y en la trazabilidad de transacciones.

### 6.3 Decisiones financieras que aún deben confirmarse
Antes de implementar cálculo de saldos o intereses, se deben confirmar, sin inventar valores, las siguientes decisiones:

- cuál es la regla de cálculo de saldo inicial y saldo final;
- cómo se identificará el cliente y la cuenta en cada movimiento;
- cuál es la base de fechas y periodo de cálculo para intereses;
- si los intereses se aplican de forma mensual, trimestral o anual;
- qué tasa o fórmula aplica para cada tipo de cuenta o producto;
- cómo se manejan movimientos compensados, duplicados o valores anómalos;
- si existe una política de tolerancia para datos incompletos o fuera de rango;
- qué criterios de auditoría y aprobación deben definirse antes de cargar la información al nuevo sistema.

Estas decisiones no pueden asumirse ni automatizarse sin una validación explicita del negocio, ya que se trata de reglas financieras sensibles y de impacto contable.

---

## 7. Conclusión de la Etapa 3

La Etapa 3 deja la base documental de la migración: identifica los cinco procesos críticos, propone la arquitectura recomendada, documenta la responsabilidad de cada módulo, define un plan de ejecución y registra los riesgos de calidad del legado y las decisiones financieras pendientes. La propuesta es consistente con la estructura actual del proyecto y con la intención del README: no se implementan funcionalidades ni despliegues todavía, sino que se prepara la base para una ejecución técnica segura y trazable.

---

## 8. Archivo modificado

- `ETAPA_3_DOCUMENTACION.md`

Toda la documentación quedó dentro del proyecto de Semana 9 y no se modificó ningún archivo de la etapa anterior ni de estructuras de Semana 8.

## 9. Validaciones realizadas

- Revisión del README del proyecto y de la estructura de módulos.
- Revisión del `pom.xml` raíz para confirmar la composición del monorepo.
- Revisión de los POM de cada módulo para verificar responsabilidades y dependencias base.
- Verificación de que la documentación no altera lógica funcional ni implementaciones existentes.
- Verificación del estado del repo para confirmar que los cambios quedan dentro del proyecto actual.

## 10. Pendiente para la siguiente etapa

- confirmar reglas financieras exactas con el negocio;
- definir la calidad mínima de los CSV y la estrategia de limpieza por tipo de dato;
- listar eventos y contratos de integración entre servicios y BFF;
- diseñar pruebas por módulo y evidencias finales antes de ejecutar código funcional.

Si me autorizas, continuaré con la siguiente etapa dentro del mismo proyecto Semana 9, sin tocar archivos de Semana 8.
