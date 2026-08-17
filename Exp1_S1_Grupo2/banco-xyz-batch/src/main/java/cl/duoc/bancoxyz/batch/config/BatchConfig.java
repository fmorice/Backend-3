package cl.duoc.bancoxyz.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import cl.duoc.bancoxyz.batch.model.Transaccion;
import cl.duoc.bancoxyz.batch.processor.TransaccionProcessor;
import cl.duoc.bancoxyz.batch.model.Interes;
import cl.duoc.bancoxyz.batch.processor.InteresProcessor;
import cl.duoc.bancoxyz.batch.model.CuentaAnual;
import cl.duoc.bancoxyz.batch.processor.CuentaAnualProcessor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    @Bean
    public FlatFileItemReader<Transaccion> transaccionReader() {
        FlatFileItemReader<Transaccion> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data/transacciones.csv"));
        reader.setLinesToSkip(1); // saltar header

        DefaultLineMapper<Transaccion> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[]{"cuenta_id","fecha","transaccion","monto","descripcion"});

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new FieldSetMapper<Transaccion>() {
            @Override
            public Transaccion mapFieldSet(FieldSet fs) {
                Transaccion t = new Transaccion();
                try {
                    t.setCuentaId(fs.readInt("cuenta_id"));
                } catch (Exception ex) {
                    t.setCuentaId(null);
                }
                String fechaStr = fs.readString("fecha");
                if (fechaStr != null && !fechaStr.isBlank()) {
                    t.setFecha(LocalDate.parse(fechaStr, DateTimeFormatter.ISO_DATE));
                }
                t.setTransaccion(fs.readString("transaccion"));
                try {
                    t.setMonto(new BigDecimal(fs.readString("monto")));
                } catch (Exception ex) {
                    t.setMonto(null);
                }
                t.setDescripcion(fs.readString("descripcion"));
                return t;
            }
        });
        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public TransaccionProcessor transaccionProcessor() {
        return new TransaccionProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Transaccion> transaccionWriter(DataSource dataSource) {
        JdbcBatchItemWriter<Transaccion> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO transaccion_procesada (cuenta_id, fecha, transaccion, monto, descripcion) VALUES (:cuentaId, :fecha, :transaccion, :monto, :descripcion)");
        writer.setDataSource(dataSource);
        return writer;
    }

    @Bean
    public FlatFileItemReader<Interes> interesesReader() {
        FlatFileItemReader<Interes> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data/intereses.csv"));
        reader.setLinesToSkip(1);

        DefaultLineMapper<Interes> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[]{"cuenta_id","nombre","saldo","edad","tipo"});

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new FieldSetMapper<Interes>() {
            @Override
            public Interes mapFieldSet(FieldSet fs) {
                Interes i = new Interes();
                try {
                    i.setCuentaId(fs.readInt("cuenta_id"));
                } catch (Exception ex) {
                    i.setCuentaId(null);
                }
                i.setNombre(fs.readString("nombre"));
                try {
                    i.setSaldo(new BigDecimal(fs.readString("saldo")));
                } catch (Exception ex) {
                    i.setSaldo(null);
                }
                try {
                    i.setEdad(fs.readInt("edad"));
                } catch (Exception ex) {
                    i.setEdad(null);
                }
                i.setTipo(fs.readString("tipo"));
                return i;
            }
        });
        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public InteresProcessor interesProcessor() {
        return new InteresProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Interes> interesWriter(DataSource dataSource) {
        JdbcBatchItemWriter<Interes> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO interes_procesado (cuenta_id, nombre, saldo, tipo, tasa, interes) VALUES (:cuentaId, :nombre, :saldo, :tipo, :tasa, :interes)");
        writer.setDataSource(dataSource);
        return writer;
    }

    @Bean
    public FlatFileItemReader<CuentaAnual> cuentasAnualesReader() {
        FlatFileItemReader<CuentaAnual> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data/cuentas_anuales.csv"));
        reader.setLinesToSkip(1);

        DefaultLineMapper<CuentaAnual> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(new String[]{"id","fecha","monto","tipo"});

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new FieldSetMapper<CuentaAnual>() {
            @Override
            public CuentaAnual mapFieldSet(FieldSet fs) {
                CuentaAnual c = new CuentaAnual();
                try {
                    c.setId(fs.readInt("id"));
                } catch (Exception ex) {
                    c.setId(null);
                }
                String fechaStr = fs.readString("fecha");
                if (fechaStr != null && !fechaStr.isBlank()) {
                    c.setFecha(LocalDate.parse(fechaStr, DateTimeFormatter.ISO_DATE));
                }
                try {
                    c.setMonto(new BigDecimal(fs.readString("monto")));
                } catch (Exception ex) {
                    c.setMonto(null);
                }
                c.setTipo(fs.readString("tipo"));
                return c;
            }
        });
        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public CuentaAnualProcessor cuentaAnualProcessor() {
        return new CuentaAnualProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter(DataSource dataSource) {
        JdbcBatchItemWriter<CuentaAnual> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO CUENTA_ANUAL_PROCESADA (id, fecha, monto, tipo) VALUES (:id, :fecha, :monto, :tipo)");
        writer.setDataSource(dataSource);
        return writer;
    }

    @Bean
    public Step reporteTransaccionesStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         FlatFileItemReader<Transaccion> transaccionReader,
                                         TransaccionProcessor transaccionProcessor,
                                         JdbcBatchItemWriter<Transaccion> transaccionWriter) {

        return new StepBuilder("reporteTransaccionesStep", jobRepository)
                .<Transaccion, Transaccion>chunk(5, transactionManager)
                .reader(transaccionReader)
                .processor(transaccionProcessor)
                .writer(transaccionWriter)
                .listener(new StepExecutionListenerSupport() {
                    @Override
                    public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
                        log.info("Step summary - readCount={}, writeCount={}, filterCount={}",
                                stepExecution.getReadCount(), stepExecution.getWriteCount(), stepExecution.getFilterCount());
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public Job reporteTransaccionesJob(JobRepository jobRepository, Step reporteTransaccionesStep) {
        return new JobBuilder("reporteTransaccionesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(reporteTransaccionesStep)
                .build();
    }

    @Bean
    public Step calculoInteresesStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     FlatFileItemReader<Interes> interesesReader,
                                     InteresProcessor interesProcessor,
                                     JdbcBatchItemWriter<Interes> interesWriter) {

        return new StepBuilder("calculoInteresesStep", jobRepository)
                .<Interes, Interes>chunk(5, transactionManager)
                .reader(interesesReader)
                .processor(interesProcessor)
                .writer(interesWriter)
                .listener(new StepExecutionListenerSupport() {
                    @Override
                    public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
                        log.info("Intereses Step summary - readCount={}, writeCount={}, filterCount={}",
                                stepExecution.getReadCount(), stepExecution.getWriteCount(), stepExecution.getFilterCount());
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public Job calculoInteresesJob(JobRepository jobRepository, Step calculoInteresesStep) {
        return new JobBuilder("calculoInteresesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(calculoInteresesStep)
                .build();
    }

    @Bean
    public Step estadosCuentaAnualesStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        FlatFileItemReader<CuentaAnual> cuentasAnualesReader,
                                        CuentaAnualProcessor cuentaAnualProcessor,
                                        JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter) {

        return new StepBuilder("estadosCuentaAnualesStep", jobRepository)
                .<CuentaAnual, CuentaAnual>chunk(5, transactionManager)
                .reader(cuentasAnualesReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualWriter)
                .listener(new StepExecutionListenerSupport() {
                    @Override
                    public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
                        log.info("Estados de cuenta Step summary - readCount={}, writeCount={}, filterCount={}",
                                stepExecution.getReadCount(), stepExecution.getWriteCount(), stepExecution.getFilterCount());
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public Job estadosCuentaAnualesJob(JobRepository jobRepository, Step estadosCuentaAnualesStep) {
        return new JobBuilder("estadosCuentaAnualesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(estadosCuentaAnualesStep)
                .build();
    }
}
