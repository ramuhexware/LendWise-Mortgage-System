package com.lendwise.patterns.consumer.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Batch processor demonstrating various loop patterns.
 * Parser should detect: for, while, do-while, enhanced for, iterator loops
 */
@Component
@Slf4j
public class BatchProcessor {

    private static final int DEFAULT_BATCH_SIZE = 100;

    /**
     * Process using traditional FOR loop.
     * Parser detects: traditional for loop
     */
    public List<ProcessingResult> processWithForLoop(List<String> items) {
        List<ProcessingResult> results = new ArrayList<>();

        // Parser detects: traditional for loop with index
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            log.debug("Processing item {} of {}: {}", i + 1, items.size(), item);

            ProcessingResult result = processItem(item, i);
            results.add(result);
        }

        return results;
    }

    /**
     * Process using enhanced FOR loop (for-each).
     * Parser detects: enhanced for-each loop
     */
    public List<ProcessingResult> processWithEnhancedFor(List<String> items) {
        List<ProcessingResult> results = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);

        // Parser detects: enhanced for-each loop
        for (String item : items) {
            int index = counter.getAndIncrement();
            log.debug("Enhanced for processing: {}", item);

            ProcessingResult result = processItem(item, index);
            results.add(result);
        }

        return results;
    }

    /**
     * Process using WHILE loop.
     * Parser detects: while loop
     */
    public List<ProcessingResult> processWithWhile(List<String> items) {
        List<ProcessingResult> results = new ArrayList<>();
        int index = 0;

        // Parser detects: while loop
        while (index < items.size()) {
            String item = items.get(index);
            log.debug("While loop processing index {}: {}", index, item);

            ProcessingResult result = processItem(item, index);
            results.add(result);

            index++;
        }

        return results;
    }

    /**
     * Process using DO-WHILE loop.
     * Parser detects: do-while loop
     */
    public List<ProcessingResult> processWithDoWhile(List<String> items) {
        List<ProcessingResult> results = new ArrayList<>();

        if (items.isEmpty()) {
            return results;
        }

        int index = 0;

        // Parser detects: do-while loop
        do {
            String item = items.get(index);
            log.debug("Do-while processing index {}: {}", index, item);

            ProcessingResult result = processItem(item, index);
            results.add(result);

            index++;
        } while (index < items.size());

        return results;
    }

    /**
     * Process using Iterator.
     * Parser detects: iterator pattern with hasNext()/next()
     */
    public List<ProcessingResult> processWithIterator(List<String> items) {
        List<ProcessingResult> results = new ArrayList<>();
        int index = 0;

        // Parser detects: iterator pattern
        Iterator<String> iterator = items.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            log.debug("Iterator processing: {}", item);

            ProcessingResult result = processItem(item, index++);
            results.add(result);
        }

        return results;
    }

    /**
     * Process in batches with nested loops.
     * Parser detects: nested loops
     */
    public List<BatchResult> processInBatches(List<String> items, int batchSize) {
        List<BatchResult> batchResults = new ArrayList<>();
        int totalBatches = (int) Math.ceil((double) items.size() / batchSize);

        // Parser detects: outer for loop
        for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
            int startIndex = batchIndex * batchSize;
            int endIndex = Math.min(startIndex + batchSize, items.size());

            log.info("Processing batch {} of {} (items {} to {})",
                     batchIndex + 1, totalBatches, startIndex, endIndex - 1);

            List<ProcessingResult> batchItems = new ArrayList<>();

            // Parser detects: inner for loop (nested)
            for (int i = startIndex; i < endIndex; i++) {
                String item = items.get(i);
                ProcessingResult result = processItem(item, i);
                batchItems.add(result);
            }

            BatchResult batchResult = new BatchResult(batchIndex, batchItems);
            batchResults.add(batchResult);
        }

        return batchResults;
    }

    /**
     * Retry logic with while and break/continue.
     * Parser detects: while loop with break/continue
     */
    public ProcessingResult processWithRetry(String item, int maxRetries) {
        int attempt = 0;
        ProcessingResult result = null;

        // Parser detects: while loop with break/continue
        while (attempt < maxRetries) {
            attempt++;
            log.info("Attempt {} of {} for item: {}", attempt, maxRetries, item);

            try {
                result = processItem(item, 0);

                if (result.isSuccess()) {
                    log.info("Success on attempt {}", attempt);
                    break;  // Parser detects: break statement
                }

                log.warn("Attempt {} failed, retrying...", attempt);
                continue;  // Parser detects: continue statement

            } catch (Exception e) {
                log.error("Error on attempt {}: {}", attempt, e.getMessage());

                if (attempt >= maxRetries) {
                    return new ProcessingResult(item, false, "Max retries exceeded");
                }
            }
        }

        return result != null ? result : new ProcessingResult(item, false, "No result");
    }

    /**
     * Infinite loop with condition (simulated).
     * Parser detects: labeled statements, nested break
     */
    public void processUntilCondition(List<String> items) {
        // Parser detects: labeled statement
        outer:
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);

            // Parser detects: inner loop
            for (int j = 0; j < 10; j++) {
                log.debug("Processing item {} iteration {}", item, j);

                if (item.equals("STOP")) {
                    log.info("Stop signal received");
                    // Parser detects: labeled break
                    break outer;
                }

                if (item.equals("SKIP")) {
                    log.info("Skip signal for item");
                    // Parser detects: labeled continue
                    continue outer;
                }
            }
        }
    }

    // Helper method
    private ProcessingResult processItem(String item, int index) {
        // Simulate processing
        boolean success = !item.contains("ERROR");
        String message = success ? "Processed successfully" : "Processing failed";
        return new ProcessingResult(item, success, message);
    }

    // Result records
    public record ProcessingResult(String item, boolean success, String message) {
        public boolean isSuccess() { return success; }
    }

    public record BatchResult(int batchIndex, List<ProcessingResult> results) {
        public int getSuccessCount() {
            return (int) results.stream().filter(ProcessingResult::success).count();
        }
    }
}
