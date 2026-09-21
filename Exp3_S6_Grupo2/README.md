# Proyecto Backend III - Exp3_S6_Grupo2

Ecosistema de Microservicios para Banco XYZ integrando Eureka Server, Config Server y resiliencia.

## Ecosistema de Microservicios

1. **Exp2_S6_Eureka_Server**
   - Servidor de descubrimiento en puerto 8761.
2. **Exp2_S6_Config_Server**
   - Servidor de configuración centralizada en puerto 8888.
3. **Exp2_S6_Banco_API**
   - API Principal con resiliencia y seguridad.

---

## Microservicio Exp2_S6_Cliente_API 
- **Puerto**: 8082
- **Nombre en Eureka**: CLIENTE-XYZ
- **Integración**: Consume parámetros de Config Server y se registra en Eureka.
- **Endpoints disponibles**:
  - GET /api/clientes/health -> Verifica estado y lectura desde Config Server.
  - GET /api/clientes/1 -> Devuelve información de prueba de un cliente en JSON.
