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

import static org.hl7.fhir.dstu3.model.Bundle.BundleType.BATCH;
import static org.hl7.fhir.dstu3.model.Bundle.HTTPVerb.POST;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for interacting with a FHIR terminology server.
 *
 * @author John Grimes
 */
public class TerminologyClient {

  private static final Logger logger = LoggerFactory.getLogger(TerminologyClient.class);
  private final IGenericClient client;

  public TerminologyClient(FhirContext fhirContext, String serverBase) {
    fhirContext.getRestfulClientFactory().setSocketTimeout(30 * 60 * 1000);
    client = fhirContext.newRestfulGenericClient(serverBase);
  }

  public Parameters lookup(String system, String code, List<String> properties) {
    long start = System.nanoTime();

    Parameters inParams = new Parameters();
    inParams.addParameter().setName("system").setValue(new UriType(system));
    inParams.addParameter().setName("code").setValue(new CodeType(code));
    for (String property : properties) {
      inParams.addParameter().setName("property").setValue(new StringType(property));
    }
    Parameters outParams = client.operation()
        .onType(CodeSystem.class)
        .named("$lookup")
        .withParameters(inParams)
        .execute();

    double elapsedMs = TimeUnit.MILLISECONDS
        .convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    logger.debug(
        "Executed lookup of " + system + "|" + code + " in " + String.format("%.1f", elapsedMs)
            + " ms");
    return outParams;
  }

  /**
   * Executes lookup operations for a set of codes within a specified CodeSystem, then returns a
   * bundle containing the results of those operations.
   */
  public Bundle batchLookup(String system, List<String> codes, List<String> properties) {
    long start = System.nanoTime();
    Bundle bundle = new Bundle();
    bundle.setType(BATCH);
    for (String code : codes) {
      Parameters inParams = new Parameters();
      inParams.addParameter().setName("system").setValue(new UriType(system));
      inParams.addParameter().setName("code").setValue(new CodeType(code));
      for (String property : properties) {
        inParams.addParameter().setName("property").setValue(new StringType(property));
      }
      bundle.addEntry()
          .setResource(inParams)
          .getRequest()
          .setMethod(POST)
          .setUrl("CodeSystem/$lookup");
    }
    Bundle result = client
        .transaction()
        .withBundle(bundle)
        .execute();
    double elapsedMs = TimeUnit.MILLISECONDS
        .convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    logger.debug(
        "Executed batch lookup of " + codes.size() + " codes from " + system + " CodeSystem in " +
            String.format("%.1f", elapsedMs) + " ms");
    return result;
  }

  /**
   * Validates a Bundle of resources against their declared profiles, returning a Bundle of
   * OperationOutcome resources.
   */
  public OperationOutcome validateBundle(Bundle bundle) {
    long start = System.nanoTime();
    OperationOutcome result = (OperationOutcome) client.validate().resource(bundle).execute()
        .getOperationOutcome();
    double elapsedMs = TimeUnit.MILLISECONDS
        .convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    logger.debug(
        "Executed batch validation of " + bundle.getEntry().size() + " resources in " +
            String.format("%.1f", elapsedMs) + " ms");
    return result;
  }
}
