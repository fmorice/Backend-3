package com.duoc.migracion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TransaccionPartitioner implements Partitioner {

    private static final Logger logger = LoggerFactory.getLogger(TransaccionPartitioner.class);

    private final String resourcePath = "data/transacciones.csv";

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(resourcePath).getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine(); // header
            int total = 0;
            while (reader.readLine() != null) total++;

            logger.info("Total transaccion records (excluding header): {}", total);

            int base = total / gridSize;
            int remainder = total % gridSize;
            int start = 0;

            for (int i = 0; i < gridSize; i++) {
                int records = base + (i < remainder ? 1 : 0);
                int end = start + records - 1;

                // create temp file with header + assigned lines
                File tmp = File.createTempFile("transacciones-part-" + i + "-", ".csv");
                tmp.deleteOnExit();
                try (FileWriter fw = new FileWriter(tmp, StandardCharsets.UTF_8)) {
                    fw.write(header);
                    fw.write(System.lineSeparator());

                    // copy assigned lines
                    try (BufferedReader r2 = new BufferedReader(new InputStreamReader(
                            new ClassPathResource(resourcePath).getInputStream(), StandardCharsets.UTF_8))) {
                        // skip header
                        r2.readLine();
                        int idx = 0;
                        int copied = 0;
                        String line;
                        while ((line = r2.readLine()) != null) {
                            if (idx >= start && idx <= end) {
                                fw.write(line);
                                fw.write(System.lineSeparator());
                                copied++;
                            }
                            idx++;
                            if (idx > end) break;
                        }
                        logger.info("Partition {} -> start={} end={} recordsCopied={}", i, start, end, copied);
                    }
                }

                ExecutionContext ctx = new ExecutionContext();
                ctx.putString("fileName", tmp.getAbsolutePath());
                ctx.putInt("partitionId", i);
                ctx.putInt("startIndex", start);
                ctx.putInt("endIndex", end);

                result.put("partition" + i, ctx);

                start = end + 1;
            }

            logger.info("Created {} partitions for resource {}", result.size(), resourcePath);

        } catch (Exception e) {
            logger.error("Error creating partitions", e);
            throw new RuntimeException(e);
        }

        return result;
    }
}
