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

package au.csiro.spiatofhir.fhir;

import au.csiro.spiatofhir.loinc.Loinc;
import au.csiro.spiatofhir.spia.Refset;
import au.csiro.spiatofhir.utils.Strings;
import java.util.Date;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.UriType;

/**
 * @author John Grimes
 */
public class ChemicalCombiningResultsMap extends SpiaFhirConceptMap {

  private static final String NAME = "spia-chemical-combining-results-map";
  private static final String VERSION = "2.0.0";
  public static final String URL = "https://www.rcpa.edu.au/fhir/ConceptMap/" + NAME + "-" + Strings
      .majorVersionFromSemVer(VERSION);
  private static final String OID = "1.2.36.1.2001.1004.300.100.1003";

  @Override
  public Resource transform(Refset refset, Date publicationDate) {
    ConceptMap conceptMap = new ConceptMap();
    conceptMap.setVersion(VERSION);
    conceptMap.setId(NAME + "-" + Strings.majorVersionFromSemVer(VERSION));
    conceptMap.setUrl(URL);
    Identifier oid = new Identifier();
    oid.setSystem("urn:ietf:rfc:3986");
    oid.setValue("urn:oid:" + OID);
    conceptMap.setIdentifier(oid);
    conceptMap.setTitle("RCPA - SPIA Chemical Combining Results Map");
    conceptMap.setName(NAME);
    conceptMap
        .setDescription("Map between the SPIA Chemical Pathology Reference Set (v3.1) and the " +
            "corresponding combining results flag for each code.");
    conceptMap.setPurpose(
        "Resolving the combining results flags for members of the SPIA Chemical Pathology "
            + "Reference Set.");
    conceptMap.setDate(publicationDate);
    SpiaFhirConceptMap.addCommonElementsToConceptMap(conceptMap);
    conceptMap.setSource(
        new UriType(ChemicalPathologyValueSet.URL));
    conceptMap.setTarget(
        new UriType("https://www.rcpa.edu.au/fhir/ValueSet/spia-combining-results-flag"));
    ConceptMap.ConceptMapGroupComponent group = SpiaFhirConceptMap
        .buildCombiningResultsFlagsGroupFromEntries(refset.getRefsetEntries());
    group.setSource(Loinc.SYSTEM_URI);
    group.setTarget("https://www.rcpa.edu.au/fhir/CodeSystem/spia-combining-results-flag");
    conceptMap.getGroup().add(group);

    return conceptMap;
  }

}
