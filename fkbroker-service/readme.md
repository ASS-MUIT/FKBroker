# Detalles del bróker FHIR-KIE

## Descripción

Este servicio facilita la comunicación indirecta entre servidores FHIR que tengan implementado el marco de suscripción y servidores KIE. Está desarrollado como una aplicación Spring Boot y utiliza las apis HAPIFHIR y jBPM para la implementación de los clientes que se comunican con los servidor FHIR y KIE respectivamente. El servicio permite crear, visualizar, modificar y eliminar suscripciones en servidores FHIR, así como recibir las notificaciones de éstos, y enviar señales a servidores KIE. Proporciona una interfaz web para facilitar la administración del servicio.

## Características

- Gestión de señales y servidores KIE
- Gestión de puntos de recepción de notificaciones FHIR
- Creación y eliminación de suscripciones FHIR
- Verificación y recuperación de canales de notificación FHIR
- Visualización y filtrado de suscripciones FHIR
- Configuración a través de `application.properties`.

## Requisitos

- Java 8 o superior
- Maven 3.6.3 o superior
- PostgreSQL 12 o superior

## Instalación

1. Clonar el repositorio

   ```bash
   git clone https://github.com/tfg-projects-dit-us/FKBroker
   cd broker-service
   ```

2. Configurar la base de datos PostgreSQL
   
   Crear la base de datos

   ```bash
    sudo -u postgres createdb fkbroker
   ```
   Otorgar todos los privilegios al usuario jbpm (O el que se configure como usuario en application.properties )

   ```bash
    GRANT ALL PRIVILEGES ON DATABASE fkbroker TO jbpm;
   ```
   Configurar application.properties

   ```bash
   spring.datasource.username=jbpm
   spring.datasource.password=jbpm
   spring.datasource.url=jdbc:postgresql://localhost:5432/fkbroker
   spring.datasource.driver-class-name=org.postgresql.xa.PGXADataSource
   ```
4. Compilar y ejecutar la aplicación

   Desde el directorio raiz de la aplicación ejecutar el siguiente comando:

   ```bash
    mvn clean install
    mvn sprint-boot:run -Ppostgres 
   ```
 
