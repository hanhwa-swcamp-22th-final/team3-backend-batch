package com.ohgiraffers.team3backendbatch.tool.mes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesEnvironmentEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesEquipmentStatusEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesProductionResultEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesQualityMeasurementEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesQualityResultEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.MesKafkaTopics;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public final class MesCsvKafkaPublisherMain {

    private static final DateTimeFormatter[] DATE_TIME_FORMATTERS = new DateTimeFormatter[]{
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    };

    private MesCsvKafkaPublisherMain() {
    }

    public static void main(String[] args) throws Exception {
        MesCsvPublisherOptions options = MesCsvPublisherOptions.fromArgs(args);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Properties properties = new Properties();
        properties.put("bootstrap.servers", options.bootstrapServers());
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
            int productionCount = publishProductionResults(
                producer,
                objectMapper,
                options.dataDir().resolve("mes_production_results.csv"),
                options.maxRowsPerFile()
            );
            int qualityResultCount = publishQualityResults(
                producer,
                objectMapper,
                options.dataDir().resolve("mes_quality_results.csv"),
                options.maxRowsPerFile()
            );
            int qualityMeasurementCount = publishQualityMeasurements(
                producer,
                objectMapper,
                options.dataDir().resolve("mes_quality_measurements.csv"),
                options.maxRowsPerFile()
            );
            int equipmentStatusCount = publishEquipmentStatuses(
                producer,
                objectMapper,
                options.dataDir().resolve("mes_equipment_status.csv"),
                options.maxRowsPerFile()
            );
            int environmentCount = publishEnvironmentEvents(
                producer,
                objectMapper,
                options.dataDir().resolve("mes_environment_events.csv"),
                options.maxRowsPerFile()
            );
            producer.flush();

            System.out.printf(
                "Published MES CSV rows. production=%d, qualityResults=%d, qualityMeasurements=%d, equipmentStatuses=%d, environmentEvents=%d%n",
                productionCount,
                qualityResultCount,
                qualityMeasurementCount,
                equipmentStatusCount,
                environmentCount
            );
        }
    }

    private static int publishProductionResults(
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper,
        Path file,
        int maxRows
    ) throws Exception {
        return publishCsv(file, maxRows, headerIndex -> row -> {
            MesProductionResultEvent event = MesProductionResultEvent.builder()
                .eventId(value(row, headerIndex, "event_id"))
                .equipmentId(parseLong(value(row, headerIndex, "equipment_id")))
                .sourceEquipmentCode(value(row, headerIndex, "source_equipment_code"))
                .equipmentNameSnapshot(value(row, headerIndex, "equipment_name_snapshot"))
                .inputLotNo(value(row, headerIndex, "input_lot_no"))
                .startTime(parseDateTime(value(row, headerIndex, "start_time")))
                .endTime(parseDateTime(value(row, headerIndex, "end_time")))
                .cycleTimeSec(parseDecimal(value(row, headerIndex, "cycle_time_sec")))
                .leadTimeSec(parseDecimal(value(row, headerIndex, "lead_time_sec")))
                .inputQty(parseDecimal(value(row, headerIndex, "input_qty")))
                .outputQty(parseDecimal(value(row, headerIndex, "output_qty")))
                .goodQty(parseDecimal(value(row, headerIndex, "good_qty")))
                .defectQty(parseDecimal(value(row, headerIndex, "defect_qty")))
                .occurredAt(parseDateTime(value(row, headerIndex, "start_time")))
                .build();

            send(producer, objectMapper, MesKafkaTopics.MES_PRODUCTION_RESULT, event.getEventId(), event);
        });
    }

    private static int publishQualityResults(
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper,
        Path file,
        int maxRows
    ) throws Exception {
        return publishCsv(file, maxRows, headerIndex -> row -> {
            MesQualityResultEvent event = MesQualityResultEvent.builder()
                .qualityResultId(parseLong(value(row, headerIndex, "quality_result_id")))
                .prodLotNo(value(row, headerIndex, "prod_lot_no"))
                .equipmentId(parseLong(value(row, headerIndex, "equipment_id")))
                .sourceEquipmentCode(value(row, headerIndex, "source_equipment_code"))
                .inputLotNo(value(row, headerIndex, "input_lot_no"))
                .eventTimeStamp(parseDateTime(value(row, headerIndex, "event_time_stamp")))
                .overallResult(value(row, headerIndex, "overall_result"))
                .occurredAt(parseDateTime(value(row, headerIndex, "event_time_stamp")))
                .build();

            send(
                producer,
                objectMapper,
                MesKafkaTopics.MES_QUALITY_RESULT,
                String.valueOf(event.getQualityResultId()),
                event
            );
        });
    }

    private static int publishQualityMeasurements(
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper,
        Path file,
        int maxRows
    ) throws Exception {
        return publishCsv(file, maxRows, headerIndex -> row -> {
            MesQualityMeasurementEvent event = MesQualityMeasurementEvent.builder()
                .qualityResultId(parseLong(value(row, headerIndex, "quality_result_id")))
                .processCode(value(row, headerIndex, "process_code"))
                .measureItem(value(row, headerIndex, "measure_item"))
                .prodLotNo(value(row, headerIndex, "prod_lot_no"))
                .inputLotNo(value(row, headerIndex, "input_lot_no"))
                .ucl(parseDecimal(value(row, headerIndex, "ucl")))
                .targetValue(parseDecimal(value(row, headerIndex, "target_value")))
                .lcl(parseDecimal(value(row, headerIndex, "lcl")))
                .measuredValue(parseDecimal(value(row, headerIndex, "measured_value")))
                .judgeResult(value(row, headerIndex, "judge_result"))
                .occurredAt(null)
                .build();

            String key = "%s|%s|%s".formatted(
                event.getQualityResultId(),
                event.getProcessCode(),
                event.getMeasureItem()
            );
            send(producer, objectMapper, MesKafkaTopics.MES_QUALITY_MEASUREMENT, key, event);
        });
    }

    private static int publishEquipmentStatuses(
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper,
        Path file,
        int maxRows
    ) throws Exception {
        return publishCsv(file, maxRows, headerIndex -> row -> {
            MesEquipmentStatusEvent event = MesEquipmentStatusEvent.builder()
                .equipmentId(parseLong(value(row, headerIndex, "equipment_id")))
                .sourceEquipmentCode(value(row, headerIndex, "source_equipment_code"))
                .statusType(value(row, headerIndex, "status_type"))
                .startTimeStamp(parseDateTime(value(row, headerIndex, "start_time_stamp")))
                .endTimeStamp(parseDateTime(value(row, headerIndex, "end_time_stamp")))
                .alarmCode(value(row, headerIndex, "alarm_code"))
                .alarmDesc(value(row, headerIndex, "alarm_desc"))
                .occurredAt(parseDateTime(value(row, headerIndex, "start_time_stamp")))
                .build();

            String key = "%s|%s|%s".formatted(
                event.getEquipmentId(),
                event.getStatusType(),
                event.getStartTimeStamp()
            );
            send(producer, objectMapper, MesKafkaTopics.MES_EQUIPMENT_STATUS, key, event);
        });
    }

    private static int publishEnvironmentEvents(
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper,
        Path file,
        int maxRows
    ) throws Exception {
        return publishCsv(file, maxRows, headerIndex -> row -> {
            MesEnvironmentEvent event = MesEnvironmentEvent.builder()
                .eventId(value(row, headerIndex, "event_id"))
                .equipmentId(parseLong(value(row, headerIndex, "equipment_id")))
                .sourceEquipmentCode(value(row, headerIndex, "source_equipment_code"))
                .envTemperature(parseDecimal(value(row, headerIndex, "env_temperature")))
                .envHumidity(parseDecimal(value(row, headerIndex, "env_humidity")))
                .envParticleCnt(parseInteger(value(row, headerIndex, "env_particle_cnt")))
                .envDetectedAt(parseDateTime(value(row, headerIndex, "env_detected_at")))
                .occurredAt(parseDateTime(value(row, headerIndex, "env_detected_at")))
                .build();

            send(producer, objectMapper, MesKafkaTopics.MES_ENVIRONMENT, event.getEventId(), event);
        });
    }

    private static int publishCsv(Path file, int maxRows, CsvRowPublisherFactory publisherFactory) throws Exception {
        if (!Files.exists(file)) {
            System.out.printf("Skip missing file: %s%n", file);
            return 0;
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0;
            }

            String[] headers = splitCsv(headerLine);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(normalizeCsvToken(headers[i]), i);
            }

            CsvRowPublisher publisher = publisherFactory.create(headerIndex);
            int published = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (maxRows > 0 && published >= maxRows) {
                    break;
                }
                if (line.isBlank()) {
                    continue;
                }
                publisher.publish(splitCsv(line));
                published++;
            }
            return published;
        }
    }

    private static void send(
        KafkaProducer<String, String> producer,
        ObjectMapper objectMapper,
        String topic,
        String key,
        Object payload
    ) throws Exception {
        producer.send(new ProducerRecord<>(topic, key, objectMapper.writeValueAsString(payload)));
    }

    private static String[] splitCsv(String line) {
        return line.split(",", -1);
    }

    private static String value(String[] row, Map<String, Integer> headerIndex, String header) {
        Integer index = headerIndex.get(header);
        if (index == null || index >= row.length) {
            return null;
        }
        String value = normalizeCsvToken(row[index]);
        return value.isEmpty() ? null : value;
    }

    private static String normalizeCsvToken(String value) {
        if (value == null) {
            return null;
        }
        return value
            .replace("\uFEFF", "")
            .replace("\u200B", "")
            .trim();
    }

    private static Long parseLong(String value) {
        return value == null ? null : Long.parseLong(value);
    }

    private static BigDecimal parseDecimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    private static Integer parseInteger(String value) {
        return value == null ? null : Integer.parseInt(value);
    }

    private static LocalDateTime parseDateTime(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(DATE_TIME_FORMATTERS)
            .map(formatter -> tryParse(value, formatter))
            .filter(dateTime -> dateTime != null)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported datetime value: " + value));
    }

    private static LocalDateTime tryParse(String value, DateTimeFormatter formatter) {
        try {
            return LocalDateTime.parse(value, formatter);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    @FunctionalInterface
    private interface CsvRowPublisherFactory {
        CsvRowPublisher create(Map<String, Integer> headerIndex);
    }

    @FunctionalInterface
    private interface CsvRowPublisher {
        void publish(String[] row) throws Exception;
    }

    private record MesCsvPublisherOptions(Path dataDir, String bootstrapServers, int maxRowsPerFile) {

        private static final Path DEFAULT_DATA_DIR =
            Path.of("C:/SWCAMP22/team_project/final/monthly-team-sample-data");

        static MesCsvPublisherOptions fromArgs(String[] args) {
            Path dataDir = DEFAULT_DATA_DIR;
            String bootstrapServers = System.getenv().getOrDefault("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
            int maxRowsPerFile = 1000;

            for (String arg : args) {
                if (arg.startsWith("--data-dir=")) {
                    dataDir = Path.of(arg.substring("--data-dir=".length()));
                } else if (arg.startsWith("--bootstrap-servers=")) {
                    bootstrapServers = arg.substring("--bootstrap-servers=".length());
                } else if (arg.startsWith("--max-rows=")) {
                    maxRowsPerFile = Integer.parseInt(arg.substring("--max-rows=".length()));
                }
            }

            return new MesCsvPublisherOptions(dataDir, bootstrapServers, maxRowsPerFile);
        }
    }
}
