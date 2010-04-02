package org.regexml.resource;

import org.regexml.exception.ExpressionFileNotFoundException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: dcallaway
 * Date: Apr 1, 2010
 * Time: 3:29:03 PM
 * To change this template use File | Settings | File Templates.
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
