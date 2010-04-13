package org.regexml.exception;

/**
 * Exception indicating that the expressions file was invalid according to the schema.
 */
public class SchemaValidationException extends RuntimeException
{
    /**
     * Constructs a new schema validation exception.
     *
     * @param message Error message
     */
    public SchemaValidationException(String message)
    {
        this(message, null);
    }

    /**
     * Constructs a new schema validation exception.
     *
     * @param message Error message
     * @param e Exception
     */
    public SchemaValidationException(String message, Throwable e)
    {
        super(message, e);
    }
}
