package lv.darzaplanotajs.address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Ielādē VZD adreses no aw_eka.csv un ik pēc 6 stundām pārbauda, vai tās nav novecojušas.
 * Ja kaut kas noiet greizi, vecie dati paliek.
 */
@Component
@ConditionalOnProperty(name = "app.address.import.enabled", havingValue = "true")
public class AddressImporter {

    private static final Logger log = LoggerFactory.getLogger(AddressImporter.class);

    /** EKS nozīmē, ka adrese eksistē */
    private static final String EXISTING = "EKS";
    /** Pilnā sarakstā ir simtiem tūkstošu rindu, tāpēc mazāk nozīmē bojātu failu */
    private static final int MIN_ROWS = 100_000;
    private static final int BATCH_SIZE = 5_000;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;
    private final String source;
    private final Charset charset;
    private final Duration maxAge;

    public AddressImporter(JdbcTemplate jdbc, TransactionTemplate transaction,
                           @Value("${app.address.import.source}") String source,
                           @Value("${app.address.import.charset}") Charset charset,
                           @Value("${app.address.import.max-age}") Duration maxAge) {
        this.jdbc = jdbc;
        this.transaction = transaction;
        this.source = source;
        this.charset = charset;
        this.maxAge = maxAge;
    }

    /** Spring plānotājam ir viens pavediens, tāpēc ar fixedDelay importi nekad nepārklājas */
    @Scheduled(initialDelayString = "PT30S", fixedDelayString = "PT6H")
    public void importIfStale() {
        if (!isStale()) {
            return;
        }
        try {
            importAddresses();
        } catch (Exception e) {
            log.error("Address import failed, keeping previous data", e);
        }
    }

    private boolean isStale() {
        OffsetDateTime last = jdbc.queryForObject("SELECT max(imported_at) FROM address_import", OffsetDateTime.class);
        return last == null || last.isBefore(OffsetDateTime.now().minus(maxAge));
    }

    private void importAddresses() throws IOException, InterruptedException {
        long started = System.currentTimeMillis();
        boolean remote = source.startsWith("http");
        Path file = remote ? download(URI.create(source)) : Path.of(source);
        try {
            jdbc.update("TRUNCATE address_staging");
            int staged = loadStaging(file);
            if (staged < MIN_ROWS) {
                throw new IllegalStateException("Only " + staged + " addresses parsed, expected at least " + MIN_ROWS);
            }
            int imported = swapIntoAddressTable();
            log.info("Imported {} addresses in {} s", imported, (System.currentTimeMillis() - started) / 1000);
        } finally {
            jdbc.update("TRUNCATE address_staging");
            if (remote) {
                Files.deleteIfExists(file);
            }
        }
    }

    private Path download(URI uri) throws IOException, InterruptedException {
        Path file = Files.createTempFile("aw_eka", ".csv");
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()) {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(10)).build();
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(file));
            if (response.statusCode() != 200) {
                throw new IOException("Download failed with HTTP " + response.statusCode() + ": " + uri);
            }
            return file;
        } catch (IOException | InterruptedException e) {
            Files.deleteIfExists(file);
            throw e;
        }
    }

    /** Bojātas rindas izlaiž, lai viena slikta rinda neapturētu visu importu */
    private int loadStaging(Path file) throws IOException {
        int rows = 0;
        int skipped = 0;
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            for (CSVRecord record : formatOf(reader).parse(reader)) {
                if (!EXISTING.equals(record.get("STATUSS"))) {
                    continue;
                }
                try {
                    batch.add(new Object[]{
                            Long.parseLong(record.get("KODS").trim()),
                            record.get("STD"),
                            parseCoordinate(record.get("KOORD_X")),
                            parseCoordinate(record.get("KOORD_Y"))});
                } catch (IllegalArgumentException e) { // arī NumberFormatException, ja vērtība tukša vai bojāta
                    skipped++;
                    continue;
                }
                if (batch.size() == BATCH_SIZE) {
                    rows += insertStaging(batch);
                }
            }
        }
        if (skipped > 0) {
            log.warn("Skipped {} address rows without valid code or coordinates", skipped);
        }
        return rows + insertStaging(batch);
    }

    /**
     * Vecie VAR faili lieto citu atdalītāju nekā VZD apraksta, tāpēc formātu nosaka pēc galvenes.
     * Ja X un Y izrādās otrādi, swapIntoAddressTable to izlabo.
     */
    private static CSVFormat formatOf(BufferedReader reader) throws IOException {
        reader.mark(4096);
        String header = reader.readLine();
        reader.reset();
        if (header != null && header.startsWith("﻿")) {
            reader.skip(1); // izlaiž BOM
        }
        boolean legacy = header != null && header.contains(";");
        return CSVFormat.DEFAULT.builder()
                .setDelimiter(legacy ? ';' : ',')
                .setQuote(legacy ? '#' : '"')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setAllowMissingColumnNames(true)
                .build();
    }

    private static double parseCoordinate(String value) {
        return Double.parseDouble(value.trim().replace(',', '.'));
    }

    private int insertStaging(List<Object[]> batch) {
        jdbc.batchUpdate("INSERT INTO address_staging (code, label, east, north) VALUES (?, ?, ?, ?)", batch);
        int size = batch.size();
        batch.clear();
        return size;
    }

    /**
     * Aizstāj visu vienā transakcijā, lai lasītāji līdz beigām redz vecos datus.
     * Latvijā austrumu koordināta parasti ir lielāka par ziemeļu, tā pārbauda, vai X un Y nav otrādi.
     */
    private int swapIntoAddressTable() {
        boolean swapped = Boolean.TRUE.equals(
                jdbc.queryForObject("SELECT avg(east) < avg(north) FROM address_staging", Boolean.class));
        if (swapped) {
            log.info("VZD KOORD_X is northing, swapping axes");
        }
        String eastNorth = swapped ? "north, east" : "east, north";
        Integer imported = transaction.execute(status -> {
            jdbc.update("DELETE FROM address");
            int rows = jdbc.update("INSERT INTO address (code, label, search_text, east, north) "
                    + "SELECT code, label, " + PostgresAddressSearch.normalized("label") + ", " + eastNorth
                    + " FROM address_staging ON CONFLICT (code) DO NOTHING");
            jdbc.update("INSERT INTO address_import (imported_at, row_count) VALUES (now(), ?)", rows);
            return rows;
        });
        return imported == null ? 0 : imported;
    }
}
