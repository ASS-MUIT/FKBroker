![GitHub top Language](https://img.shields.io/github/languages/top/tfg-projects-dit-us/FKBroker)
![GitHub forks](https://img.shields.io/github/forks/tfg-projects-dit-us/FKBroker?style=social)
![GitHub contributors](https://img.shields.io/github/contributors/tfg-projects-dit-us/FKBroker)
![GitHub Repo stars](https://img.shields.io/github/stars/tfg-projects-dit-us/FKBroker?style=social)
![GitHub repo size](https://img.shields.io/github/repo-size/tfg-projects-dit-us/FKBroker)
![GitHub watchers](https://img.shields.io/github/watchers/tfg-projects-dit-us/FKBroker)
![GitHub](https://img.shields.io/github/license/tfg-projects-dit-us/FKBroker)


<img src="https://github.com/tfg-projects-dit-us/FKBroker/blob/master/Resources/img/steampunk.png" width="200" />

# FKBroker

This project develops a FHIR-KIE broker that allows managing subscriptions to FHIR servers and sending signals to KIE servers, derived from receiving notifications from the former.

Currently, it is a beta version that includes only the most basic capabilities.

It is developed in the Department of Telematics Engineering at the University of Seville.

## License

This project is licensed under the terms of the [GNU General Public License (GPL) version 3](https://www.gnu.org/licenses/gpl-3.0.html).
This project uses the following third-party libraries:

- jBPM (Apache 2.0) - https://www.jbpm.org/
- HAPI FHIR (Apache 2.0) - https://github.com/hapifhir/hapi-fhir
  
Each of these libraries maintains its own license and terms of use.

## Acknowledgments

This project is the result of work developed by the students mentioned below, under the supervision of Professor Isabel Román Martínez.

**Authors:**
- [Juan Manuel Brazo Mora](https://github.com/juanmabrazo98): developed the initial version of the project in his Bachelor's Thesis.
- [José Antonio Pérez Beltrán](https://github.com/josepebe12): developed V.1.1.0, the second official version, which expands the capabilities, mainly in the FHIR-Broker interface

**Supervisor:**
- [Isabel Román Martínez](https://github.com/Isabel-Roman), Professor at the Department of Telematics Engineering, University of Seville

The supervision includes idea generation, proofreading, development of some components, and technical guidance throughout the development process.

**Graphical design**
- Icon created by Paul J. -Flaticon https://www.flaticon.com/free-icons/steampunk
<a href="https://www.flaticon.com/free-icons/steampunk" title="steampunk icons">Steampunk icons created by Paul J. - Flaticon</a>

## Contents
1. fkbroker-service: broker code
2. Resources: additional resources for verification and testing

## Dependencies
**In the current version we use:**
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
