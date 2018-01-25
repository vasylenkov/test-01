package com.vasylenkov.java.jpm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public class Instruction {

    private String entity;
    private String action;
    private BigDecimal agreedFx;
    private String currency;
    private LocalDate instructionDate;
    private LocalDate settlementDate;
    private BigDecimal units;
    private BigDecimal pricePerUnit;

    @JsonCreator
    public Instruction(
            @JsonProperty(required = true, value = "entity") String entity,
            @JsonProperty(required = true, value = "action") String action,
            @JsonProperty(required = true, value = "agreedFx") BigDecimal agreedFx,
            @JsonProperty(required = true, value = "currency") String currency,
            @JsonProperty(required = true, value = "instructionDate") LocalDate instructionDate,
            @JsonProperty(required = true, value = "settlementDate") LocalDate settlementDate,
            @JsonProperty(required = true, value = "units") BigDecimal units,
            @JsonProperty(required = true, value = "pricePerUnit") BigDecimal pricePerUnit
    ) {
        if (settlementDate.isBefore(instructionDate)) throw new IllegalArgumentException("Settlement Date date should be after Instruction Date");
        if (!(action.equals("S") || action.equals("B"))) throw new IllegalArgumentException("Unexpected Action");
        if (agreedFx.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("AgreedFx should not be negative");
        if (units.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Units should not be negative");
        if (pricePerUnit.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price per Unit should not be negative");

        this.entity = requireNonNull(entity);
        this.action = requireNonNull(action);
        this.agreedFx = requireNonNull(agreedFx);
        this.currency = requireNonNull(currency);
        this.instructionDate = requireNonNull(instructionDate);
        this.settlementDate = requireNonNull(settlementDate);
        this.units = requireNonNull(units);
        this.pricePerUnit = requireNonNull(pricePerUnit);
    }

    public String getEntity() { return entity; }

    public String getAction() {
        return action;
    }

    public BigDecimal getAgreedFx() {
        return agreedFx;
    }

    public String getCurrency() { return currency; }

    public LocalDate getInstructionDate() {
        return instructionDate;
    }

    /**
     *  This method allows to correct the settlement date according to working calendar

     * @return
     *  A work week starts Monday and ends Friday, unless the currency of the trade is AED or SAR, where
     *  the work week starts Sunday and ends Thursday. No other holidays to be taken into account.
     *  A trade can only be settled on a working day.
     *  If an instructed settlement date falls on a weekend, then the settlement date should be changed to
     *  the next working day.
     */
    public LocalDate getSettlementDate() {

        boolean gulfWeekdays = false;

        if (this.getCurrency().equals("AED") || this.getCurrency().equals("SAR")) {
            gulfWeekdays = true;
        }

        switch (settlementDate.getDayOfWeek()) {
            case FRIDAY:
                if (gulfWeekdays) {
                    settlementDate = settlementDate.plusDays(2);
                }
                break;

            case SATURDAY:
                if (gulfWeekdays) {
                    settlementDate = settlementDate.plusDays(1);
                } else {
                    settlementDate = settlementDate.plusDays(2);
                }
                break;

            case SUNDAY:
                if (!gulfWeekdays) {
                    settlementDate = settlementDate.plusDays(1);
                }
                break;
        }

        return settlementDate;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    /**
     *
     * @return USD amount of a trade = Price per unit * Units * Agreed Fx
     */
    public BigDecimal getCost() {
        return pricePerUnit.multiply(units).multiply(agreedFx);
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "entity='" + entity + '\'' +
                ", action='" + action + '\'' +
                ", agreedFx=" + agreedFx +
                ", currency='" + currency + '\'' +
                ", instructionDate=" + instructionDate +
                ", settlementDate=" + settlementDate +
                ", units=" + units +
                ", pricePerUnit=" + pricePerUnit +
                '}';
    }
}