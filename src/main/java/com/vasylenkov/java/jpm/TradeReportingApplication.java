package com.vasylenkov.java.jpm;


import java.io.IOException;
import java.util.List;

public class TradeReportingApplication {

    /**
     *
     * @param args - path to folder with data should be specified as application argument
     *       i.e. "/home/json_data"
     *
     */
    public static void main(String[] args) throws IOException {

       InstructionProcessor processor = InstructionProcessor.getInstance();

       if (args.length == 0 || args.length > 1) {
           throw new IllegalArgumentException("Path to json data folder not specified");
       }

        String pathToDataFolder = args[0];

        List<Instruction> instructions = processor.readInstructions(pathToDataFolder);

        System.out.println("--- Daily Report ---");
        processor.printDailyReport(
                processor.collectDailyReport(instructions)
        );

        System.out.println();
        System.out.println("--- Rank Report ---");
        processor.printRankReport(
                processor.collectRankReport(instructions)
            );

    }

}
