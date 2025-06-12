package us.dit.fkbroker.service.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Permite definir un bean único para FhirContext, asegurando que toda la
 * aplicación use la misma instancia y se beneficie de las optimizaciones
 * internas del contexto.
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Configuration
public class FhirConfig {

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR5();
    }

    @Bean
    public IParser jsonParser(FhirContext fhirContext) {
        return fhirContext.newJsonParser();
    }

}
