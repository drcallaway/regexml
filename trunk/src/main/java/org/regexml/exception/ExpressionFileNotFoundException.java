package org.regexml.exception;

/**
 * Exception indicating that the specified expressions file was not found.
 */
public class ExpressionFileNotFoundException extends RuntimeException
{
    /**
     * Constructs a new expression file not found exception.
     *
     * @param message Error message
     */
    public ExpressionFileNotFoundException(String message)
    {
        super(message);
    }
}
