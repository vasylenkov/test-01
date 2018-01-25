package com.vasylenkov.java.jpm;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class InstructionProcessor {

    private static ObjectMapper mapper = new ObjectMapper();

    private static InstructionProcessor instance;

    private InstructionProcessor() {}

    static synchronized InstructionProcessor getInstance(){
        if(instance == null){
            instance = new InstructionProcessor();
        }
        return instance;
    }

    /**
     * This method reads all available instructions from JSON files located in the data folder.
     *
     * @param pathToDataFolder - path to folder with JSON files of instructions
     * @return - List of all available instructions
     * @throws IOException - IOException in case of any exception during to JSON-files parsing
     */
    List<Instruction> readInstructions(String pathToDataFolder) throws IOException {

        mapper.registerModule(new JavaTimeModule());
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);

        List<Instruction> instructions = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(pathToDataFolder))) {
            for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                instructions.addAll(mapper.readValue(path.toFile(), mapper.getTypeFactory().constructCollectionType(List.class, Instruction.class)));
            }
        }

        return instructions;
    }

    /**
     *  This method allows to collect the daily report.
     *  It collects information about amount in USD (cost) settled incoming and outgoing everyday.
     *
     * @param instructions - list of instructions
     * @return - Tree map grouped by Settlement Date and Action (S / B). Also contains information about amount (cost) in USD settled incoming and outgoing everyday
     */
    TreeMap<LocalDate, TreeMap<String, BigDecimal>> collectDailyReport(List<Instruction> instructions) {
        return instructions.stream().collect(
                Collectors.groupingBy(Instruction::getSettlementDate, TreeMap::new,
                        Collectors.groupingBy(Instruction::getAction, TreeMap::new,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Instruction::getCost,
                                        BigDecimal::add))
                )
        );
    }

    /**
     * This method prints to System.out data collected to daily report
     *
     * @param instructionsByEntityAndAction - collected daily report data
     */
    void printDailyReport(TreeMap<LocalDate, TreeMap<String, BigDecimal>> instructionsByEntityAndAction) {
        instructionsByEntityAndAction.forEach((settledDate, data) ->
                {
                    System.out.println((settledDate + ":"));
                    data.forEach(
                            (action, cost) ->
                                    System.out.println(new StringBuilder()
                                            .append(action)
                                            .append(": $")
                                            .append(String.format("%12.3f", cost))
                                    )
                    );
                }
        );

    }

    /**
     *  This method allows to collect the rank report.
     *  It collects information about rank of entities based on incoming and outgoing amount. Eg: If entity foo instructs the highest
     *   amount for a buy instruction, then foo is rank 1 for outgoing
     *
     * @param instructions - list of instructions
     * @return - Tree map grouped by Action (S / B) and Entities. Also contains information about total settled incoming and outgoing amount
     */
    TreeMap<String, TreeMap<String, BigDecimal>> collectRankReport(List<Instruction> instructions) {
        return instructions.stream().collect(
                Collectors.groupingBy(Instruction::getAction, TreeMap::new,
                        Collectors.groupingBy(Instruction::getEntity, TreeMap::new,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Instruction::getCost,
                                        BigDecimal::add))
                )
        );
    }

    /**
     * This method prints to System.out data collected to rank report
     *
     * @param instructionsByActionAndEntity - collected rank report data
     */
    void printRankReport (TreeMap<String, TreeMap<String, BigDecimal>> instructionsByActionAndEntity) {

        instructionsByActionAndEntity.forEach((action, values) ->
                {
                    System.out.println((action + ":"));

                    final AtomicInteger counter = new AtomicInteger(1);

                    LinkedHashMap<String, BigDecimal> sortedByTotalCost = values.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                            .collect(Collectors.toMap(Map.Entry::getKey,
                                    Map.Entry::getValue, (entity, totalCost) -> entity, LinkedHashMap::new));

                    sortedByTotalCost.forEach(
                            (entity, totalCost) ->
                                System.out.println(new StringBuilder()
                                    .append(String.format("%3d ", counter.getAndIncrement()))
                                    .append(String.format("%-20s", entity))
                                    .append("   $")
                                    .append(String.format("%12.3f", totalCost))
                                )
                    );
                }
        );
    }
}