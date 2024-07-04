# Proyecto de Business Central y Colección de Postman

En esta carpeta, encontrarás una colección de Postman y dos activos de Business Central que contienen los procesos de negocio usados para la verificación. 

## Contenidos

- `Solicitudes servidor FHIR.postman_collection.json`: Colección de Postman para realizar pruebas y validar las funcionalidades del servicio. Permite crear SuscriptionTopics en un servidor FHIR
- `create-ServiceRequest.bpmn`: Activo con el proceso de negocio que recibe la señal "create-ServiceRequest".
- `update-ServiceRequest.bpmn`:Activo con el proceso de negocio que recibe la señal "update-ServiceRequest"

## Requisitos

- [Postman](https://www.postman.com/downloads/)
- [Business Central](https://www.jbpm.org/)

## Instrucciones de uso

### Proyecto de Business Central

1. **Acceder a Business Central**:
   - Abre tu instancia de Business Central en un navegador.
2. **Importar el Proyecto**:
   - En el menú principal, selecciona "Proyectos".
   - Crea un nuevo proyecto.
   - En la ventana de activos aparece la opción de importar activos, se deben seleccionar los archivos .bpmn
4. **Desplegar el Proyecto**:
   - Una vez importado, selecciona el proyecto y haz clic en "Build & Deploy".
5. **Ejecutar Procesos**:
   - Desde el menú de inicio, vamos al menú "instancias de proceso". Desde aquí se pueden visualizar las instancias creadas al recibir señales.

### Servidor FHIR local

Para las pruebas se ha utilizado el servidor JPA de hapi-FHIR. Los pasos a seguir para tenerlo operativo, en local, son:

1. Descargar el servidor desde  https://github.com/hapifhir/hapi-fhir-jpaserver-starter
2. Editar el fichero application.properties para configurar:
   - La versión (hapi.fhir.fhir_version:R5)
   - El puerto donde queremos que esté disponible el servidor (server.port:8080)
   - Habilitar las suscripciones restHook (hapi.fhir.suscription.resthook_enabled:true)
3. Arrancar el servidor:  mvn spring-boot:run   

### Colección de Postman

1. **Instalar Postman**: Asegúrate de tener Postman instalado en tu máquina.
2. **Importar la Colección**:
   - Abre Postman.
   - Haz clic en el botón "Importar" en la parte superior izquierda.
   - Selecciona el archivo `Solicitudes servidor FHIR.postman_collection.json`.
3. **Ejecutar Requests**:
   - Navega por las carpetas y requests dentro de la colección importada.
   - Selecciona el request que deseas ejecutar y haz clic en "Send" para realizar la llamada al servicio.
   - Estos Request se encuentran enumerados en orden de uso.

