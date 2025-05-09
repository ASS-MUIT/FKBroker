package us.dit.fhirserver.service.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestClient {

    private final RestTemplate restTemplate;

    public RestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Crea los encabezados HTTP necesarios para la autenticación.
     * 
     * @return los encabezados HTTP.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/fhir+json"));
        return headers;
    }

    public Boolean sendMessage(String url, String message) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<String>(message, headers);

            // TODO Eliminar remplazo
            url = url.replace("host.docker.internal", "localhost");

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            System.err.println("General error: " + e.getMessage());
            return false;
        }

    }
}
