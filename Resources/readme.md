# Recursos para verificar el bróker

En esta carpeta, encontrarás una colección de recursos que facilitan la verificación del servicio.

El bróker interactúa con servidores FHIR y servidores KIE. Se proporciona un fichero docker compose que permite la ejecución de un servidor HAPIFHIR y de Business Central en docker

Además se proporcionan ficheros para facilitar la creación de recursos FHIR desde Postman y los bpmn de dos procesos configurados para recibir señales

## Contenidos

- `Solicitudes servidor FHIR.postman_collection.json`: Colección de Postman para realizar pruebas y validar las funcionalidades del servicio. Permite crear SuscriptionTopics en un servidor FHIR
- `create-ServiceRequest.bpmn`: Activo con el proceso de negocio que recibe la señal "create-ServiceRequest".
- `update-ServiceRequest.bpmn`: Activo con el proceso de negocio que recibe la señal "update-ServiceRequest"
- `application.yaml`: Fichero de configuración del servidor FHIR (R5 y suscripciones con resthooks)
- `docker-compose.yaml`: Para facilitar el despliegue de contenedores con HAPI-FHIR y BC

## Requisitos

- [Postman](https://www.postman.com/downloads/)
- [Docker](https://www.docker.com/)


## Instrucciones

### Arrancar los servidores FHIR y BC

Para ello ejecutar el comando ```docker compose up -d```

A partir de ahora tendrá disponible
   - Business Central en el puerto 8080 (localhost:8080)
   - El servidor Hapi FHIR en el puerto 8888 (localhost:8888)  


### Arrancar un servidor KIE con BC

1. **Acceder a Business Central**:
   - Abre tu instancia de Business Central en un navegador (localhost:8080).
2. **Crear el Proyecto**:
   - En el menú principal, selecciona "Proyectos".
   - Crea un nuevo proyecto.
   - En la ventana de activos aparece la opción de importar activos, se deben seleccionar los archivos .bpmn que se proporcionan
   - Como alternativa se proporcional el kjar
4. **Desplegar el Proyecto**:
   - Una vez importado, selecciona el proyecto y haz clic en "Build & Deploy".
   - Eso levantará un servidor kie con los procesos desplegados
5. **Consultar las instancias de procesos**:
   - Desde el menú de inicio, vamos al menú "instancias de proceso". Desde aquí se pueden visualizar las instancias creadas al recibir señales.
     
![Proyecto en BC](https://github.com/tfg-projects-dit-us/FKBroker/blob/master/Resources/img/proyectoEnBC.jpg)

### Servidor FHIR local (Alternativa)

Si prefiere levantar el servidor FHIR local sin usar docker puede seguir estas instrucciones:

1. Descargar el servidor desde  https://github.com/hapifhir/hapi-fhir-jpaserver-starter
2. Editar el fichero application.properties para configurar:
   - La versión (hapi.fhir.fhir_version:R5)
   - El puerto donde queremos que esté disponible el servidor (server.port:8888)
   - Habilitar las suscripciones restHook (hapi.fhir.suscription.resthook_enabled:true)
3. Arrancar el servidor:  mvn spring-boot:run   

### Colección de Postman

1. **Instalar Postman**: Asegure tener Postman instalado
2. **Importar la Colección**:
   - Abrir Postman.
   - Hacer clic en el botón "Importar" en la parte superior izquierda.
   - Seleccionar el archivo `Solicitudes servidor FHIR.postman_collection.json`.
3. **Ejecutar Requests**:
   - Navegar por las carpetas y requests dentro de la colección importada.
   - Seleccionar el request que se desea ejecutar y hacer clic en "Send" para realizar la llamada al servicio.
   - Estos Request se encuentran enumerados en orden de uso.

