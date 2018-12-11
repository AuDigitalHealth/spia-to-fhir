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
import org.hl7.fhir.dstu3.model.ValueSet;

/**
 * @author John Grimes
 */
public class ImmunopathologyValueSet implements SpiaFhirValueSet {

    private HasRefsetEntries refset;
    private ValueSet valueSet;

    public ImmunopathologyValueSet(HasRefsetEntries refset) {
        this.refset = refset;
        buildValueSet();
    }

    private void buildValueSet() {
        valueSet = new ValueSet();
        valueSet.setId("spia-immunopathology-refset-3");
        valueSet.setUrl("https://www.rcpa.edu.au/fhir/ValueSet/spia-immunopathology-refset-3");
        valueSet.setVersion("3.0");
        valueSet.setName("RCPA - SPIA Immunopathology Terminology Reference Set v3.0");
        valueSet.setTitle("RCPA - SPIA Immunopathology Terminology Reference Set v3.0");
        SpiaFhirValueSet.addCommonElementsToValueSet(valueSet);
        ValueSet.ValueSetComposeComponent compose = SpiaFhirValueSet.buildComposeFromEntries(refset.getRefsetEntries(),
                                                                                             "http://loinc.org");
        valueSet.setCompose(compose);
    }

    @Override
    public ValueSet getValueSet() {
        return valueSet;
    }

}
