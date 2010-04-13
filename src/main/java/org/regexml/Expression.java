package org.regexml;

import java.util.regex.Pattern;

/**
 * Represents a regexml expression.
 */
public class Expression
{
    private String regExString;
    private Pattern pattern;

    /**
     * Constructs a new expression object.
     *
     * @param regExString Regular expression string
     * @param pattern Regular expression pattern
     */
    public Expression(String regExString, Pattern pattern)
    {
        this.regExString = regExString;
        this.pattern = pattern;
    }

    /**
     * Gets the regular expression string.
     *
     * @return Regular expression string
     */
    public String getRegExString()
    {
        return regExString;
    }

    /**
     * Gets the regular expression pattern for this expression.
     *
     * @return Regular expression pattern
     */
    public Pattern getPattern()
    {
        return pattern;
    }
}
