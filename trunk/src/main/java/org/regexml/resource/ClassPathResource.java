package org.regexml.resource;

import org.regexml.exception.ExpressionFileNotFoundException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Resource implementation that reads configuration files from the classpath.
 */
public class ClassPathResource implements Resource
{
    String name;

    public ClassPathResource(String name)
    {
        this.name = name;
    }

    public Reader getReader() throws ExpressionFileNotFoundException
    {
        InputStream is = this.getClass().getResourceAsStream(name);

        if (is == null)
        {
            is = this.getClass().getClassLoader().getResourceAsStream(name);
        }

        if (is == null)
        {
            throw new ExpressionFileNotFoundException();
        }

        return new InputStreamReader(is);
    }
}
