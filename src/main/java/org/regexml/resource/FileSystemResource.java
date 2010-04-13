/*
 * Copyright (c) 2010 Dustin R. Callaway
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
