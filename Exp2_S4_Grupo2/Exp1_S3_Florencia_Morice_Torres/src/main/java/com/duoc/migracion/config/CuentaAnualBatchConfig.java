package com.duoc.migracion.config;

import com.duoc.migracion.dto.CuentaAnualCsvDto;
import com.duoc.migracion.exception.InvalidCsvRecordException;
import com.duoc.migracion.exception.InvalidDateException;
import com.duoc.migracion.exception.InvalidMontoException;
import com.duoc.migracion.exception.RegistroMalClasificadoException;
import com.duoc.migracion.listener.BancoJobListener;
import com.duoc.migracion.listener.BatchSkipListener;
import com.duoc.migracion.model.CuentaAnual;
import com.duoc.migracion.processor.CuentaAnualProcessor;
import com.duoc.migracion.reader.CuentaAnualReader;
import com.duoc.migracion.writer.CuentaAnualWriter;
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
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CuentaAnualBatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(CuentaAnualBatchConfig.class);

    @Bean
    public Step cuentaAnualStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                SynchronizedItemStreamReader<com.duoc.migracion.dto.CuentaAnualCsvDto> cuentaAnualReader,
                                CuentaAnualProcessor cuentaAnualProcessor,
                                CuentaAnualWriter cuentaAnualWriter,
                                ThreadPoolTaskExecutor batchTaskExecutor,
                                BancoJobListener bancoJobListener,
                                BatchSkipListener batchSkipListener) {
        return new StepBuilder("cuentaAnualStep", jobRepository)
                .<CuentaAnualCsvDto, CuentaAnual>chunk(5, transactionManager)
                .reader(cuentaAnualReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualWriter)
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
    public Partitioner cuentaAnualPartitioner() {
        return new CuentaAnualPartitioner();
    }

    @Bean
    public Step cuentaAnualWorkerStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      FlatFileItemReader<com.duoc.migracion.dto.CuentaAnualCsvDto> cuentaAnualPartitionedItemReader,
                                      CuentaAnualProcessor cuentaAnualProcessor,
                                      CuentaAnualWriter cuentaAnualWriter,
                                      BancoJobListener bancoJobListener,
                                      BatchSkipListener batchSkipListener) {
        return new StepBuilder("cuentaAnualWorkerStep", jobRepository)
                .<CuentaAnualCsvDto, CuentaAnual>chunk(5, transactionManager)
                .reader(cuentaAnualPartitionedItemReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualWriter)
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
    public TaskExecutorPartitionHandler cuentaAnualPartitionHandler(ThreadPoolTaskExecutor batchTaskExecutor,
                                                                    Step cuentaAnualWorkerStep,
                                                                    @Value("${batch.cuentaAnual.partitions:3}") int gridSize) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(batchTaskExecutor);
        handler.setStep(cuentaAnualWorkerStep);
        handler.setGridSize(gridSize);
        logger.info("Configured CuentaAnual TaskExecutorPartitionHandler gridSize={}", gridSize);
        return handler;
    }

    @Bean
    public Step cuentaAnualMasterStep(JobRepository jobRepository,
                                      Partitioner cuentaAnualPartitioner,
                                      TaskExecutorPartitionHandler cuentaAnualPartitionHandler) {
        logger.info("Creating master step for cuentaAnual with partitioner");
        return new StepBuilder("cuentaAnualMasterStep", jobRepository)
                .partitioner("cuentaAnualWorkerStep", cuentaAnualPartitioner)
                .partitionHandler(cuentaAnualPartitionHandler)
                .build();
    }

    @Bean("cuentaAnualJob")
    public Job cuentaAnualJob(JobRepository jobRepository, Step cuentaAnualMasterStep, BancoJobListener bancoJobListener) {
        return new JobBuilder("cuentaAnualJob", jobRepository)
                .start(cuentaAnualMasterStep)
                .listener(bancoJobListener)
                .build();
    }
}