package org.regexml.exception;

/**
 * Exception indicating that the specified expressions file was not found.
 */
public class ExpressionFileNotFoundException extends RuntimeException
{
    public ExpressionFileNotFoundException(String message)
    {
        super(message);
    }
}
