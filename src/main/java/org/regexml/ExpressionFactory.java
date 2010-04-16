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

import org.regexml.exception.ExpressionNotFoundException;
import org.regexml.exception.SchemaValidationException;
import org.regexml.resource.ClassPathResource;
import org.regexml.resource.Resource;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Constructs regular expressions from an XML file. Note that this class is not thread safe. Use of a ThreadLocal
 * variable is recommended if this factory is to be called from multiple threads.
 */
public class ExpressionFactory
{
    private static final String[] autoEscapeChars = {"$", "(", ")", "*", "+", "?", "^", "{", "|"};

    private static final String SCHEMA_FILE_NAME = "regexml.xsd";
    private static final String ELEMENT_REGEXML = "regexml";
    private static final String ELEMENT_EXPRESSION = "expression";
    private static final String ELEMENT_START = "start";
    private static final String ELEMENT_END = "end";
    private static final String ELEMENT_MATCH = "match";
    private static final String ELEMENT_GROUP = "group";
    private static final String ATTR_AUTO_ESCAPE = "autoEscape";
    private static final String ATTR_ID = "id";
    private static final String ATTR_IGNORE_CASE = "ignoreCase";
    private static final String ATTR_DOT_MATCHES_LINE_BREAKS = "dotMatchesLineBreaks";
    private static final String ATTR_ANCHORS_MATCH_LINE_BREAKS = "anchorsMatchLineBreaks";
    private static final String ATTR_MATCH_LINE_BREAKS = "matchLineBreaks";
    private static final String ATTR_EQUALS = "equals";
    private static final String ATTR_EXCEPT = "except";
    private static final String ATTR_MIN = "min";
    private static final String ATTR_MAX = "max";
    private static final String ATTR_CAPTURE = "capture";
    private static final String ATTR_OPERATOR = "operator";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String OPERATOR_AND = "and";
    private static final String OPERATOR_OR = "or";

    private Map<String, Expression> expressionMap = new HashMap<String, Expression>();
    private XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private Stack<GroupData> groupStack = new Stack<GroupData>();
    private StringBuilder regExpression;
    private String expressionId;
    private boolean autoEscape = true;
    private boolean ignoreCase;
    private boolean dotMatchesLineBreaks;
    private boolean anchorsMatchLineBreaks;
    private boolean startAnchorMatchesLineBreaks;

    /**
     * Constructs an ExpressionFactory object.
     *
     * @param inputResource Resource referencing the file containing expressions in XML
     */
    public ExpressionFactory(Resource inputResource)
    {
        this(inputResource, false);
    }

    /**
     * Constructs an ExpressionFactory object.
     *
     * @param inputResource Resource referencing the file containing expressions in XML
     * @param validate Indicates whether or not the expressions file should be validated against the regexml schema
     */
    public ExpressionFactory(Resource inputResource, boolean validate)
    {
        if (validate)
        {
            validateDocument(inputResource);
        }

        processExpressions(inputResource.getReader());
    }

    /**
     * Validates the given document against the expressions schema.
     *
     * @param inputResource Document to validate
     * @throws SchemaValidationException Indicates that the given document was not valid
     */
    private void validateDocument(Resource inputResource) throws SchemaValidationException
    {
        Resource schemaResource = new ClassPathResource(SCHEMA_FILE_NAME);

        try
        {
            Validator validator = schemaFactory.newSchema(new StreamSource(schemaResource.getReader())).newValidator();
            validator.validate(new StreamSource(inputResource.getReader()));
        }
        catch (Exception e)
        {
            throw new SchemaValidationException("Error validating document: " + inputResource.getName(), e);
        }
    }

    /**
     * Retrieves a pattern based on the given ID.
     *
     * @param id ID of expression
     * @return Pattern object representing the requested regular expression
     * @throws ExpressionNotFoundException Indicates that the requested expression was not found
     */
    public Pattern getPattern(String id) throws ExpressionNotFoundException
    {
        if (!expressionMap.containsKey(id))
        {
            throw new ExpressionNotFoundException("Expression not found: " + id);
        }

        return expressionMap.get(id).getPattern();
    }

    /**
     * Retrieves an expression based on the given ID.
     *
     * @param id ID of expression
     * @return Expression object containing the regular expression string and a pattern object
     * @throws ExpressionNotFoundException Indicates that the requested expression was not found
     */
    public Expression getExpression(String id) throws ExpressionNotFoundException
    {
        if (!expressionMap.containsKey(id))
        {
            throw new ExpressionNotFoundException("Expression not found: " + id);
        }

        return expressionMap.get(id);
    }

