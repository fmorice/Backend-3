package com.duoc.migracion.listener;

import com.duoc.migracion.policy.BatchRecordPolicy;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class BatchSkipListener implements SkipListener<Object, Object> {

    private final BatchRecordPolicy batchRecordPolicy;

    public BatchSkipListener(BatchRecordPolicy batchRecordPolicy) {
        this.batchRecordPolicy = batchRecordPolicy;
    }

    @Override
    public void onSkipInRead(Throwable throwable) {
        logSkip("lectura", null, throwable);
    }

    @Override
    public void onSkipInWrite(Object item, Throwable throwable) {
        logSkip("escritura", item, throwable);
    }

    @Override
    public void onSkipInProcess(Object item, Throwable throwable) {
        logSkip("procesamiento", item, throwable);
    }

    private void logSkip(String etapa, Object item, Throwable throwable) {
        String category = batchRecordPolicy.classify(throwable);
        System.out.println("[SkipListener] Registro omitido en " + etapa + ". Causa: " + category + " - " + throwable.getMessage());
        if (item != null) {
            System.out.println("[SkipListener] Registro descartado: " + item);
        }
    }
}
