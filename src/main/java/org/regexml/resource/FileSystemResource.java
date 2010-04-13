package org.regexml.resource;

import org.regexml.exception.ExpressionFileNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * Resource implementation that reads configuration files from the classpath.
 *
 * @author Dustin R. Callaway
 */
public class FileSystemResource implements Resource
{
    File file;

    /**
     * Constructs a new FileSystemResource object.
     *
     * @param name Name of file containing expressions in XML
     */
    public FileSystemResource(String name)
    {
        this(new File(name));
    }

    /**
     * Constructs a new FileSystemResource object.
     *
     * @param file File containing expressions in XML
     */
    public FileSystemResource(File file)
    {
        if (file.exists() && file.isFile())
        {
            this.file = file;
        }
    }

    /**
     * Returns the name of the file represented by this resource.
     *
     * @return Name of file abstracted by this resource
     */
    public String getName()
    {
        return file.getName();
    }

    /**
     * Returns a reader object for the file system resource.
     *
     * @return Reader for the file containing expressions in XML
     * @throws ExpressionFileNotFoundException
     */
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
            throw new ExpressionFileNotFoundException("File not found: " + file.getAbsolutePath());
        }
    }
}
