# Proyecto Backend III - Exp3_S7_Grupo2

## 1. Definición de la Arquitectura de Eventos
* **Patrón Seleccionado:** Se implementa una Arquitectura Orientada a Eventos (EDA) utilizando el patrón de Publicación-Suscripción (Pub/Sub) mediante coreografía, donde los microservicios reaccionan de manera desacoplada a los cambios de estado.
* **Justificación:** Este enfoque permite que las transacciones de transferencias se procesen de forma asíncrona, garantizando escalabilidad y tolerancia a fallos ante interrupciones temporales en la red o en los servicios.

## 2. Diagrama de la Solución de Arquitectura
* **Flujo de Eventos:**
  1. El cliente inicia una transacción en el Banco API (Producer).
  2. El microservicio publica el evento `TRANSFERENCIA_REALIZADA` estructurado en formato JSON.
  3. El mensaje viaja hacia el broker de Apache Kafka a través del tópico `banco-xyz.transferencias` (configurado con 3 particiones para distribución de carga).
  4. El Cliente API (Consumer) lee y procesa de forma asíncrona el evento validando el eventId, cuentas, monto y moneda.

## 3. Implementación de Componentes y Tolerancia a Fallos
* **Apache Kafka & Docker:** El broker corre centralizado mediante Docker Compose en el puerto 9092, facilitando la comunicación local y en contenedores.
* **Resilience4j:** Se incorporan mecanismos de resiliencia para proteger la disponibilidad del sistema ante fallos de conectividad en las peticiones sincrónicas y asíncronas.

## 4. Instrucciones de Ejecución y Evidencias
* Clona el repositorio y ubícate en la carpeta:
  ```bash
  cd Exp3_S7_Grupo2
  Levanta el entorno completo utilizando Docker Compose en tu instancia de AWS EC2:

Bash
sudo docker compose up --build -d
Evidencias de Ejecución (Requeridas):

Captura 1: Ejecución de Kafka mediante Docker Compose (docker compose ps).

Captura 2: Topic de Kafka con sus 3 particiones (banco-xyz.transferencias).

Captura 3: Ejecución y registro del Banco API en Eureka.

Captura 4: Recepción y procesamiento exitoso del evento TRANSFERENCIA_REALIZADA en el Cliente API.