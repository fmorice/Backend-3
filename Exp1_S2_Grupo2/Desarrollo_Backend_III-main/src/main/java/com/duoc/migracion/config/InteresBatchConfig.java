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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger logger = LoggerFactory.getLogger(InteresBatchConfig.class);

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

    @Bean
    public Partitioner interesPartitioner() {
        return new InteresPartitioner();
    }

    @Bean
    public Step interesWorkerStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  FlatFileItemReader<com.duoc.migracion.model.Interes> interesPartitionedItemReader,
                                  InteresProcessor interesProcessor,
                                  RepositoryItemWriter<Interes> interesWriter,
                                  BancoJobListener bancoJobListener,
                                  BatchSkipListener batchSkipListener) {
        return new StepBuilder("interesWorkerStep", jobRepository)
                .<Interes, Interes>chunk(5, transactionManager)
                .reader(interesPartitionedItemReader)
                .processor(interesProcessor)
                .writer(interesWriter)
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

    @Bean
    public TaskExecutorPartitionHandler interesPartitionHandler(ThreadPoolTaskExecutor batchTaskExecutor,
                                                                Step interesWorkerStep,
                                                                @Value("${batch.interes.partitions:3}") int gridSize) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(batchTaskExecutor);
        handler.setStep(interesWorkerStep);
        handler.setGridSize(gridSize);
        logger.info("Configured Interes TaskExecutorPartitionHandler gridSize={}", gridSize);
        return handler;
    }

    @Bean
    public Step interesMasterStep(JobRepository jobRepository,
                                  Partitioner interesPartitioner,
                                  TaskExecutorPartitionHandler interesPartitionHandler) {
        logger.info("Creating master step for interes with partitioner");
        return new StepBuilder("interesMasterStep", jobRepository)
                .partitioner("interesWorkerStep", interesPartitioner)
                .partitionHandler(interesPartitionHandler)
                .build();
    }

    @Bean("interesJob")
    public Job interesJob(JobRepository jobRepository, Step interesMasterStep, BancoJobListener bancoJobListener) {
        return new JobBuilder("interesJob", jobRepository)
                .start(interesMasterStep)
                .listener(bancoJobListener)
                .build();
    }
}