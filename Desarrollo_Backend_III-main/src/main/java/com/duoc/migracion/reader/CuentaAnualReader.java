package com.duoc.migracion.reader;

import com.duoc.migracion.dto.CuentaAnualCsvDto;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class CuentaAnualReader extends FlatFileItemReader<CuentaAnualCsvDto> {

    public CuentaAnualReader() {
        setName("cuentaAnualReader");
        setResource(new ClassPathResource("data/cuentas_anuales.csv"));
        setLinesToSkip(1);

        DefaultLineMapper<CuentaAnualCsvDto> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("cuentaId", "fecha", "transaccion", "monto", "descripcion");

        BeanWrapperFieldSetMapper<CuentaAnualCsvDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CuentaAnualCsvDto.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        setLineMapper(lineMapper);
    }

    @Bean
    public SynchronizedItemStreamReader<CuentaAnualCsvDto> cuentaAnualItemReader() {
        SynchronizedItemStreamReader<CuentaAnualCsvDto> reader = new SynchronizedItemStreamReader<>();
        reader.setDelegate(this);
        return reader;
    }
}