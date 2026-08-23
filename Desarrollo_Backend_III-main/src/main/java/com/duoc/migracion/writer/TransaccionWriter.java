package com.duoc.migracion.writer;

import com.duoc.migracion.model.Transaccion;
import com.duoc.migracion.repository.TransaccionRepository;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransaccionWriter {

    @Bean
    public RepositoryItemWriter<Transaccion> itemWriterTransaccion(TransaccionRepository repository) {
        return new RepositoryItemWriterBuilder<Transaccion>()
                .repository(repository)
                .methodName("save")
                .build();
    }
}