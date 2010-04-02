package org.regexml.resource;

import org.regexml.exception.ExpressionFileNotFoundException;
import org.regexml.resource.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: dcallaway
 * Date: Apr 1, 2010
 * Time: 3:28:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileSystemResource implements Resource
{
    File file;

    public FileSystemResource(String name)
    {
        this(new File(name));
    }

    public FileSystemResource(File file)
    {
        if (file.exists() && file.isFile())
        {
            this.file = file;
        }
    }

    public Reader getReader() throws ExpressionFileNotFoundException
    {
        if (file == null)
        {
            return null;
        }

        try
        {
            return new FileReader(file);
        }
        catch (FileNotFoundException e)
        {
            throw new ExpressionFileNotFoundException();
        }
    }
}
