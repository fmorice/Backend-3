package com.duoc.migracion.config;

import com.duoc.migracion.exception.CuentaNoEncontradaException;
import com.duoc.migracion.exception.InvalidCsvRecordException;
import com.duoc.migracion.exception.InvalidMontoException;
import com.duoc.migracion.exception.RegistroMalClasificadoException;
import com.duoc.migracion.listener.BancoJobListener;
import com.duoc.migracion.listener.BatchSkipListener;
import com.duoc.migracion.model.Interes;
import com.duoc.migracion.processor.InteresProcessor;
import com.duoc.migracion.reader.InteresReader;
import com.duoc.migracion.repository.InteresRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class InteresBatchConfig {

    private final InteresReader interesReader;

    public InteresBatchConfig(InteresReader interesReader) {
        this.interesReader = interesReader;
    }

    @Bean
    public RepositoryItemWriter<Interes> interesWriter(InteresRepository repository) {
        RepositoryItemWriter<Interes> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step interesStep(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager,
                            RepositoryItemWriter<Interes> interesWriter,
                            ThreadPoolTaskExecutor batchTaskExecutor,
                            SynchronizedItemStreamReader<Interes> itemReaderInteres,
                            InteresProcessor interesProcessor,
                            BancoJobListener bancoJobListener,
                            BatchSkipListener batchSkipListener) {
        return new StepBuilder("interesStep", jobRepository)
                .<Interes, Interes>chunk(5, transactionManager)
                .reader(itemReaderInteres)
                .processor(interesProcessor)
                .writer(interesWriter)
                .taskExecutor(batchTaskExecutor)
                .faultTolerant()
                .skip(InvalidCsvRecordException.class)
                .skip(InvalidMontoException.class)
                .skip(RegistroMalClasificadoException.class)
                .skip(CuentaNoEncontradaException.class)
                .skipLimit(10)
                .retry(CuentaNoEncontradaException.class)
                .retryLimit(2)
                .listener(bancoJobListener)
                .listener(batchSkipListener)
                .build();
    }

    @Bean("interesJob")
    public Job interesJob(JobRepository jobRepository, Step interesStep, BancoJobListener bancoJobListener) {
        return new JobBuilder("interesJob", jobRepository)
                .start(interesStep)
                .listener(bancoJobListener)
                .build();
    }
}