package us.dit.fhirserver.service.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class SubscriptionSchedulerManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<Long, ScheduledFuture<?>> tareas = new ConcurrentHashMap<>();

    public void iniciarTarea(Long subscriptionId, long intervaloMs, Runnable tarea) {
        cancelarTarea(subscriptionId);
        ScheduledFuture<?> nuevaTarea = scheduler.scheduleAtFixedRate(tarea, 0, intervaloMs, TimeUnit.MILLISECONDS);
        tareas.put(subscriptionId, nuevaTarea);
    }

    public void cancelarTarea(Long subscriptionId) {
        ScheduledFuture<?> tarea = tareas.remove(subscriptionId);
        if (tarea != null) {
            tarea.cancel(false);
        }
    }

    public void detenerTodas() {
        tareas.values().forEach(f -> f.cancel(false));
        tareas.clear();
    }

}
