package com.duoc.migracion.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job cuentaAnualJob;
    private final Job interesJob;
    private final Job transaccionJob;

    public JobController(JobLauncher jobLauncher,
                         @Qualifier("cuentaAnualJob") Job cuentaAnualJob,
                         @Qualifier("interesJob") Job interesJob,
                         @Qualifier("transaccionJob") Job transaccionJob) {
        this.jobLauncher = jobLauncher;
        this.cuentaAnualJob = cuentaAnualJob;
        this.interesJob = interesJob;
        this.transaccionJob = transaccionJob;
    }

    @GetMapping("/run/cuenta-anual")
    public String runCuentaAnualJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(cuentaAnualJob, params);
            return "Job de Cuenta Anual ejecutado exitosamente. Revisa la consola.";
        } catch (Exception e) {
            return "Error al ejecutar el Job de Cuenta Anual: " + e.getMessage();
        }
    }

    @GetMapping("/run/interes")
    public String runInteresJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(interesJob, params);
            return "Job de Intereses ejecutado exitosamente. Revisa la consola.";
        } catch (Exception e) {
            return "Error al ejecutar el Job de Intereses: " + e.getMessage();
        }
    }

    @GetMapping("/run/transaccion")
    public String runTransaccionJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(transaccionJob, params);
            return "Job de Transacciones ejecutado exitosamente. Revisa la consola.";
        } catch (Exception e) {
            return "Error al ejecutar el Job de Transacciones: " + e.getMessage();
        }
    }
}