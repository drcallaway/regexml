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
package org.regexml;

import java.util.regex.Pattern;

/**
 * Represents a single regexml expression.
 */
public class Expression
{
    private String id;
    private Pattern pattern;
    private String regExString;

    /**
     * Constructs a new expression object.
     *
     * @param id Expression ID
     * @param regExString Regular expression string
     * @param pattern Regular expression pattern
     */
    public Expression(String id, String regExString, Pattern pattern)
    {
        this.id = id;
        this.regExString = regExString;
        this.pattern = pattern;
    }

    /**
     * Gets the ID for this expression.
     *
     * @return Expression ID
     */
    public String getId()
    {
        return id;
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

    /**
     * Gets the expression in traditional regular expression syntax.
     *
     * @return Regular expression string
     */
    public String getRegExString()
    {
        return regExString;
    }
}
