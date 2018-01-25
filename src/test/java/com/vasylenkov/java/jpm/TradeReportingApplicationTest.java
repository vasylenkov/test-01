package com.vasylenkov.java.jpm;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class TradeReportingApplicationTest {

    private String basePath = "/home/java/jpmtest/target/classes/json_data/junit_test_instructions/";

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testMainWithBadCommandLineZeroParams() throws IOException{
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Path to json data folder not specified");
        TradeReportingApplication.main(new String[]{});
    }

    @Test
    public void testMainWithBadCommandLineMoreThanOneParam()  throws IOException {
        expectedEx.expect(IllegalArgumentException.class);
        TradeReportingApplication.main(new String[]{"/home", "/data"});
    }


    @Test
    public void testUnexpectedCharacter() throws IOException {
        expectedEx.expect(com.fasterxml.jackson.databind.JsonMappingException.class);
        expectedEx.expectMessage("Unexpected character ('+' (code 43)): was expecting double-quote to start field name");
        TradeReportingApplication.main(new String[]{basePath + "wrong_json"});
    }

    @Test
    public void testNegativeUnits() throws IOException {
        expectedEx.expect(com.fasterxml.jackson.databind.exc.InvalidDefinitionException.class);
        expectedEx.expectMessage("Cannot construct instance of `com.vasylenkov.java.jpm.Instruction`, problem: Units should not be negative");
        TradeReportingApplication.main(new String[]{basePath + "negative_units"});
    }

    @Test
    public void testNegativeFx() throws IOException {
        expectedEx.expect(com.fasterxml.jackson.databind.exc.InvalidDefinitionException.class);
        expectedEx.expectMessage("Cannot construct instance of `com.vasylenkov.java.jpm.Instruction`, problem: AgreedFx should not be negative");
        TradeReportingApplication.main(new String[]{basePath + "negative_fx"});
    }

    @Test
    public void testNegativePricePerUnit() throws IOException {
        expectedEx.expect(com.fasterxml.jackson.databind.exc.InvalidDefinitionException.class);
        expectedEx.expectMessage("Cannot construct instance of `com.vasylenkov.java.jpm.Instruction`, problem: Price per Unit should not be negative");
        TradeReportingApplication.main(new String[]{basePath + "negative_price_per_unit"});
    }

    @Test
    public void testUnexpectedAction() throws IOException {
        expectedEx.expect(com.fasterxml.jackson.databind.exc.InvalidDefinitionException.class);
        expectedEx.expectMessage("Cannot construct instance of `com.vasylenkov.java.jpm.Instruction`, problem: Unexpected Action");
        TradeReportingApplication.main(new String[]{basePath + "unexpected_action"});
    }


    @Test
    public void testMissedAttribute() throws IOException {
        expectedEx.expect(com.fasterxml.jackson.databind.exc.MismatchedInputException.class);
        expectedEx.expectMessage("Missing required creator property 'entity' (index 0)");
        TradeReportingApplication.main(new String[]{basePath + "missed_attribute"});
    }

    @Test
    public void testDuplicateAttribute() throws IOException {
        expectedEx.expect(com.fasterxml.jackson.databind.JsonMappingException.class);
        expectedEx.expectMessage("Duplicate field 'agreedFx'");
        TradeReportingApplication.main(new String[]{basePath + "duplicate_attribute"});
    }

    @Test
    public void testSettlementBeforeInstructionDate() throws IOException {
        expectedEx.expect(com.fasterxml.jackson.databind.exc.InvalidDefinitionException.class);
        expectedEx.expectMessage("Cannot construct instance of `com.vasylenkov.java.jpm.Instruction`, problem: Settlement Date date should be after Instruction Date");
        TradeReportingApplication.main(new String[]{basePath + "settlement_before_instruction_date"});
    }

    @Test
    public void testGulfCalendar()  throws IOException {
        String expected =
                ("--- Daily Report ---\r\n" +
                "2018-01-28:\r\n" +
                "S: $   13875.000\r\n" +
                "\r\n" +
                "--- Rank Report ---\r\n" +
                "S:\r\n" +
                "  1 3I GRP.                $   13875.000").trim();

        TradeReportingApplication.main(new String[]{basePath + "gulf_calendar"});

        assertEquals(expected, outContent.toString().trim());

    }

}