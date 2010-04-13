package org.regexml.resource;

import java.io.Reader;

/**
 * Represents a resource file containing XML expressions.
 *
 * @author Dustin R. Callaway
 */
public interface Resource
{
    public String getName();
    public Reader getReader();
}