    /**
     * Initializes the factory by loading regular expressions from an XML file using the StAX pull parser.
     *
     * @param reader Reader for file containing regular expressions in XML
     */
    private void processExpressions(Reader reader)
    {
        try
        {
            XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(reader);

            while (xmlEventReader.hasNext())
            {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();

                if (xmlEvent.isStartElement())
                {
                    handleStartElement(xmlEvent.asStartElement());
                }
                else if (xmlEvent.isEndElement())
                {
                    handleEndElement(xmlEvent.asEndElement());
                }
            }

            xmlEventReader.close();
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Processes start elements.
     *
     * @param se Start element
     */
    private void handleStartElement(StartElement se)
    {
        String name = se.getName().getLocalPart();

        if (name.equals(ELEMENT_REGEXML))
        {
            handleRegexmlElement(se);
        }
        else if (name.equals(ELEMENT_EXPRESSION))
        {
            handleExpressionElementStart(se);
        }
        else if (name.equals(ELEMENT_START))
        {
            handleStartAnchorElement(se);
        }
        else if (name.equals(ELEMENT_END))
        {
            handleEndAnchorElement(se);
        }
        else if (name.equals(ELEMENT_MATCH))
        {
            handleMatchElement(se);
        }
        else if (name.equals(ELEMENT_GROUP))
        {
            handleGroupElementStart(se);
        }
    }

    /**
     * Processes end elements.
     *
     * @param ee End element
     */
    private void handleEndElement(EndElement ee)
    {
        String name = ee.getName().getLocalPart();

        if (name.equals(ELEMENT_EXPRESSION))
        {
            handleExpressionElementEnd(ee);
        }
        else if (name.equals(ELEMENT_GROUP))
        {
            handleGroupElementEnd(ee);
        }
    }

    /**
     * Processes the regexml element.
     *
     * @param se Start element
     */
    private void handleRegexmlElement(StartElement se)
    {
        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute a = it.next();

            String name = a.getName().getLocalPart();
            String value = a.getValue();

            if (name.equals(ATTR_AUTO_ESCAPE) && value.equals(FALSE))
            {
                autoEscape = false;
            }
        }
    }

    /**
     * Processes the start of the expression element.
     *
     * @param se Start element
     */
    private void handleExpressionElementStart(StartElement se)
    {
        resetInstanceVariables();

        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute a = it.next();

            String name = a.getName().getLocalPart();
            String value = a.getValue();

            if (name.equals(ATTR_ID))
            {
                expressionId = value;
            }
            else if (name.equals(ATTR_IGNORE_CASE) && value.equals(TRUE))
            {
                ignoreCase = true;
            }
            else if (name.equals(ATTR_DOT_MATCHES_LINE_BREAKS) && value.equals(TRUE))
            {
                dotMatchesLineBreaks = true;
            }
            else if (name.equals(ATTR_ANCHORS_MATCH_LINE_BREAKS) && value.equals(TRUE))
            {
                anchorsMatchLineBreaks = true;
            }
        }
    }

    /**
     * Resets all instance variables to their initial values.
     */
    private void resetInstanceVariables()
    {
        ignoreCase = false;
        dotMatchesLineBreaks = false;
        anchorsMatchLineBreaks = false;
        startAnchorMatchesLineBreaks = false;
        regExpression = new StringBuilder();
    }

    /**
     * Processes the end of the expression element.
     *
     * @param ee End element
     */
    private void handleExpressionElementEnd(EndElement ee)
    {
        int options = 0;

        if (ignoreCase)
        {
            options = options | Pattern.CASE_INSENSITIVE;
        }

        if (dotMatchesLineBreaks)
        {
            options = options | Pattern.DOTALL;
        }

        if (anchorsMatchLineBreaks)
        {
            options = options | Pattern.MULTILINE;
        }

        String regExpressionString = regExpression.toString();
        Pattern pattern = Pattern.compile(regExpressionString, options);

        expressionMap.put(expressionId, new Expression(regExpressionString, pattern));
    }

    /**
     * Processes the start anchor element.
     *
     * @param se Start element
     */
    private void handleStartAnchorElement(StartElement se)
    {
        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute a = it.next();

            String name = a.getName().getLocalPart();
            String value = a.getValue();

            if (name.equals(ATTR_MATCH_LINE_BREAKS) && value.equals(TRUE))
            {
                regExpression.append("(?m)");
                startAnchorMatchesLineBreaks = true;
            }
        }

