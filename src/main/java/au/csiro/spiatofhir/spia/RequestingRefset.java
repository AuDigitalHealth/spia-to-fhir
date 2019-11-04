/*
 * Copyright 2019 Australian e-Health Research Centre, CSIRO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.csiro.spiatofhir.spia;

import au.csiro.spiatofhir.fhir.TerminologyClient;
import java.util.ArrayList;
import java.util.Set;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.fhir.ucum.UcumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Grimes
 */
public class RequestingRefset extends Refset {

  protected static final Logger logger = LoggerFactory.getLogger(RequestingRefset.class);
  protected static final String[] expectedHeaders =
      {"RCPA Preferred term", "RCPA Synonyms", "Usage guidance", "Length", "Discipline", "SNOMED ",
          "Subgroup", "SNOMED", "Specimen", "Terminology binding (SNOMED CT-AU)", "Version",
          "History"};
  private static final String SHEET_NAME = "SPIA Requesting terms v3.1";

  public RequestingRefset(Workbook workbook,
      TerminologyClient terminologyClient, UcumService ucumService)
      throws ValidationException {
    super(workbook, terminologyClient, ucumService);
  }

  @Override
  protected void parse() throws ValidationException {
    Sheet sheet = workbook.getSheet(SHEET_NAME);
    if (sheet == null) {
      throw new ValidationException("Sheet not found: " + SHEET_NAME);
    }
    refsetEntries = new ArrayList<>();
    for (Row row : sheet) {
      // Check that header row matches expectations.
      if (row.getRowNum() == 0) {
        validateHeaderRow(row, expectedHeaders);
        continue;
      }

      RefsetEntry refsetEntry = new RefsetEntry();

      // Extract information from row.
      String rcpaPreferredTerm = getStringValueFromCell(row, 0);
      Set<String> rcpaSynonyms = getDelimitedStringsFromCell(row, 1);
      String snomedCode;

      // Skip entire row if code is missing or invalid.
      try {
        snomedCode = getSnomedCodeFromCell(row, 9, terminologyClient);
      } catch (BlankCodeException | InvalidCodeException e) {
        logger.warn(e.getMessage());
        continue;
      }

      // Populate information into RefsetEntry object.
      refsetEntry.setRcpaPreferredTerm(rcpaPreferredTerm);
      refsetEntry.getRcpaSynonyms().addAll(rcpaSynonyms);
      refsetEntry.setCode(snomedCode);

      // Add RefsetEntry object to list.
      refsetEntries.add(refsetEntry);
    }
  }

}
