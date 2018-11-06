/*
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to
 * license terms and conditions.
 */
package au.csiro.spiatofhir.spia;

import java.util.Optional;
import java.util.Set;

/**
 * @author John Grimes
 */
public interface RefsetEntry {

    public Optional<String> getRcpaPreferredTerm();

    public Set<String> getRcpaSynonyms();

    public Optional<String> getCode();

}
