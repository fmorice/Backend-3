package com.duoc.migracion.reader;

import com.duoc.migracion.model.Interes;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InteresReader {

    @Bean
    public SynchronizedItemStreamReader<Interes> itemReaderInteres() {
        FlatFileItemReader<Interes> delegate = new FlatFileItemReaderBuilder<Interes>()
                .name("interesItemReader")
                .resource(new ClassPathResource("data/intereses.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .fieldSetMapper(fieldSet -> {
                    Interes interes = new Interes();
                    interes.setCuentaId(fieldSet.readLong("cuentaId"));
                    interes.setNombre(fieldSet.readString("nombre"));
                    interes.setSaldo(fieldSet.readBigDecimal("saldo"));
                    interes.setEdad(fieldSet.readInt("edad"));
                    interes.setTipo(fieldSet.readString("tipo"));
                    return interes;
                })
                .build();

        SynchronizedItemStreamReader<Interes> reader = new SynchronizedItemStreamReader<>();
        reader.setDelegate(delegate);
        return reader;
    }

    @Bean(name = "interesPartitionedItemReader")
    @StepScope
    public FlatFileItemReader<Interes> interesPartitionedItemReader(@Value("#{stepExecutionContext['fileName']}") String filename) {
        FlatFileItemReader<Interes> reader = new FlatFileItemReaderBuilder<Interes>()
                .name("interesPartitionedItemReader")
                .resource(new FileSystemResource(filename))
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .fieldSetMapper(fieldSet -> {
                    Interes interes = new Interes();
                    interes.setCuentaId(fieldSet.readLong("cuentaId"));
                    interes.setNombre(fieldSet.readString("nombre"));
                    interes.setSaldo(fieldSet.readBigDecimal("saldo"));
                    interes.setEdad(fieldSet.readInt("edad"));
                    interes.setTipo(fieldSet.readString("tipo"));
                    return interes;
                })
                .build();

        return reader;
    }
}