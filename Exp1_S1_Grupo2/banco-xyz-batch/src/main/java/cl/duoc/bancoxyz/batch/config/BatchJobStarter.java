package cl.duoc.bancoxyz.batch.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class BatchJobStarter implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BatchJobStarter.class);

    private final JobLauncher jobLauncher;
    private final Job reporteTransaccionesJob;
    private final Job calculoInteresesJob;
    private final Job estadosCuentaAnualesJob;

    public BatchJobStarter(JobLauncher jobLauncher, Job reporteTransaccionesJob, Job calculoInteresesJob, Job estadosCuentaAnualesJob) {
        this.jobLauncher = jobLauncher;
        this.reporteTransaccionesJob = reporteTransaccionesJob;
        this.calculoInteresesJob = calculoInteresesJob;
        this.estadosCuentaAnualesJob = estadosCuentaAnualesJob;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JobParametersBuilder params = new JobParametersBuilder();
        params.addLong("run.id", System.currentTimeMillis());

        log.info("Lanzando Job: reporteTransaccionesJob");
        JobExecution exec1 = jobLauncher.run(reporteTransaccionesJob, params.toJobParameters());
        log.info("reporteTransaccionesJob status={}", exec1.getStatus());

        // nuevo run id para el segundo job
        JobParametersBuilder params2 = new JobParametersBuilder();
        params2.addLong("run.id", System.currentTimeMillis());

        log.info("Lanzando Job: calculoInteresesJob");
        JobExecution exec2 = jobLauncher.run(calculoInteresesJob, params2.toJobParameters());
        log.info("calculoInteresesJob status={}", exec2.getStatus());

        JobParametersBuilder params3 = new JobParametersBuilder();
        params3.addLong("run.id", System.currentTimeMillis());

        log.info("Lanzando Job: estadosCuentaAnualesJob");
        JobExecution exec3 = jobLauncher.run(estadosCuentaAnualesJob, params3.toJobParameters());
        log.info("estadosCuentaAnualesJob status={}", exec3.getStatus());
    }
}
