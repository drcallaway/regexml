package org.regexml.resource;

import org.regexml.exception.ExpressionFileNotFoundException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Resource implementation that reads configuration files from the classpath.
 *
 * @author Dustin R. Callaway
 */
public class ClassPathResource implements Resource
{
    String name;

    /**
     * Constructs a new ClassPathResource object.
     *
     * @param name Name of file in classpath containing expressions in XML
     */
    public ClassPathResource(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of the file represented by this resource.
     *
     * @return Name of file abstracted by this resource
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns a reader object for the classpath resource.
     *
     * @return Reader for the file containing expressions in XML
     * @throws ExpressionFileNotFoundException
     */
    public Reader getReader() throws ExpressionFileNotFoundException
    {
        InputStream is = this.getClass().getResourceAsStream(name);

        if (is == null)
        {
            is = this.getClass().getClassLoader().getResourceAsStream(name);
        }

        if (is == null)
        {
            throw new ExpressionFileNotFoundException("File not found in classpath: " + name);
        }

        return new InputStreamReader(is);
    }
}
