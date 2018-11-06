/*
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to
 * license terms and conditions.
 */
package au.csiro.spiatofhir.spia;

import org.apache.poi.ss.usermodel.*;

import java.util.*;

/**
 * @author John Grimes
 */
public class ChemicalPathologyRefset extends Refset implements HasRefsetEntries {

    protected static final String[] expectedHeaders =
            {"RCPA Preferred term", "RCPA Synonyms", "Usage guidance", "Length", "Specimen", "Unit", "UCUM", "LOINC",
             "Component", "Property", "Timing", "System", "Scale", "Method", "LongName", "Combining Results Flag",
             "Version", "History"};
    private static final String SHEET_NAME = "Terminology for Chem Pathology";
    private static final Map<String, ChemicalPathologyRefsetEntry.CombiningResultsFlag> combiningResultsFlagMap =
            Collections.unmodifiableMap(new HashMap<String, ChemicalPathologyRefsetEntry.CombiningResultsFlag>() {
                {
                    put("Red", ChemicalPathologyRefsetEntry.CombiningResultsFlag.RED);
                    put("Green", ChemicalPathologyRefsetEntry.CombiningResultsFlag.GREEN);
                    put("Orange", ChemicalPathologyRefsetEntry.CombiningResultsFlag.ORANGE);
                }
            });
    private Workbook workbook;
    private Sheet sheet;
    private List<RefsetEntry> refsetEntries;

    /**
     * Creates a new reference set, based on the contents of the supplied workbook.
     *
     * @param workbook
     * @throws ValidationException
     */
    public ChemicalPathologyRefset(Workbook workbook) throws ValidationException {
        this.workbook = workbook;
        parse();
    }

    /**
     * Gets a list of all entries within this reference set.
     *
     * @return
     */
    @Override
    public List<RefsetEntry> getRefsetEntries() {
        return refsetEntries;
    }

    private void parse() throws ValidationException {
        sheet = workbook.getSheet(SHEET_NAME);
        refsetEntries = new ArrayList<>();
        for (Row row : sheet) {
            // Check that header row matches expectations.
            if (row.getRowNum() == 0) {
                validateHeaderRow(row, expectedHeaders);
                continue;
            }
            // Skip heading row.
            if (row.getRowNum() == 172) continue;
            // This is necessary due to a formula being left at the end of this spreadsheet (230, 15).
            if (row.getRowNum() > 221) continue;

            ChemicalPathologyRefsetEntry refsetEntry = new ChemicalPathologyRefsetEntry();
            // Extract information from row.
            Optional<String> rcpaPreferredTerm = getStringValueFromCell(row, 0);
            Optional<String> rcpaSynonymsRaw = getStringValueFromCell(row, 1);
            Set<String> rcpaSynonyms = new HashSet<>();
            if (rcpaSynonymsRaw.isPresent()) {
                Arrays.asList(rcpaSynonymsRaw.get().split(";")).stream().forEach(s -> rcpaSynonyms.add(s.trim()));
            }
            Optional<String> usageGuidance = getStringValueFromCell(row, 2);
            // Length has been omitted, as formulas are being used within the spreadsheet.
            Optional<String> specimen = getStringValueFromCell(row, 4);
            Optional<String> unit = getStringValueFromCell(row, 5);
            Optional<String> ucum = getStringValueFromCell(row, 6);
            Optional<String> loincCode = getStringValueFromCell(row, 7);
            Optional<String> loincComponent = getStringValueFromCell(row, 8);
            Optional<String> loincProperty = getStringValueFromCell(row, 9);
            Optional<String> loincTiming = getStringValueFromCell(row, 10);
            Optional<String> loincSystem = getStringValueFromCell(row, 11);
            Optional<String> loincScale = getStringValueFromCell(row, 12);
            Optional<String> loincMethod = getStringValueFromCell(row, 13);
            Optional<String> loincLongName = getStringValueFromCell(row, 14);
            Optional<ChemicalPathologyRefsetEntry.CombiningResultsFlag> combiningResultsFlag =
                    getCombiningResultsFlagFromCell(row, 15);
            Optional<Double> version = getNumericValueFromCell(row, 16);
            Optional<String> history = getStringValueFromCell(row, 17);

            // Populate information into ChemicalPathologyRefsetEntry object.
            refsetEntry.setRcpaPreferredTerm(rcpaPreferredTerm);
            refsetEntry.setRcpaSynonyms(rcpaSynonyms);
            refsetEntry.setUsageGuidance(usageGuidance);
            refsetEntry.setSpecimen(specimen);
            refsetEntry.setUnit(unit);
            refsetEntry.setUcum(ucum);
            refsetEntry.setCode(loincCode);
            refsetEntry.setLoincComponent(loincComponent);
            refsetEntry.setLoincProperty(loincProperty);
            refsetEntry.setLoincTiming(loincTiming);
            refsetEntry.setLoincSystem(loincSystem);
            refsetEntry.setLoincScale(loincScale);
            refsetEntry.setLoincMethod(loincMethod);
            refsetEntry.setLoincLongName(loincLongName);
            refsetEntry.setVersion(version);
            refsetEntry.setHistory(history);

            // Add ChemicalPathologyRefsetEntry object to list.
            refsetEntries.add(refsetEntry);
        }
    }

    private Optional<ChemicalPathologyRefsetEntry.CombiningResultsFlag> getCombiningResultsFlagFromCell(Row row,
                                                                                                        int cellNumber)
            throws ValidationException {
        Cell cell = row.getCell(cellNumber, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return Optional.empty();
        if (cell.getCellTypeEnum() != CellType.STRING)
            throw new CellValidationException(
                    "Cell identified for extraction of Combining Results Flag is not of string type, actual type: " +
                            cell.getCellTypeEnum().toString(), cell.getRowIndex(), cell.getColumnIndex());
        if (!combiningResultsFlagMap.containsKey(cell.getStringCellValue()))
            throw new ValidationException(
                    "Unexpected value encountered in Combining Results Flag column: " + cell.getStringCellValue());
        return Optional.of(combiningResultsFlagMap.get(cell.getStringCellValue()));
    }

}
