package org.craftsmenlabs.gareth.api.definition;

import org.craftsmenlabs.gareth.api.exception.GarethDefinitionParseException;
import org.craftsmenlabs.gareth.api.exception.GarethExperimentParseException;


public interface ParsedDefinitionFactory {


    /**
     * Parse definitions into a parsed definition
     *
     * @param clazz The class that should be initialized
     * @throws GarethExperimentParseException
     */
    ParsedDefinition parse(final Class clazz) throws GarethDefinitionParseException;


}
