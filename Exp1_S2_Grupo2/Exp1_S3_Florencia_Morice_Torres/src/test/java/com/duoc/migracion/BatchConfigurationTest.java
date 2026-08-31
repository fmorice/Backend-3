package com.duoc.migracion;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BatchConfigurationTest {

    @Autowired
    private ThreadPoolTaskExecutor batchTaskExecutor;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("transaccionJob")
    private Job transaccionJob;

    @Autowired
    @Qualifier("interesJob")
    private Job interesJob;

    @Autowired
    @Qualifier("cuentaAnualJob")
    private Job cuentaAnualJob;

    @Test
    void batchExecutorMustUseThreeThreadsAndExpectedNaming() {
        assertThat(batchTaskExecutor).isNotNull();
        assertThat(batchTaskExecutor.getCorePoolSize()).isEqualTo(3);
        assertThat(batchTaskExecutor.getMaxPoolSize()).isEqualTo(3);
        assertThat(batchTaskExecutor.getThreadNamePrefix()).isEqualTo("batch-thread-");
    }

    @Test
    void threeJobsShouldExecuteSuccessfully() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("runId", System.currentTimeMillis())
                .toJobParameters();

        JobExecution transaccionExecution = jobLauncher.run(transaccionJob, params);
        JobExecution interesExecution = jobLauncher.run(interesJob, params);
        JobExecution cuentaAnualExecution = jobLauncher.run(cuentaAnualJob, params);

        assertThat(transaccionExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(interesExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(cuentaAnualExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}
