package org.regexml.exception;

/**
 * Exception indicating that the requested expression ID was not found.
 */
public class ExpressionNotFoundException extends RuntimeException
{
    public ExpressionNotFoundException(String message)
    {
        super(message);
    }
}
