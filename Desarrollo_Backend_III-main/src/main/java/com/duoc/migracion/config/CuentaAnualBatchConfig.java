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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CuentaAnualBatchConfig {

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

    @Bean("cuentaAnualJob")
    public Job cuentaAnualJob(JobRepository jobRepository, Step cuentaAnualStep, BancoJobListener bancoJobListener) {
        return new JobBuilder("cuentaAnualJob", jobRepository)
                .start(cuentaAnualStep)
                .listener(bancoJobListener)
                .build();
    }
}