package org.regexml.exception;

/**
 * Exception indicating that the requested expression ID was not found.
 */
public class ExpressionNotFoundException extends RuntimeException
{
    /**
     * Constructs a new expression not found exception.
     *
     * @param message Error message
     */
    public ExpressionNotFoundException(String message)
    {
        super(message);
    }
}
