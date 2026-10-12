package cl.duoc.eft.s9.batch;

import cl.duoc.eft.s9.batch.movimientos.MovimientosBatchConfiguration;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableBatchProcessing
public class BatchJobsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchJobsApplication.class, args);
    }

    @Bean
    public ApplicationRunner reportDiarioRunner(
            JobLauncher jobLauncher,
            @Qualifier("movimientosReporteDiarioJob") Job movimientosReporteDiarioJob,
            @Value("${batch.movimientos.input-path:./data/semana_3/movimientos_financieros_diarios.csv}") String inputPath,
            @Value("${batch.movimientos.output-dir:./batch/batch-jobs/output}") String outputDir) {
        String resolvedInputPath = resolveInputOrOutputPath(inputPath);
        String resolvedOutputDir = resolveInputOrOutputPath(outputDir);
        return args -> jobLauncher.run(
                movimientosReporteDiarioJob,
                new JobParametersBuilder()
                        .addString("inputPath", resolvedInputPath)
                        .addString("outputDir", resolvedOutputDir)
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters()
        );
    }

    private String resolveInputOrOutputPath(String pathValue) {
        if (pathValue == null || pathValue.isBlank()) {
            return pathValue;
        }
        Path candidate = Paths.get(pathValue);
        if (candidate.isAbsolute()) {
            return candidate.normalize().toString();
        }

        Path root = findProjectRoot(Paths.get("").toAbsolutePath().normalize());
        if (root != null) {
            Path resolved = root.resolve(candidate).normalize();
            if (Files.exists(resolved) || candidate.startsWith("data/") || candidate.startsWith("batch/") || candidate.startsWith("./batch/") || candidate.startsWith("./data/")) {
                return resolved.toString();
            }
        }

        Path currentDir = Paths.get("").toAbsolutePath().normalize();
        Path direct = currentDir.resolve(candidate).normalize();
        if (Files.exists(direct)) {
            return direct.toString();
        }
        Path parentDir = currentDir.getParent();
        while (parentDir != null) {
            Path parentCandidate = parentDir.resolve(candidate).normalize();
            if (Files.exists(parentCandidate)) {
                return parentCandidate.toString();
            }
            parentDir = parentDir.getParent();
        }
        return direct.toString();
    }

    private Path findProjectRoot(Path start) {
        Path current = start;
        Path projectRoot = null;
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml"))) {
                boolean looksLikeRepoRoot = Files.exists(current.resolve("data"))
                        || Files.exists(current.resolve("batch"))
                        || Files.exists(current.resolve("services"))
                        || Files.exists(current.resolve("platform"))
                        || Files.exists(current.resolve("bff"));
                if (looksLikeRepoRoot) {
                    projectRoot = current;
                }
            }
            if (projectRoot != null && current.getParent() == null) {
                return projectRoot;
            }
            current = current.getParent();
        }
        return projectRoot;
    }
}
