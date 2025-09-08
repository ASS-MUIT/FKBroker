![GitHub top Language](https://img.shields.io/github/languages/top/tfg-projects-dit-us/FKBroker)
![GitHub forks](https://img.shields.io/github/forks/tfg-projects-dit-us/FKBroker?style=social)
![GitHub contributors](https://img.shields.io/github/contributors/tfg-projects-dit-us/FKBroker)
![GitHub Repo stars](https://img.shields.io/github/stars/tfg-projects-dit-us/FKBroker?style=social)
![GitHub repo size](https://img.shields.io/github/repo-size/tfg-projects-dit-us/FKBroker)
![GitHub watchers](https://img.shields.io/github/watchers/tfg-projects-dit-us/FKBroker)
![GitHub](https://img.shields.io/github/license/tfg-projects-dit-us/FKBroker)


<img src="https://github.com/tfg-projects-dit-us/FKBroker/blob/master/Resources/img/steampunk.png" width="200" />

# FKBroker

En este proyecto se desarrolla un bróker FHIR-KIE, que permite gestionar suscripciones a servidores FHIR y envíos de señales a servidores KIE, derivadas de la recepción de notificaciones de los primeros.

Actualmente es una versión beta en la que se han incluido sólo las capacidades más elementales

Está desarrollado en el Departamento de Ingeniería Telemática de la Universidad de Sevilla

## Licencia

Este proyecto está licenciado bajo los términos de la [Licencia Pública General de GNU (GPL) versión 3](https://www.gnu.org/licenses/gpl-3.0.html).
Este proyecto utiliza las siguientes bibliotecas de terceros:

- jBPM (Apache 2.0) - https://www.jbpm.org/
Cada una de estas librerías mantiene su propia licencia y términos de uso.

## License

This project is licensed under the terms of the [GNU General Public License (GPL) version 3](https://www.gnu.org/licenses/gpl-3.0.html).
This project uses the following third-party libraries:

    jBPM (Apache 2.0) - https://www.jbpm.org/
Each of these libraries maintains its own license and terms of use.

## Reconocimientos

Este proyecto es el resultado del trabajo desarrollado por los alumnos que a continuación se mencionan, bajo la supervisión de la profesora Isabel Román Martínez.

**Autores:**
- [Juan Manuel Brazo Mora](https://github.com/juanmabrazo98): desarrolla en su TFG la versión inicial del proyecto.
- [José Antonio Pérez Beltrán](https://github.com/josepebe12): desarrolla la V.1.1.0, segunda versión oficial, en la que se amplian las capacidades, principalmente en la interfaz FHIR-Bróker

**Supervisora:**
- [Isabel Román Martínez](https://github.com/Isabel-Roman), Profesora del Departamento de Ingeniería Telemática de la Universidad de Sevilla

La supervisión incluye la generación de ideas, la corrección, el desarrollo de algunos componentes y la orientación técnica durante todo el proceso de desarrollo.

**Graphical design**
- Icon created by Paul J. -Flaticon https://www.flaticon.com/free-icons/steampunk
<a href="https://www.flaticon.com/free-icons/steampunk" title="steampunk icons">Steampunk icons created by Paul J. - Flaticon</a>

## Contenido
1. fkbroker-service: código del bróker
2. Resources: recursos adicionales para verificación y pruebas
## Dependencias
**En la versión actual utilizamos:**
* _spring boot starter_ [_2.6.15_](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter/2.6.15)
* _kie server_: [_7.74.1.Final_](https://mvnrepository.com/artifact/org.kie/kie-server-spring-boot-starter/7.74.1.Final)

## Contributions

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement". Do not forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

