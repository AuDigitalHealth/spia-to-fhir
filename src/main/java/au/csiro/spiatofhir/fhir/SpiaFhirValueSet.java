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

import au.csiro.spiatofhir.spia.RefsetEntry;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author John Grimes
 */
public interface SpiaFhirValueSet {

    static void addCommonElementsToValueSet(ValueSet valueSet) {
        Meta meta = new Meta();
        List<UriType> profile = new ArrayList<>();
        profile.add(new UriType("http://hl7.org/fhir/StructureDefinition/shareablevalueset"));
        profile.add(new UriType("https://healthterminologies.gov.au/fhir/StructureDefinition/composed-value-set-2"));
        meta.setProfile(profile);
        valueSet.setMeta(meta);
        Narrative text = new Narrative();
        text.setStatus(Narrative.NarrativeStatus.EMPTY);
        XhtmlNode div = new XhtmlNode(NodeType.Element, "div");
        text.setDiv(div);
        valueSet.setText(text);
        valueSet.setStatus(Enumerations.PublicationStatus.DRAFT);
        valueSet.setExperimental(true);
        valueSet.setDate(new Date());
        valueSet.setPublisher("Australian Digital Health Agency");
        valueSet.setCopyright(
                "Copyright © 2019 Australian Digital Health Agency - All rights reserved. This content is licensed " +
                        "under a Creative Commons Attribution 4.0 International License. See https://creativecommons" +
                        ".org/licenses/by/4.0/.");
        List<ContactDetail> contact = new ArrayList<>();
        ContactDetail contactDetail = new ContactDetail();
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.EMAIL);
        contactPoint.setValue("help@digitalhealth.gov.au");
        contactDetail.addTelecom(contactPoint);
        contact.add(contactDetail);
        valueSet.setContact(contact);
        List<CodeableConcept> jurisdiction = new ArrayList<>();
        CodeableConcept jurisdictionCodeableConcept = new CodeableConcept();
        Coding jurisdictionCoding = new Coding();
        jurisdictionCoding.setSystem("urn:iso:std:iso:3166");
        jurisdictionCoding.setCode("AU");
        jurisdictionCoding.setDisplay("Australia");
        jurisdictionCodeableConcept.addCoding(jurisdictionCoding);
        jurisdiction.add(jurisdictionCodeableConcept);
        valueSet.setJurisdiction(jurisdiction);
    }

    static ValueSet.ValueSetComposeComponent buildComposeFromEntries(List<RefsetEntry> refsetEntries, String system) {
        ValueSet.ValueSetComposeComponent compose = new ValueSet.ValueSetComposeComponent();
        List<ValueSet.ConceptSetComponent> include = new ArrayList<>();
        ValueSet.ConceptSetComponent includeEntry = new ValueSet.ConceptSetComponent();
        List<ValueSet.ConceptReferenceComponent> concept = new ArrayList<>();

        for (RefsetEntry entry : refsetEntries) {
            if (entry.getCode() != null) {
                includeEntry.setSystem(system);
                ValueSet.ConceptReferenceComponent conceptEntry = new ValueSet.ConceptReferenceComponent();
                conceptEntry.setCode(entry.getCode());
                conceptEntry.setDisplay(entry.getNativeDisplay());

                if (entry.getRcpaPreferredTerm() != null || !entry.getRcpaSynonyms().isEmpty()) {
                    List<ValueSet.ConceptReferenceDesignationComponent> designation = new ArrayList<>();
                    if (entry.getRcpaPreferredTerm() != null) {
                        designation.add(buildPreferredTermDesignation(entry));
                    }
                    if (!entry.getRcpaSynonyms().isEmpty()) {
                        designation.addAll(buildSynonymDesignations(entry));
                    }
                    conceptEntry.setDesignation(designation);
                }
                concept.add(conceptEntry);
            }
        }
        includeEntry.setConcept(concept);
        include.add(includeEntry);
        compose.setInclude(include);

        return compose;
    }

    static ValueSet.ConceptReferenceDesignationComponent buildPreferredTermDesignation(RefsetEntry entry) {
        ValueSet.ConceptReferenceDesignationComponent designationEntry =
                new ValueSet.ConceptReferenceDesignationComponent();
        designationEntry.setValue(entry.getRcpaPreferredTerm());
        Coding designationUse =
                new Coding("https://www.rcpa.edu.au/fhir/CodeSystem/spia-designation-type",
                           "preferred-term",
                           "RCPA Preferred Term");
        designationEntry.setUse(designationUse);
        return designationEntry;
    }

    static List<ValueSet.ConceptReferenceDesignationComponent> buildSynonymDesignations(RefsetEntry entry) {
        List<ValueSet.ConceptReferenceDesignationComponent> designationEntries = new ArrayList<>();
        for (String rcpaSynonym : entry.getRcpaSynonyms()) {
            ValueSet.ConceptReferenceDesignationComponent designationEntry =
                    new ValueSet.ConceptReferenceDesignationComponent();
            designationEntry.setValue(rcpaSynonym);
            Coding designationUse = new Coding(
                    "https://www.rcpa.edu.au/fhir/CodeSystem/spia-designation-type",
                    "synonym",
                    "RCPA Synonym");
            designationEntry.setUse(designationUse);
            designationEntries.add(designationEntry);
        }
        return designationEntries;
    }

    ValueSet getValueSet();

}
