package us.dit.fhirserver.service.services.fhir;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

/**
 * Componente encargado de programar las tareas realizadas periodicamente sobre
 * subscripciones FHIR.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Component
public class SubscriptionSchedulerManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<Long, ScheduledFuture<?>> tareas = new ConcurrentHashMap<>();

    /**
     * Programa una nueva tarea periódica para una subscripción, cancelando si
     * hubiera alguna tarea programada anteriormente para esta misma subscripción.
     * 
     * @param idSubscription identificador de la subscripción.
     * @param intervalo      intervalo de tiempo (en ms) en el que se debe ejecutar
     *                       la tarea.
     * @param tarea          tarea que se debe ejecutar periódicamente.
     */
    public void iniciarTarea(Long idSubscription, long intervalo, Runnable tarea) {
        cancelarTarea(idSubscription);
        ScheduledFuture<?> nuevaTarea = scheduler.scheduleAtFixedRate(tarea, 90, intervalo, TimeUnit.SECONDS);
        tareas.put(idSubscription, nuevaTarea);
    }

    /**
     * Cancela una tarea periódica programada para una subscripción.
     * 
     * @param idSubscription identificador de la subscripción.
     */
    public void cancelarTarea(Long idSubscription) {
        ScheduledFuture<?> tarea = tareas.remove(idSubscription);
        if (tarea != null) {
            tarea.cancel(false);
        }
    }

    /**
     * Cancela todas tarea periódica programadas.
     */
    public void detenerTodas() {
        tareas.values().forEach(f -> f.cancel(false));
        tareas.clear();
    }

    /**
     * Programa una nueva tarea periódica para una subscripción, cancelando si
     * hubiera alguna tarea programada anteriormente para esta misma subscripción.
     * 
     * @param idSubscription identificador de la subscripción.
     * @param intervalo      intervalo de tiempo (en ms) en el que se debe ejecutar
     *                       la tarea.
     * @param tarea          tarea que se debe ejecutar periódicamente.
     */
    public void programaTarea(Runnable tarea, long retraso) {
        scheduler.schedule(tarea, retraso, TimeUnit.SECONDS);
    }

}
