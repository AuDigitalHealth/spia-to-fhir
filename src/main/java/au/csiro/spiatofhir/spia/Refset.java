/*
 *    Copyright 2018 Australian e-Health Research Centre, CSIRO
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package au.csiro.spiatofhir.spia;

import au.csiro.spiatofhir.fhir.TerminologyClient;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author John Grimes
 */
public abstract class Refset {

    /**
     * Returns a list of display terms corresponding to the supplied list of reference set entries, sourced using the
     * supplied terminology server client.
     * <p>
     * If there are any problems looking up a particular code, a null will be added to the list that is returned.
     */
    protected static List<String> lookupDisplayTerms(TerminologyClient terminologyClient, String system,
                                                     List<RefsetEntry> refsetEntries) {
        List<String> codes = refsetEntries.stream().map(RefsetEntry::getCode).collect(Collectors.toList());
        Bundle result = terminologyClient.batchLookup(system, codes, new ArrayList<>());
        return result.getEntry()
                     .stream()
                     .map(Refset::entryToParameters)
                     .map(Refset::parametersToDisplayValue)
                     .collect(Collectors.toList());
    }

    /**
     * Uses the supplied terminology server client to look up display terms for LOINC reference set entries that have
     * UCUM codes.
     */
    protected static void addUcumDisplays(TerminologyClient terminologyClient,
                                          List<RefsetEntry> refsetEntries) {
        List<String> codes = refsetEntries.stream()
                                          .map(refsetEntry -> ((LoincRefsetEntry) refsetEntry).getUcumCode())
                                          .collect(Collectors.toList());
        Bundle result = terminologyClient.batchLookup("http://unitsofmeasure.org", codes, new ArrayList<>());
        List<String> displays = result.getEntry()
                                      .stream()
                                      .map(Refset::entryToParameters)
                                      .map(Refset::parametersToDisplayValue)
                                      .collect(Collectors.toList());
        for (int i = 0; i < refsetEntries.size(); i++) {
            LoincRefsetEntry refsetEntry = (LoincRefsetEntry) refsetEntries.get(i);
            if (displays.get(i) != null) refsetEntry.setUcumDisplay(displays.get(i));
        }
    }

    private static Parameters entryToParameters(Bundle.BundleEntryComponent entry) {
        return entry.getResource().fhirType().equals("Parameters")
               ? (Parameters) entry.getResource()
               : null;
    }

    private static String parametersToDisplayValue(Parameters parameters) {
        return parameters != null ? parameters.getParameter()
                                              .stream()
                                              .filter(parameter -> parameter.getName().equals("display"))
                                              .map(parameter -> parameter.getValue().toString())
                                              .findFirst()
                                              .orElse(null) : null;
    }

    protected void validateHeaderRow(Row row, String[] expectedHeaders) throws ValidationException {
        ArrayList<String> headerValues = new ArrayList<>();
        for (Cell cell : row) {
            headerValues.add(cell.getStringCellValue());
        }
        if (!Arrays.equals(headerValues.toArray(), expectedHeaders))
            throw new ValidationException("Header values do not match expected values.");
    }

    protected String getStringValueFromCell(Row row, int cellNumber) throws ValidationException {
        Cell cell = row.getCell(cellNumber, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        if (cell.getCellTypeEnum() != CellType.STRING)
            throw new CellValidationException("Cell identified for extraction of string value is not of string type, " +
                                                      "actual type: " + cell.getCellTypeEnum().toString(),
                                              cell.getRowIndex(), cell.getColumnIndex());
        return cell.getStringCellValue().trim();
    }

    protected Double getNumericValueFromCell(Row row, int cellNumber) throws ValidationException {
        Cell cell = row.getCell(cellNumber, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        if (cell.getCellTypeEnum() != CellType.NUMERIC)
            throw new CellValidationException("Cell identified for extraction of numeric value is not of numeric " +
                                                      "type, actual type: " + cell.getCellTypeEnum().toString(),
                                              cell.getRowIndex(), cell.getColumnIndex());
        double value = cell.getNumericCellValue();
        if (value == 0) return null;
        else return value;
    }

}
