package us.dit.fhirserver.service.controllers;

import java.util.List;

import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r5.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r5.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.r5.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.Enumerations.CapabilityStatementKind;
import org.hl7.fhir.r5.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.context.FhirContext;

/**
 * Controlador REST que gestiona las llamadas a la operación de metadata.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@RestController
@RequestMapping("/fhir/metadata")
public class MetadataController {

    private final FhirContext fhirContext;

    /**
     * Constructor que inyecta {@link FhirContext}.
     * 
     * @param fhirContext contexto de FHIR.
     */
    @Autowired
    public MetadataController(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    /**
     * Maneja las solicitudes GET al recurso de metadata.
     * 
     * @return el recurso FHIR {@link CapabilityStatement} con la información de
     *         metadata del servidor.
     */
    @GetMapping
    public ResponseEntity<String> getMetadata() {
        CapabilityStatement capability = new CapabilityStatement();

        capability.setId("example-metadata");
        capability.setStatus(PublicationStatus.ACTIVE);
        capability.setFhirVersion(FHIRVersion._5_0_0);
        capability.setKind(CapabilityStatementKind.INSTANCE);
        capability.setFormat(List.of(new CodeType("json")));

        // Describir el soporte para el recurso Subscription
        CapabilityStatementRestComponent rest = new CapabilityStatementRestComponent();
        rest.setMode(RestfulCapabilityMode.SERVER);

        CapabilityStatementRestResourceComponent subscription = new CapabilityStatementRestResourceComponent();
        subscription.setType("Subscription");
        subscription.addInteraction().setCode(TypeRestfulInteraction.CREATE);
        subscription.addInteraction().setCode(TypeRestfulInteraction.READ);
        subscription.addInteraction().setCode(TypeRestfulInteraction.UPDATE);
        subscription.addInteraction().setCode(TypeRestfulInteraction.DELETE);

        rest.addResource(subscription);
        capability.addRest(rest);

        String response = fhirContext.newJsonParser().encodeResourceToString(capability);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }
}
