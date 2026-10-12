package cl.duoc.eft.s9.batch.movimientos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MovimientosBatchConfiguration {

    @Bean
    public RejectedMovimientoTracker rejectedMovimientoTracker() {
        return new RejectedMovimientoTracker();
    }

        @Bean
            @StepScope
            public FlatFileItemReader<MovimientoDiarioCsv> movimientosReader(
                @Value("#{jobParameters['inputPath'] != null ? jobParameters['inputPath'] : '${batch.movimientos.input-path:./data/semana_3/movimientos_financieros_diarios.csv}'}") String inputPath
    ) {
        FlatFileItemReader<MovimientoDiarioCsv> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputPath));
        reader.setLinesToSkip(1);
        reader.setName("movimientosReader");

        DefaultLineMapper<MovimientoDiarioCsv> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id", "fecha", "monto", "tipo");
        tokenizer.setStrict(false);
        lineMapper.setLineTokenizer(tokenizer);

        BeanWrapperFieldSetMapper<MovimientoDiarioCsv> fieldMapper = new BeanWrapperFieldSetMapper<>();
        fieldMapper.setTargetType(MovimientoDiarioCsv.class);
        lineMapper.setFieldSetMapper(fieldMapper);
        reader.setLineMapper(lineMapper);
        return reader;
    }

    @Bean
    public MovementReadListener movementReadListener() {
        return new MovementReadListener();
    }

    @Bean
    public ItemProcessor<MovimientoDiarioCsv, MovimientoDiarioReporte> movimientosProcessor() {
        MovimientoDiarioValidator validator = new MovimientoDiarioValidator();
        return item -> {
            MovimientoDiarioValidator.ValidationResult result = validator.validate(item);
            if (!result.valid()) {
                throw new ValidationException(String.join("; ", result.reasons()));
            }
            String estado = result.pendingBusinessDecision() ? "PENDIENTE_NEGOCIO" : "VALIDADO_TECNICAMENTE";
            MovimientoDiarioReporte reporte = new MovimientoDiarioReporte(item.getId(), item.getFecha(), item.getMonto(), item.getTipo(), estado);
            reporte.setNumeroFila(item.getNumeroFila());
            return reporte;
        };
    }

        @Bean
            @StepScope
    public FlatFileItemWriter<MovimientoDiarioReporte> movimientosWriter(
                @Value("#{jobParameters['outputDir'] != null ? jobParameters['outputDir'] : '${batch.movimientos.output-dir:./batch/batch-jobs/output}'}") String outputDir
    ) {
        Path outputPath = Paths.get(outputDir, "movimientos_diarios_validos.csv");
        try {
            Files.createDirectories(outputPath.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el directorio de salida", e);
        }

        FlatFileItemWriter<MovimientoDiarioReporte> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputPath.toFile()));
        writer.setName("movimientosWriter");
        writer.setHeaderCallback(header -> header.write("id,fecha,monto,tipo,estado"));

        BeanWrapperFieldExtractor<MovimientoDiarioReporte> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[] {"id", "fecha", "monto", "tipo", "estado"});

        DelimitedLineAggregator<MovimientoDiarioReporte> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(extractor);
        writer.setLineAggregator(aggregator);
        return writer;
    }

    @Bean
    public SkipListener<MovimientoDiarioCsv, MovimientoDiarioReporte> movimientosSkipListener(RejectedMovimientoTracker tracker) {
        return new SkipListener<>() {
            @Override
            public void onSkipInProcess(MovimientoDiarioCsv item, Throwable t) {
                if (item != null) {
                    tracker.add(new RejectedMovimiento(item.getNumeroFila(), t.getMessage(), item.getId(), item.getFecha(), item.getMonto(), item.getTipo()));
                }
            }

            @Override
            public void onSkipInRead(Throwable throwable) {
                long numeroFila = throwable instanceof FlatFileParseException parseException
                        ? parseException.getLineNumber()
                        : 0L;
                tracker.add(new RejectedMovimiento(numeroFila, "error_lectura: " + errorMessage(throwable), "", "", "", ""));
            }

            @Override
            public void onSkipInWrite(MovimientoDiarioReporte item, Throwable throwable) {
                if (item != null) {
                    tracker.add(new RejectedMovimiento(item.getNumeroFila(), "error_escritura: " + errorMessage(throwable),
                            item.getId(), item.getFecha(), item.getMonto(), item.getTipo()));
                }
            }
        };
    }

    @Bean
    public Job movimientosReporteDiarioJob(JobRepository jobRepository,
                                          Step movimientosDiarioStep,
                                          JobExecutionListener movimientosJobListener) {
        return new JobBuilder("movimientosReporteDiarioJob", jobRepository)
                .listener(movimientosJobListener)
                .start(movimientosDiarioStep)
                .build();
    }

    @Bean
    public Step movimientosDiarioStep(JobRepository jobRepository,
                                     ItemReader<MovimientoDiarioCsv> reader,
                                     ItemProcessor<MovimientoDiarioCsv, MovimientoDiarioReporte> processor,
                                     ItemWriter<MovimientoDiarioReporte> writer,
                                     SkipListener<MovimientoDiarioCsv, MovimientoDiarioReporte> skipListener,
                         ItemReadListener<MovimientoDiarioCsv> readListener,
                         PlatformTransactionManager transactionManager) {
            return new StepBuilder("movimientosDiarioStep", jobRepository)
                .<MovimientoDiarioCsv, MovimientoDiarioReporte>chunk(50, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(ValidationException.class)
                .skipLimit(Integer.MAX_VALUE)
                .listener(skipListener)
                .listener(readListener)
                .build();
    }

    @Bean
    public JobExecutionListener movimientosJobListener(
            @Value("${batch.movimientos.output-dir:./batch/batch-jobs/output}") String defaultOutputDir,
            RejectedMovimientoTracker rejectedMovimientoTracker) {
        return new JobExecutionListener() {
            @Override
            public void afterJob(JobExecution jobExecution) {
                String outputDir = jobExecution.getJobParameters().getString("outputDir");
                if (outputDir == null || outputDir.isBlank()) {
                    outputDir = defaultOutputDir;
                }
                Path outputPath = Paths.get(outputDir);
                try {
                    Files.createDirectories(outputPath);

                    Path summaryPath = outputPath.resolve("movimientos_diarios_resumen.txt");
                    Path rejectedPath = outputPath.resolve("movimientos_diarios_rechazados.csv");
                    Path validPath = outputPath.resolve("movimientos_diarios_validos.csv");

                    long pendingBusinessCount = 0L;
                    MovimientoBatchCounters counters = new MovimientoBatchCounters(0, 0, 0, 0, 0, 0, 0, 0);

                    if (!jobExecution.getStepExecutions().isEmpty()) {
                        var stepExecution = jobExecution.getStepExecutions().iterator().next();
                        if (Files.exists(validPath)) {
                            List<String> validLines = Files.readAllLines(validPath, StandardCharsets.UTF_8);
                            pendingBusinessCount = validLines.stream()
                                    .skip(1)
                                    .filter(line -> line.endsWith(",PENDIENTE_NEGOCIO"))
                                    .count();
                        }
                        counters = MovimientoBatchCounters.from(
                                stepExecution.getReadCount(),
                                stepExecution.getReadSkipCount(),
                                stepExecution.getWriteCount(),
                                stepExecution.getProcessSkipCount(),
                                stepExecution.getWriteSkipCount(),
                                pendingBusinessCount);
                    }

                    List<String> summary = new ArrayList<>();
                    summary.add("job=movimientosReporteDiarioJob");
                    summary.add("archivo_entrada=" + jobExecution.getJobParameters().getString("inputPath"));
                    summary.add("archivo_validos=" + validPath.toString());
                    summary.add("archivo_rechazados=" + rejectedPath.toString());
                    summary.add("registros_leidos=" + counters.registrosLeidos());
                    summary.add("registros_escritos_despues_validacion_tecnica=" + counters.registrosEscritos());
                    summary.add("registros_pendientes_decision_negocio=" + counters.registrosPendientesNegocio());
                    summary.add("registros_aceptados_reglas_negocio_confirmadas=" + counters.registrosAceptadosNegocio());
                    summary.add("registros_sin_decision_negocio=" + counters.registrosSinDecisionNegocio());
                    summary.add("registros_rechazados=" + counters.registrosRechazados());
                    summary.add("errores_lectura=" + counters.erroresLectura());
                    summary.add("errores_escritura=" + counters.erroresEscritura());
                    summary.add("conciliacion_ok=" + counters.estaConciliado());
                    summary.add("conciliacion=leidos=escritos+rechazados+errores_lectura+errores_escritura");
                    summary.add("regla_tecnica=campos obligatorios, fecha valida, monto numerico y tipo reconocido");
                    summary.add("reglas_negocio_confirmadas=ninguna; ningun registro se marca como aceptado por negocio");
                    summary.add("regla_pendiente_de_confirmacion=montos <= 0 se mantienen pendientes; no se aceptan ni rechazan automaticamente");
                    summary.add("lista_tipos_admitidos_pendiente_de_confirmacion=credito,debito,deposito,retiro,compra,pago");
                    Files.write(summaryPath, summary, StandardCharsets.UTF_8);

                    StringBuilder rejectedBuilder = new StringBuilder();
                    rejectedBuilder.append("numero_fila,motivo,id,fecha,monto,tipo\n");
                    for (RejectedMovimiento rejected : rejectedMovimientoTracker.getRejected()) {
                        rejectedBuilder.append(rejected.getNumeroFila())
                                .append(',').append(csvValue(rejected.getMotivo()))
                                .append(',').append(csvValue(rejected.getId()))
                                .append(',').append(csvValue(rejected.getFecha()))
                                .append(',').append(csvValue(rejected.getMonto()))
                                .append(',').append(csvValue(rejected.getTipo()))
                                .append('\n');
                    }
                    Files.write(rejectedPath, rejectedBuilder.toString().getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new IllegalStateException("No se pudo escribir el resumen o los rechazos", e);
                }
            }
        };
    }

    private static String errorMessage(Throwable throwable) {
        if (throwable == null) {
            return "error sin detalle";
        }
        String message = throwable.getMessage();
        return message == null || message.isBlank() ? throwable.getClass().getSimpleName() : message;
    }

    private static String csvValue(String value) {
        String safeValue = value == null ? "" : value;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    public static class RejectedMovimientoTracker {
        private final List<RejectedMovimiento> rejected = new ArrayList<>();

        public void add(RejectedMovimiento movimiento) {
            rejected.add(movimiento);
        }

        public List<RejectedMovimiento> getRejected() {
            return rejected;
        }
    }

    public static class MovementReadListener implements ItemReadListener<MovimientoDiarioCsv> {
        private final AtomicLong currentRow = new AtomicLong(2L);

        @Override
        public void beforeRead() {
            // no-op
        }

        @Override
        public void afterRead(MovimientoDiarioCsv item) {
            if (item != null) {
                item.setNumeroFila(currentRow.getAndIncrement());
            }
        }

        @Override
        public void onReadError(Exception ex) {
            // no-op
        }
    }
}
