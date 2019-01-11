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

package au.csiro.spiatofhir.fhir;

import au.csiro.spiatofhir.spia.HasRefsetEntries;
import org.hl7.fhir.dstu3.model.ConceptMap;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.UriType;

/**
 * @author John Grimes
 */
public class ImmunopathologyUnitMap {

    private final HasRefsetEntries refset;
    private ConceptMap conceptMap;

    public ImmunopathologyUnitMap(HasRefsetEntries refset) {
        this.refset = refset;
        buildConceptMap();
    }

    private void buildConceptMap() {
        conceptMap = new ConceptMap();
        conceptMap.setId("spia-immunopathology-unit-map-1");
        conceptMap.setUrl("https://www.rcpa.edu.au/fhir/ConceptMap/spia-immunopathology-unit-map-1");
        conceptMap.setVersion("1.0.0");
        Identifier oid = new Identifier();
        oid.setSystem("urn:ietf:rfc:3986");
        // TODO: Add a real OID.
        oid.setValue("urn:oid:TBD");
        conceptMap.setIdentifier(oid);
        conceptMap.setName("RCPA - SPIA Immunopathology Unit Map");
        conceptMap.setTitle("RCPA - SPIA Immunopathology Unit Map");
        conceptMap.setDescription(
                "Map between the SPIA Immunopathology Reference Set (v3.0) and the corresponding RCPA preferred units" +
                        " (v1.0) for each code.");
        conceptMap.setPurpose("Resolving RCPA specified units for members of the SPIA Immunopathology Reference " +
                                      "Set.");
        SpiaFhirConceptMap.addCommonElementsToConceptMap(conceptMap);
        conceptMap.getText().getDiv().addText("RCPA - SPIA Immunopathology Unit Map");
        conceptMap.setSource(new UriType("https://www.rcpa.edu.au/fhir/ValueSet/spia-immunopathology-refset-1"));
        conceptMap.setTarget(new UriType("https://www.rcpa.edu.au/fhir/ValueSet/spia-preferred-units-refset-1"));
        ConceptMap.ConceptMapGroupComponent group = SpiaFhirConceptMap.buildGroupFromEntries(refset.getRefsetEntries());
        group.setSource("http://loinc.org");
        group.setTarget("http://unitsofmeasure.org");
        conceptMap.getGroup().add(group);
    }

    public ConceptMap getConceptMap() {
        return conceptMap;
    }

}