        regExpression.append("^");
    }

    /**
     * Processes the end anchor element.
     *
     * @param se Start element
     */
    private void handleEndAnchorElement(StartElement se)
    {
        boolean matchLineBreaks = false;

        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute a = it.next();

            String name = a.getName().getLocalPart();
            String value = a.getValue();

            if (name.equals(ATTR_MATCH_LINE_BREAKS) && value.equals(TRUE))
            {
                matchLineBreaks = true;
            }
        }

        if (startAnchorMatchesLineBreaks)
        {
            if (!matchLineBreaks)
            {
                regExpression.append("(?-m)");
            }
        }
        else if (matchLineBreaks)
        {
            regExpression.append("(?m)");
        }

        regExpression.append("$");
    }

    /**
     * Processes the match element.
     *
     * @param se Start element
     */
    private void handleMatchElement(StartElement se)
    {
        int length = regExpression.length();
        boolean capture = false;
        boolean ignoreCase = false;
        boolean dotMatchesLineBreaks = false;
        String min = "1";
        String max = "1";

        processOrOperator();

        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute a = it.next();

            String name = a.getName().getLocalPart();
            String value = a.getValue();

            if (name.equals(ATTR_EQUALS))
            {
                regExpression.append(autoEscape(value));
            }
            else if (name.equals(ATTR_EXCEPT))
            {
                value = autoEscape(value);

                if (value.startsWith("["))
                {
                    regExpression.append("[^").append(value.substring(1));
                }
                else
                {
                    regExpression.append("[^").append(value).append("]");
                }
            }
            else if (name.equals(ATTR_MIN))
            {
                min = value;
            }
            else if (name.equals(ATTR_MAX))
            {
                max = value;
            }
            else if (name.equals(ATTR_CAPTURE) && value.equals(TRUE))
            {
                capture = true;
            }
            else if (name.equals(ATTR_IGNORE_CASE) && value.equals(TRUE))
            {
                ignoreCase = true;
            }
            else if (name.equals(ATTR_DOT_MATCHES_LINE_BREAKS) && value.equals(TRUE))
            {
                dotMatchesLineBreaks = true;
            }
        }

        handleMinMax(min, max);

        if (ignoreCase || dotMatchesLineBreaks)
        {
            StringBuilder optionsOn = new StringBuilder(8);
            optionsOn.append("(?");

            StringBuilder optionsOff = new StringBuilder(8);
            optionsOff.append("(?-");

            if (ignoreCase)
            {
                optionsOn.append("i");
                optionsOff.append("i");
            }
            if (dotMatchesLineBreaks)
            {
                optionsOn.append("s");
                optionsOff.append("s");
            }

            optionsOn.append(")");
            optionsOff.append(")");

            regExpression.insert(length, optionsOn.toString()).append(optionsOff.toString());
        }

        if (capture)
        {
            regExpression.insert(length, "(").append(")");
        }
    }

    /**
     * Determines if the logical OR operator needs to be inserted before the next match or group.
     */
    private void processOrOperator()
    {
        if (!groupStack.isEmpty())
        {
            GroupData groupData = groupStack.peek();

            if (groupData.isFirstMatch())
            {
                groupData.setFirstMatch(false);
            }
            else if (groupData.getOperator().equals(OPERATOR_OR))
            {
                regExpression.append("|");
            }
        }
    }

    /**
     * Processes the start of the group element.
     *
     * @param se Start element
     */
    private void handleGroupElementStart(StartElement se)
    {
        boolean capture = false;
        StringBuilder matchOptionsOn = new StringBuilder();
        StringBuilder matchOptionsOff = new StringBuilder();

        processOrOperator();

        int length = regExpression.length();

        GroupData groupData = new GroupData();
        groupStack.push(groupData);

        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute a = it.next();

            String name = a.getName().getLocalPart();
            String value = a.getValue();

            if (name.equals(ATTR_MIN))
            {
                groupData.setMin(value);
            }
            else if (name.equals(ATTR_MAX))
            {
                groupData.setMax(value);
            }
            else if (name.equals(ATTR_CAPTURE) && value.equals(TRUE))
            {
                capture = true;
            }
            else if (name.equals(ATTR_IGNORE_CASE))
            {
                if (value.equals(TRUE))
                {
                    matchOptionsOn.append("i");
                }
                else
                {
                    matchOptionsOff.append("i");
                }
            }
            else if (name.equals(ATTR_DOT_MATCHES_LINE_BREAKS))
            {
                if (value.equals(TRUE))
                {
                    matchOptionsOn.append("s");
                }
                else
                {
                    matchOptionsOff.append("s");
                }
            }
            else if (name.equals(ATTR_ANCHORS_MATCH_LINE_BREAKS))
            {
                if (value.equals(TRUE))
                {
                    matchOptionsOn.append("m");
                }
                else
                {
                    matchOptionsOff.append("m");
                }
            }
            else if (name.equals(ATTR_OPERATOR) && value.equals(OPERATOR_OR))
            {
                groupData.setOperator(OPERATOR_OR);
            }
        }

        StringBuilder groupStart = new StringBuilder("(");

        if (!capture)
        {
            groupStart.append("?");
        }

        groupStart.append(matchOptionsOn.toString());

        if (matchOptionsOff.length() > 0)
        {
            groupStart.append("-").append(matchOptionsOff.toString());
        }

        if (!capture)
        {
            groupStart.append(":");
        }

        regExpression.insert(length, groupStart.toString());
    }

    /**
     * Processes the end of the group element.
     *
     * @param ee End element
     */
    private void handleGroupElementEnd(EndElement ee)
    {
        regExpression.append(")");

        GroupData groupData = groupStack.pop();
        handleMinMax(groupData.getMin(), groupData.getMax());
    }

    /**
     * Processes min and max settings.
     *
     * @param min Minimum number of times match can occur
     * @param max Maximum number of times match can occur
     */
    private void handleMinMax(String min, String max)
    {
        if (!min.equals("1") || !max.equals("1"))
        {
            if (min.equals("0") && max.equals("1"))
            {
                regExpression.append("?");
            }
            else if (min.equals("0") && max.equals("*"))
            {
                regExpression.append("*");
            }
            else if (min.equals("1") && max.equals("*"))
            {
                regExpression.append("+");
            }
            else
            {
                regExpression.append("{").append(min);

                if (max.equals("*"))
                {
                    regExpression.append(",");
                }
                else if (Integer.parseInt(max) > Integer.parseInt(min))
                {
                    regExpression.append(",").append(max);
                }
                
                regExpression.append("}");
            }
        }
    }

    /**
     * Escapes the following characters: $()*+.?\^{|
     * 
     * @param text Text containing characters to autoEscape
     * @return Escaped text
     */
    private String autoEscape(String text)
    {
        if (autoEscape)
        {
            for (int i = 0; i < autoEscapeChars.length; i++)
            {
                if (text.contains(autoEscapeChars[i]))
                {
                    text = text.replaceAll("\\" + autoEscapeChars[i], "\\\\" + autoEscapeChars[i]);
                }
            }
        }

        return text;
    }

    /**
     * Encapsulates settings for a single group.
     */
    private static class GroupData
    {
        private String min = "1";
        private String max = "1";
        private String operator = OPERATOR_AND;
        private boolean firstMatch = true;

        /**
         * Gets the minimum number of times group may appear.
         *
         * @return Minimum quantity
         */
        public String getMin()
        {
            return min;
        }

        /**
         * Sets the minimum number of times group may appear.
         *
         * @param min Minimum quantity
         */
        public void setMin(String min)
        {
            this.min = min;
        }

        /**
         * Gets the maximum number of times group may appear.
         *
         * @return Maximum quantity
         */
        public String getMax()
        {
            return max;
        }

        /**
         * Sets the maximum number of times group may appear.
         *
         * @param max Maximum quantity
         */
        public void setMax(String max)
        {
            this.max = max;
        }

        /**
         * Gets the logical operator to be used by this group ("and" or "or").
         *
         * @return Logical operator
         */
        public String getOperator()
        {
            return operator;
        }

        /**
         * Sets the logical operator to be used by this group ("and" or "or").
         *
         * @param operator Logical operator
         */
        public void setOperator(String operator)
        {
            this.operator = operator;
        }

        /**
         * Gets the variable used to flag the first match within this group.
         *
         * @return First match flag
         */
        public boolean isFirstMatch()
        {
            return firstMatch;
        }

        /**
         * Sets the variable used to flag the first match within this group.
         *
         * @param firstMatch First match flag
         */
        public void setFirstMatch(boolean firstMatch)
        {
            this.firstMatch = firstMatch;
        }
    }
}
