package com.duoc.migracion.reader;

import com.duoc.migracion.dto.TransaccionCsvDto;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class TransaccionReader {

    @Bean
    public SynchronizedItemStreamReader<TransaccionCsvDto> itemReaderTransaccion() {
        FlatFileItemReader<TransaccionCsvDto> delegate = new FlatFileItemReaderBuilder<TransaccionCsvDto>()
                .name("transaccionItemReader")
                .resource(new ClassPathResource("data/transacciones.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .fieldSetMapper(fieldSet -> {
                    TransaccionCsvDto dto = new TransaccionCsvDto();
                    dto.setId(fieldSet.readLong("id"));
                    dto.setFecha(fieldSet.readString("fecha"));
                    dto.setMonto(fieldSet.readBigDecimal("monto"));
                    dto.setTipo(fieldSet.readString("tipo"));
                    return dto;
                })
                .build();

        SynchronizedItemStreamReader<TransaccionCsvDto> reader = new SynchronizedItemStreamReader<>();
        reader.setDelegate(delegate);
        return reader;
    }

    @Bean(name = "transaccionPartitionedItemReader")
    @StepScope
    public FlatFileItemReader<TransaccionCsvDto> transaccionPartitionedItemReader(
            @Value("#{stepExecutionContext['fileName']}") String filename) {

        FlatFileItemReader<TransaccionCsvDto> reader = new FlatFileItemReaderBuilder<TransaccionCsvDto>()
                .name("transaccionPartitionedItemReader")
                .resource(new FileSystemResource(filename))
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .fieldSetMapper(fieldSet -> {
                    TransaccionCsvDto dto = new TransaccionCsvDto();
                    dto.setId(fieldSet.readLong("id"));
                    dto.setFecha(fieldSet.readString("fecha"));
                    dto.setMonto(fieldSet.readBigDecimal("monto"));
                    dto.setTipo(fieldSet.readString("tipo"));
                    return dto;
                })
                .build();

        return reader;
    }
}