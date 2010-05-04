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
    private static final String ATTR_LAZY = "lazy";
    private static final String ATTR_ATOMIC = "atomic";
    private static final String ATTR_OPERATOR = "operator";
    private static final String ATTR_LOOKAHEAD = "lookahead";
    private static final String ATTR_LOOKBEHIND = "lookbehind";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String OPERATOR_AND = "and";
    private static final String OPERATOR_OR = "or";
    private static final String LOOKAROUND_NONE = "none";
    private static final String LOOKAROUND_POSITIVE = "positive";
    private static final String LOOKAROUND_NEGATIVE = "negative";

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
    private enum LookaroundOptions {NONE, POSITIVE, NEGATIVE};

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

        expressionMap.put(expressionId, new Expression(expressionId, regExpressionString, pattern));
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
        boolean lazy = false;
        boolean atomic = false;
        LookaroundOptions lookahead = LookaroundOptions.NONE;
        LookaroundOptions lookbehind = LookaroundOptions.NONE;
        int addGroupingIndex = -1;
        String equalsExpression = null;
        String exceptExpression = null;
        String min = "1";
        String max = "1";

        processOrOperator();

        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute attribute = it.next();

            String name = attribute.getName().getLocalPart();
            String value = attribute.getValue();

            if (name.equals(ATTR_EQUALS))
            {
                equalsExpression = autoEscape(value);
            }
            else if (name.equals(ATTR_EXCEPT))
            {
                exceptExpression = autoEscape(value);
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
            else if (name.equals(ATTR_LAZY) && value.equals(TRUE))
            {
                lazy = true;
            }
            else if (name.equals(ATTR_ATOMIC) && value.equals(TRUE))
            {
                atomic = true;
            }
            else if (name.equals(ATTR_LOOKAHEAD))
            {
                lookahead = LookaroundOptions.valueOf(value.toUpperCase());
            }
            else if (name.equals(ATTR_LOOKBEHIND))
            {
                lookbehind = LookaroundOptions.valueOf(value.toUpperCase());
            }
        }

        // process equals and except expressions
        if (equalsExpression != null && exceptExpression != null)
        {
            if (isCharacterClass(equalsExpression))
            {
                regExpression.append("[").append(equalsExpression).append("&&[^");

                if (isCharacterClass(exceptExpression))
                {
                    regExpression.append(exceptExpression.substring(1));
                }
                else
                {
                    regExpression.append(exceptExpression).append("]");
                }

                regExpression.append("]");
            }
            else //ignore except expression since it's only allowed when the equals expression is a character class
            {
                regExpression.append(equalsExpression);
            }
        }
        else
        {
            if (equalsExpression != null)
            {
                regExpression.append(equalsExpression);
            }
            else if (exceptExpression != null)
            {
                regExpression.append("[^");

                if (isCharacterClass(exceptExpression))
                {
                    regExpression.append(exceptExpression.substring(1));
                }
                else
                {
                    regExpression.append(exceptExpression).append("]");
                }
            }
        }

        // grouping index is for grouping text before a quantifier is applied, only necessary for equals expressions
        // that are longer than one character and not a character class
        if (requiresGrouping(equalsExpression))
        {
            addGroupingIndex = length;
        }

        handleMinMax(min, max, lazy, addGroupingIndex);

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

        if (lookahead == LookaroundOptions.POSITIVE)
        {
            regExpression.insert(length, "(?=").append(")");
        }
        else if (lookahead == LookaroundOptions.NEGATIVE)
        {
            regExpression.insert(length, "(?!").append(")");
        }
        else if (lookbehind == LookaroundOptions.POSITIVE)
        {
            regExpression.insert(length, "(?<=").append(")");
        }
        else if (lookbehind == LookaroundOptions.NEGATIVE)
        {
            regExpression.insert(length, "(?<!").append(")");
        }

        if (atomic)
        {
            regExpression.insert(length, "(?>").append(")"); //do not store any backtracking positions
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
     * Indicates whether or not a given expression must be in a group before a quantifier is applied.
     *
     * @param expression Expression to evaluate for grouping
     * @return True indicates that expression must be grouped before quantifier
     */
    private boolean requiresGrouping(String expression)
    {
        boolean requiresGrouping = true;

        if (expression == null)
        {
            requiresGrouping = false;
        }
        else if (expression.startsWith("\\"))
        {
            if (expression.length() == 2)
            {
                requiresGrouping = false;
            }
        }
        else if (expression.length() == 1 || isCharacterClass(expression))
        {
            requiresGrouping = false;
        }

        return requiresGrouping;
    }

    /**
     * Indicates whether or not the given expression represents a character class.
     *
     * @param expression Expression to evaluate
     * @return True if expression is a character class
     */
    private boolean isCharacterClass(String expression)
    {
        return (expression.startsWith("[") && expression.endsWith("]")) || expression.equals(".") ||
            expression.equalsIgnoreCase("\\d") || expression.equalsIgnoreCase("\\s") ||
            expression.equalsIgnoreCase("\\w") || expression.equalsIgnoreCase("\\b");
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
            Attribute attribute = it.next();

            String name = attribute.getName().getLocalPart();
            String value = attribute.getValue();

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
            else if (name.equals(ATTR_LAZY) && value.equals(TRUE))
            {
                groupData.setLazy(true);
            }
            else if (name.equals(ATTR_ATOMIC) && value.equals(TRUE))
            {
                groupData.setAtomic(true);
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
            else if (name.equals(ATTR_LOOKAHEAD))
            {
                groupData.setLookahead(LookaroundOptions.valueOf(value.toUpperCase()));
            }
            else if (name.equals(ATTR_LOOKBEHIND))
            {
                groupData.setLookbehind(LookaroundOptions.valueOf(value.toUpperCase()));
            }
        }

        StringBuilder groupStart = new StringBuilder("("); //start capturing or non-capturing group

        if (groupData.isAtomic())
        {
            groupStart.append("?>("); //start atomic group
        }

        if (groupData.getLookahead() == LookaroundOptions.POSITIVE)
        {
            groupStart.append("?=("); //start positive lookahead
        }
        else if (groupData.getLookahead() == LookaroundOptions.NEGATIVE)
        {
            groupStart.append("?!("); //start negative lookahead
        }
        else if (groupData.getLookbehind() == LookaroundOptions.POSITIVE)
        {
            groupStart.append("?<=("); //start positive lookbehind
        }
        else if (groupData.getLookbehind() == LookaroundOptions.NEGATIVE)
        {
            groupStart.append("?<!("); //start negative lookbehind
        }

        if (!capture)
        {
            groupStart.append("?"); //start non-capturing group
        }

        groupStart.append(matchOptionsOn.toString());

        if (matchOptionsOff.length() > 0)
        {
            groupStart.append("-").append(matchOptionsOff.toString());
        }

        if (!capture)
        {
            groupStart.append(":"); //part of non-capturing group start
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
        regExpression.append(")"); //end capturing or non-capturing group

        GroupData groupData = groupStack.pop();
        handleMinMax(groupData.getMin(), groupData.getMax(), groupData.isLazy(), -1);

        if (groupData.getLookahead() != LookaroundOptions.NONE || groupData.getLookbehind() != LookaroundOptions.NONE)
        {
            regExpression.append(")"); //end lookaround group
        }

        if (groupData.isAtomic())
        {
            regExpression.append(")"); //end atomic group
        }
    }

    /**
     * Processes min and max settings.
     *
     * @param min Minimum number of times match can occur
     * @param max Maximum number of times match can occur
     * @param lazy Indicates lazy matching is to be performed
     * @param addGroupingIndex A value greater than -1 indicates that a non-capturing group needs to be added
     */
    private void handleMinMax(String min, String max, boolean lazy, int addGroupingIndex)
    {
        if (!min.equals("1") || !max.equals("1"))
        {
            if (addGroupingIndex > -1)
            {
                regExpression.insert(addGroupingIndex, "(?:").append(")");
            }

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

            if (lazy)
            {
                regExpression.append("?"); //add lazy quantifier
            }
        }
    }

    /**
     * Escapes the following characters: $()*+?^{|
     * 
     * @param text Text containing characters to autoEscape
     * @return Escaped text
     */
    private String autoEscape(String text)
    {
        if (autoEscape)
        {
            text = text.replaceAll("([\\$\\(\\)\\*\\+\\?\\^\\{\\|])", "\\\\$1");
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
        private boolean lazy = false;
        private boolean atomic = false;
        private LookaroundOptions lookahead = LookaroundOptions.NONE;
        private LookaroundOptions lookbehind = LookaroundOptions.NONE;
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
         * Indicates whether or not lazy matching is to be performed.
         *
         * @return True indicates lazy matching, otherwise greedy matching is used
         */
        public boolean isLazy()
        {
            return lazy;
        }

        /**
         * Sets whether or not lazy matching is to be performed.
         *
         * @param lazy True indicates lazy matching, otherwise greedy matching is used
         */
        public void setLazy(boolean lazy)
        {
            this.lazy = lazy;
        }

        /**
         * Gets whether or not the group is atomic. An atomic group is more efficient since all backtracking information
         * is dropped after it is evaluated.
         *
         * @return True indicates an atomic group
         */
        public boolean isAtomic()
        {
            return atomic;
        }

        /**
         * Sets whether or not the group is atomic. An atomic group is more efficient since all backtracking information
         * is dropped after it is evaluated.
         *
         * @param atomic True indicates an atomic group
         */
        public void setAtomic(boolean atomic)
        {
            this.atomic = atomic;
        }

        /**
         * Gets the lookahead option used by this group.
         *
         * @return Lookahead option
         */
        public LookaroundOptions getLookahead()
        {
            return lookahead;
        }

        /**
         * Sets the lookahead option used by this group.
         *
         * @param lookahead Lookahead option
         */
        public void setLookahead(LookaroundOptions lookahead)
        {
            this.lookahead = lookahead;
        }

        /**
         * Gets the lookbehind option used by this group.
         *
         * @return Lookbehind option
         */
        public LookaroundOptions getLookbehind()
        {
            return lookbehind;
        }

        /**
         * Sets the lookbehind option used by this group.
         *
         * @param lookbehind Lookbehind option
         */
        public void setLookbehind(LookaroundOptions lookbehind)
        {
            this.lookbehind = lookbehind;
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
