package com.duoc.migracion.config;

import com.duoc.migracion.dto.TransaccionCsvDto;
import com.duoc.migracion.exception.InvalidCsvRecordException;
import com.duoc.migracion.exception.InvalidDateException;
import com.duoc.migracion.exception.InvalidMontoException;
import com.duoc.migracion.exception.RegistroMalClasificadoException;
import com.duoc.migracion.listener.BancoJobListener;
import com.duoc.migracion.listener.BatchSkipListener;
import com.duoc.migracion.model.Transaccion;
import com.duoc.migracion.processor.TransaccionProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransaccionBatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(TransaccionBatchConfig.class);

    @Bean
    public Step transaccionStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                SynchronizedItemStreamReader<TransaccionCsvDto> itemReaderTransaccion,
                                TransaccionProcessor processor,
                                RepositoryItemWriter<Transaccion> itemWriterTransaccion,
                                ThreadPoolTaskExecutor batchTaskExecutor,
                                BancoJobListener bancoJobListener,
                                BatchSkipListener batchSkipListener) {
        // legacy single-step (kept for compatibility). Prefer master partitioned step.
        return new StepBuilder("transaccionStep", jobRepository)
                .<TransaccionCsvDto, Transaccion>chunk(5, transactionManager)
                .reader(itemReaderTransaccion)
                .processor(processor)
                .writer(itemWriterTransaccion)
                .taskExecutor(batchTaskExecutor)
                .faultTolerant()
                .skip(InvalidCsvRecordException.class)
                .skip(InvalidMontoException.class)
                .skip(InvalidDateException.class)
                .skip(RegistroMalClasificadoException.class)
                .skipLimit(10)
                .retry(IllegalArgumentException.class)
                .retryLimit(2)
                .listener(bancoJobListener)
                .listener(batchSkipListener)
                .build();
    }

    @Bean
    public Partitioner transaccionPartitioner() {
        return new TransaccionPartitioner();
    }

    @Bean
    public Step transaccionWorkerStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      FlatFileItemReader<TransaccionCsvDto> transaccionPartitionedItemReader,
                                      TransaccionProcessor processor,
                                      RepositoryItemWriter<Transaccion> itemWriterTransaccion,
                                      BancoJobListener bancoJobListener,
                                      BatchSkipListener batchSkipListener) {

        return new StepBuilder("transaccionWorkerStep", jobRepository)
                .<TransaccionCsvDto, Transaccion>chunk(5, transactionManager)
                .reader(transaccionPartitionedItemReader)
                .processor(processor)
                .writer(itemWriterTransaccion)
                .faultTolerant()
                .skip(InvalidCsvRecordException.class)
                .skip(InvalidMontoException.class)
                .skip(InvalidDateException.class)
                .skip(RegistroMalClasificadoException.class)
                .skipLimit(10)
                .retry(IllegalArgumentException.class)
                .retryLimit(2)
                .listener(bancoJobListener)
                .listener(batchSkipListener)
                .build();
    }

    @Bean
    public TaskExecutorPartitionHandler transaccionPartitionHandler(ThreadPoolTaskExecutor batchTaskExecutor,
                                                                    Step transaccionWorkerStep,
                                                                    @Value("${batch.transaccion.partitions:3}") int gridSize) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(batchTaskExecutor);
        handler.setStep(transaccionWorkerStep);
        handler.setGridSize(gridSize);
        logger.info("Configured TaskExecutorPartitionHandler with gridSize={}", gridSize);
        return handler;
    }

    @Bean
    public Step transaccionMasterStep(JobRepository jobRepository,
                                      Partitioner transaccionPartitioner,
                                      TaskExecutorPartitionHandler transaccionPartitionHandler) {
        logger.info("Creating master step for transaccion with partitioner");
        return new StepBuilder("transaccionMasterStep", jobRepository)
                .partitioner("transaccionWorkerStep", transaccionPartitioner)
                .partitionHandler(transaccionPartitionHandler)
                .build();
    }

    @Bean
    public Job transaccionJob(JobRepository jobRepository, Step transaccionMasterStep, BancoJobListener bancoJobListener) {
        logger.info("Building transaccionJob to start master partitioned step");
        return new JobBuilder("transaccionJob", jobRepository)
                .start(transaccionMasterStep)
                .listener(bancoJobListener)
                .build();
    }
}