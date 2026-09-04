package com.duoc.migracion.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CuentaAnualPartitionerTest {

    private final List<Path> toDelete = new ArrayList<>();

    @AfterEach
    void cleanup() {
        toDelete.forEach(p -> {
            try { Files.deleteIfExists(p); } catch (Exception ignored) {}
        });
        toDelete.clear();
    }

    private int countRecords(String resourcePath) throws Exception {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                new ClassPathResource(resourcePath).getInputStream(), StandardCharsets.UTF_8))) {
            r.readLine();
            int total = 0;
            while (r.readLine() != null) total++;
            return total;
        }
    }

    @Test
    void partitionProducesExpectedPartitionsAndExecutionContexts() throws Exception {
        CuentaAnualPartitioner p = new CuentaAnualPartitioner();
        int gridSize = 3;

        Map<String, ExecutionContext> parts = p.partition(gridSize);
        assertThat(parts).hasSize(gridSize);

        for (ExecutionContext ctx : parts.values()) {
            assertThat(ctx).isNotNull();
            String fileName = ctx.getString("fileName");
            assertThat(fileName).isNotBlank();
            toDelete.add(Path.of(fileName));
            assertThat(ctx.getInt("partitionId")).isNotNull();
            assertThat(ctx.getInt("startIndex")).isNotNull();
            assertThat(ctx.getInt("endIndex")).isNotNull();
        }
    }

    @Test
    void partitionsAreNonOverlappingAndCoverAllRecords_evenWhenRemainderExists() throws Exception {
        String resource = "data/cuentas_anuales.csv";
        int total = countRecords(resource);
        int gridSize = Math.max(2, Math.min(5, total + 1));

        CuentaAnualPartitioner p = new CuentaAnualPartitioner();
        Map<String, ExecutionContext> parts = p.partition(gridSize);

        boolean[] seen = new boolean[Math.max(0, total)];
        int marked = 0;

        for (ExecutionContext ctx : parts.values()) {
            String fileName = ctx.getString("fileName");
            toDelete.add(Path.of(fileName));
            int start = ctx.getInt("startIndex");
            int end = ctx.getInt("endIndex");
            if (start <= end) {
                for (int i = start; i <= end; i++) {
                    assertThat(i).isGreaterThanOrEqualTo(0);
                    assertThat(i).isLessThan(total);
                    assertThat(seen[i]).isFalse();
                    seen[i] = true;
                    marked++;
                }
            }
        }

        assertThat(marked).isEqualTo(total);
    }

    @Test
    void gridSizeGreaterThanTotalProducesSomeEmptyPartitions_butAllRecordsAssigned() throws Exception {
        String resource = "data/cuentas_anuales.csv";
        int total = countRecords(resource);
        int gridSize = total + 4;

        CuentaAnualPartitioner p = new CuentaAnualPartitioner();
        Map<String, ExecutionContext> parts = p.partition(gridSize);

        int marked = 0;
        for (ExecutionContext ctx : parts.values()) {
            String fileName = ctx.getString("fileName");
            toDelete.add(Path.of(fileName));
            int start = ctx.getInt("startIndex");
            int end = ctx.getInt("endIndex");
            if (start <= end) marked += (end - start + 1);
        }

        assertThat(marked).isEqualTo(total);
    }
}
