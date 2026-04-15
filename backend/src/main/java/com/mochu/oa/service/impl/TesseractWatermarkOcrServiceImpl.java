package com.mochu.oa.service.impl;

import com.mochu.oa.common.WatermarkOcrResult;
import com.mochu.oa.service.WatermarkOcrService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TesseractWatermarkOcrServiceImpl implements WatermarkOcrService {

    private static final Pattern DATE_PATTERN = Pattern.compile("(19|20)\\d{2}[-/_.]?([01]?\\d)[-/_.]?([0-3]?\\d)");
    private static final Map<String, CacheValue> OCR_CACHE = new ConcurrentHashMap<>();
    private static final AtomicLong TOTAL_REQUESTS = new AtomicLong();
    private static final AtomicLong CACHE_HITS = new AtomicLong();
    private static final AtomicLong OCR_SUCCESSES = new AtomicLong();
    private static final AtomicLong OCR_FAILURES = new AtomicLong();
    private static final AtomicLong TOTAL_LATENCY_MS = new AtomicLong();

    @Value("${ocr.tesseract.enabled:false}")
    private boolean enabled;

    @Value("${ocr.tesseract.datapath:}")
    private String dataPath;

    @Value("${ocr.tesseract.language:chi_sim+eng}")
    private String language;

    @Value("${ocr.tesseract.psm:6}")
    private int psm;

    @Value("${ocr.tesseract.oem:1}")
    private int oem;

    @Value("${ocr.tesseract.timeout-seconds:5}")
    private int timeoutSeconds;

    @Value("${ocr.tesseract.cache-ttl-seconds:600}")
    private long cacheTtlSeconds;

    @Value("${ocr.tesseract.base-url:http://127.0.0.1:9090}")
    private String baseUrl;

    @Value("${ocr.tesseract.metrics-log-interval:50}")
    private long metricsLogInterval;

    @Override
    public WatermarkOcrResult parsePhoto(String photoUrl, String taskName) {
        long startNs = System.nanoTime();
        if (!enabled) {
            return new WatermarkOcrResult(false, false, "", "tesseract disabled");
        }
        if (photoUrl == null || photoUrl.isBlank()) {
            return new WatermarkOcrResult(false, false, "", "empty photo url");
        }
        if (dataPath == null || dataPath.isBlank()) {
            return new WatermarkOcrResult(false, false, "", "tesseract datapath not configured");
        }
        String finalUrl = normalizePhotoUrl(photoUrl);
        String cacheKey = (finalUrl + "|" + (taskName == null ? "" : taskName.trim())).toLowerCase(Locale.ROOT);
        CacheValue cached = OCR_CACHE.get(cacheKey);
        long now = Instant.now().getEpochSecond();
        if (cached != null && cached.expireAtEpochSecond >= now) {
            CACHE_HITS.incrementAndGet();
            return finish(cached.result, startNs);
        }

        Path tmp = null;
        try {
            tmp = downloadToTempFile(finalUrl);
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(dataPath);
            tesseract.setLanguage(language);
            tesseract.setPageSegMode(psm);
            tesseract.setOcrEngineMode(oem);
            String text = tesseract.doOCR(tmp.toFile());
            boolean valid = isTextMatched(text, taskName);
            WatermarkOcrResult result = new WatermarkOcrResult(true, valid, text, valid ? "ocr matched" : "ocr text not matched");
            OCR_CACHE.put(cacheKey, new CacheValue(result, now + Math.max(30, cacheTtlSeconds)));
            OCR_SUCCESSES.incrementAndGet();
            return finish(result, startNs);
        } catch (Exception e) {
            log.warn("Tesseract OCR failed for photo: {}", finalUrl, e);
            OCR_FAILURES.incrementAndGet();
            return finish(new WatermarkOcrResult(false, false, "", "ocr failed: " + e.getMessage()), startNs);
        } finally {
            if (tmp != null) {
                try {
                    Files.deleteIfExists(tmp);
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public Map<String, Object> metricsSnapshot() {
        long total = TOTAL_REQUESTS.get();
        long hit = CACHE_HITS.get();
        long success = OCR_SUCCESSES.get();
        long fail = OCR_FAILURES.get();
        long avgLatency = total == 0 ? 0 : TOTAL_LATENCY_MS.get() / total;
        long hitRate = total == 0 ? 0 : hit * 100 / total;
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("enabled", enabled);
        metrics.put("cacheSize", OCR_CACHE.size());
        metrics.put("total", total);
        metrics.put("cacheHit", hit);
        metrics.put("hitRate", hitRate);
        metrics.put("ocrSuccess", success);
        metrics.put("ocrFail", fail);
        metrics.put("avgLatencyMs", avgLatency);
        metrics.put("cacheTtlSeconds", cacheTtlSeconds);
        metrics.put("timeoutSeconds", timeoutSeconds);
        metrics.put("language", language);
        return metrics;
    }

    private String normalizePhotoUrl(String photoUrl) {
        String decoded = URLDecoder.decode(photoUrl, StandardCharsets.UTF_8);
        if (decoded.startsWith("http://") || decoded.startsWith("https://")) {
            return decoded;
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            return decoded;
        }
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = decoded.startsWith("/") ? decoded : "/" + decoded;
        return normalizedBase + normalizedPath;
    }

    private Path downloadToTempFile(String photoUrl) throws IOException, InterruptedException {
        URI uri = URI.create(photoUrl);
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IOException("only http/https image url supported for OCR");
        }
        var client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        var request = java.net.http.HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(Math.max(1, timeoutSeconds)))
                .GET()
                .build();
        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("download image failed, status=" + response.statusCode());
        }
        Path tmp = Files.createTempFile("mochu-ocr-", ".img");
        Files.write(tmp, response.body());
        return tmp;
    }

    private boolean isTextMatched(String text, String taskName) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = text.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        boolean hasDate = DATE_PATTERN.matcher(normalized).find();
        if (taskName == null || taskName.isBlank()) {
            return hasDate;
        }
        String normalizedTask = taskName.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        return hasDate && normalized.contains(normalizedTask);
    }

    private WatermarkOcrResult finish(WatermarkOcrResult result, long startNs) {
        long requestNo = TOTAL_REQUESTS.incrementAndGet();
        long latencyMs = (System.nanoTime() - startNs) / 1_000_000;
        TOTAL_LATENCY_MS.addAndGet(latencyMs);

        long interval = Math.max(1, metricsLogInterval);
        if (requestNo % interval == 0) {
            long total = TOTAL_REQUESTS.get();
            long hit = CACHE_HITS.get();
            long success = OCR_SUCCESSES.get();
            long fail = OCR_FAILURES.get();
            long avgLatency = total == 0 ? 0 : TOTAL_LATENCY_MS.get() / total;
            long hitRate = total == 0 ? 0 : hit * 100 / total;
            log.info("OCR metrics: total={}, cacheHit={}, hitRate={}%, ocrSuccess={}, ocrFail={}, avgLatencyMs={}",
                    total, hit, hitRate, success, fail, avgLatency);
        }
        return result;
    }

    private static final class CacheValue {
        private final WatermarkOcrResult result;
        private final long expireAtEpochSecond;

        private CacheValue(WatermarkOcrResult result, long expireAtEpochSecond) {
            this.result = Objects.requireNonNull(result);
            this.expireAtEpochSecond = expireAtEpochSecond;
        }
    }
}
