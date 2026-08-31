package com.duoc.migracion.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class BancoJobListener extends StepExecutionListenerSupport implements JobExecutionListener {

    private Instant startTime;
    private Instant endTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = Instant.now();
        System.out.println("[BancoJobListener] Iniciando Job: " + jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);

        List<Throwable> failures = jobExecution.getAllFailureExceptions();
        long processed = jobExecution.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();
        long skipped = jobExecution.getStepExecutions().stream().mapToLong(StepExecution::getSkipCount).sum();

        System.out.println("[BancoJobListener] Job finalizado: " + jobExecution.getJobInstance().getJobName());
        System.out.println("[BancoJobListener] Inicio: " + startTime);
        System.out.println("[BancoJobListener] Fin: " + endTime);
        System.out.println("[BancoJobListener] Duracion: " + duration.toMillis() + " ms");
        System.out.println("[BancoJobListener] Estado: " + jobExecution.getStatus());
        System.out.println("[BancoJobListener] Registros procesados: " + processed);
        System.out.println("[BancoJobListener] Registros omitidos: " + skipped);
        if (!failures.isEmpty()) {
            System.out.println("[BancoJobListener] Errores detectados:");
            failures.forEach(failure -> System.out.println("- " + failure.getMessage()));
        }
    }
}
