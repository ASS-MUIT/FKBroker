
/**
*  This file is part of FKBroker - Broker sending signals to KIEServers from FHIR notifications.
*  Copyright (C) 2024  Universidad de Sevilla/Departamento de Ingeniería Telemática
*
*  FKBroker is free software: you can redistribute it and/or
*  modify it under the terms of the GNU General Public License as published
*  by the Free Software Foundation, either version 3 of the License, or (at
*  your option) any later version.
*
*  FKBroker is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
*  Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with FKBroker. If not, see <https://www.gnu.org/licenses/>.
**/
package us.dit.fkbroker.service.conf;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Versión nueva de seguridad, sustituye la ocnfiguración por defecto que se
 * genera con el arquetipo maven utilizando convenciones de seguridad más
 * actuales ahora está basada en beans, más información en:
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
 * Esto va bien cuando se utiliza el spring boot starter 2.6.15 y el kie server
 * 7.74.1.Final fecha de la revisión 6/10/2023
 * 
 * TO DO: Utilizar la autenticación basada en oauth o en SAML (SSO) contra un
 * servidor de autenticación externo REF:
 * https://is.docs.wso2.com/en/latest/sdks/spring-boot/ para hacerlo con el
 * identity server de WSO2 usando oauth
 */
@Configuration("kieServerSecurity")
@EnableWebSecurity
public class DefaultWebSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests.antMatchers("/").authenticated()
                .antMatchers(HttpMethod.POST, "/notification/**").permitAll().antMatchers("/fhir/servers")
                .authenticated().antMatchers("/fhir/servers/*").authenticated()
                .antMatchers("/fhir/servers/*/subscriptions").authenticated()
                .antMatchers("/fhir/servers/*/subscriptions/*").authenticated()
                .antMatchers("/fhir/servers/*/subscriptions/*/delete").authenticated().antMatchers("/kie")
                .authenticated().antMatchers("/kie/servers/add").authenticated().antMatchers("/kie/servers/delete")
                .authenticated().antMatchers("/kie/signals/add").authenticated().antMatchers("/kie/signals/delete")
                .authenticated().antMatchers("/img/*").permitAll())
                .exceptionHandling((exceptionHandling) -> exceptionHandling.accessDeniedPage("/access-denied.html"))
                .csrf((csrf) -> csrf.disable()).httpBasic(withDefaults()).cors(withDefaults())
                .formLogin(withDefaults());
        return http.build();
    }

    /**
     * Configuración de la autenticación con autenticación en memoria y encriptada
     * Muy débil no sirve para producción
     **/
    @Bean
    UserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {

        // codifico las password en https://bcrypt-generator.com/, uso nombre como
        // password
        // $2a$12$Pa3IIDS5JhAJpiLt5/lT4O5KVw1pyU.dVGpz/q7kEGUAH.JL85tRC
        UserDetails user = User.withUsername("user").password(encoder.encode("user")).roles("kie-server").build();
        // $2a$12$irR0VcP4SdtvAn7cbnXXQ.Cnfk/NlLWZa4mnx0J8EeXFum8Pt1pfm
        UserDetails wbadmin = User.withUsername("wbadmin").password(encoder.encode("wbadmin")).roles("admin").build();
        // Este usuario se va a utilizar para el acceso al servidor
        UserDetails consentimientos = User.withUsername("consentimientos").password(encoder.encode("consentimientos"))
                .roles("kie-server").build();
        // $2a$12$1T7IYm0PmxpWyJFjqTSlm.489.s65TvHJbW4R7d1SG0giNHb5bqAm
        UserDetails kieserver = User.withUsername("kieserver").password(encoder.encode("kieserver")).roles("kie-server")
                .build();

        return new InMemoryUserDetailsManager(wbadmin, user, kieserver, consentimientos);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(), HttpMethod.HEAD.name(),
                HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.PUT.name()));
        corsConfiguration.applyPermitDefaultValues();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
