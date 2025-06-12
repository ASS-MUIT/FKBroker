# Simulador de Servidor FHIR

## Descripción

Este proyecto es una aplicación para simular la gestión de suscripciones de un servidor FHIR, Kie Server y Spring Boot. La aplicación permite gestionar subscripciones y temas de subscripción a través de operaciones REST, visualizar y gestionar eventos de las subscripciónes a través de una interfaz web.

## Características

- Gestión de subscripciones mediante API REST.
- Gestión de temas de subscripción mediante API REST.
- Implementación de operaciones especiales $status y $events.
- Implementación de mensajes de handshake y heartbeat.
- Visualización de suscripciones y temas de subscripción mediante interfaz web.
- Viasualización y creación personalizada de eventos de subscripciones mediante interfaz web.

## Requisitos

- Java 8 o superior
- Maven 3.6.3 o superior
- PostgreSQL 12 o superior

## Instalación

1. Clonar el repositorio

   ```bash
   git clone https://github.com/tfg-projects-dit-us/FKBroker
   cd fhirserver-service
   ```

2. Configurar la base de datos PostgreSQL
   
   Crear la base de datos

   ```bash
    sudo -u postgres createdb fhirserver
   ```
   Otorgar todos los privilegios al usuario jbpm (O el que se configure como usuario en application.properties )

   ```bash
    GRANT ALL PRIVILEGES ON DATABASE fhirserver TO jbpm;
   ```
   Configurar application.properties

   ```bash
   spring.datasource.username=jbpm
   spring.datasource.password=jbpm
   spring.datasource.url=jdbc:postgresql://localhost:5432/fhirserver
   spring.datasource.driver-class-name=org.postgresql.xa.PGXADataSource
   ```
4. Compilar y ejecutar la aplicación

   Desde el directorio raiz de la aplicación ejecutar el siguiente comando:

   ```bash
  mvn clean install
  mvn sprint-boot:run -Ppostgres 
   ```
 
