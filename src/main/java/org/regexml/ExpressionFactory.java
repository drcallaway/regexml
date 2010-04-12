package org.regexml;

import org.regexml.exception.ExpressionNotFoundException;
import org.regexml.resource.Resource;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Constructs regular expressions from an XML file.
 *
 * @author Dustin R. Callaway
 */
public class ExpressionFactory
{
    private static final String[] autoEscapeChars = {"$", "(", ")", "*", "+", "?", "^", "{", "|"};

    private Map<String, Pattern> map = new HashMap<String, Pattern>();
    private XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    private StringBuilder regExpression;
    private String expressionId;
    private String groupMin;
    private String groupMax;
    private boolean autoEscape = true;
    private boolean ignoreCase;
    private boolean dotMatchAll;
    private boolean multiline;

    /**
     * Constructs an ExpressionFactory object.
     *
     * @param resource Resource referencing the file containing expressions in XML
     */
    public ExpressionFactory(Resource resource)
    {
        this(resource.getReader());
    }

    /**
     * Constructs an ExpressionFactory object.
     *
     * @param reader Reader for the file containing expressions in XML
     */
    public ExpressionFactory(Reader reader)
    {
        loadExpressions(reader);
    }

    /**
     * Retrieves an expression from the factory based on the given ID.
     *
     * @param id ID of expression to retrieve
     * @return Pattern object representing the requested regular expression
     * @throws ExpressionNotFoundException
     */
    public Pattern getExpression(String id) throws ExpressionNotFoundException
    {
        if (!map.containsKey(id))
        {
            throw new ExpressionNotFoundException("Expression not found: " + id);
        }

        return map.get(id);
    }

    /**
     * Initializes the factory by loading regular expressions from an XML file.
     *
     * @param reader Reader for file containing regular expressions in XML
     */
    private void loadExpressions(Reader reader)
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

        if (name.equals("regexml"))
        {
            handleRegexmlElement(se);
        }
        else if (name.equals("expression"))
        {
            handleExpressionElementStart(se);
        }
        else if (name.equals("start"))
        {
            regExpression.append("^");
        }
        else if (name.equals("end"))
        {
            regExpression.append("$");
        }
        else if (name.equals("match"))
        {
            handleMatchElement(se);
        }
        else if (name.equals("group"))
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

        if (name.equals("expression"))
        {
            handleExpressionElementEnd(ee);
        }
        else if (name.equals("group"))
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

            if (name.equals("escape") && value.equals("false"))
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
        ignoreCase = false;
        dotMatchAll = false;
        multiline = false;
        regExpression = new StringBuilder();
        
        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute a = it.next();

            String name = a.getName().getLocalPart();
            String value = a.getValue();

            if (name.equals("id"))
            {
                expressionId = value;
            }
        }
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

        if (dotMatchAll)
        {
            options = options | Pattern.DOTALL;
        }

        if (multiline)
        {
            options = options | Pattern.MULTILINE;
        }
        
        Pattern pattern = Pattern.compile(regExpression.toString(), options);
        map.put(expressionId, pattern);
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
        String min = "1";
        String max = "1";

        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute a = it.next();

            String name = a.getName().getLocalPart();
            String value = a.getValue();

            if (name.equals("equals"))
            {
                regExpression.append(autoEscape(value));
            }
            else if (name.equals("except"))
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
            else if (name.equals("min"))
            {
                min = value;
            }
            else if (name.equals("max"))
            {
                max = value;
            }
            else if (name.equals("capture") && value.equals("true"))
            {
                capture = true;
            }
        }

        handleMinMax(min, max);

        if (capture)
        {
            regExpression.insert(length, "(").append(")");
        }
    }

    /**
     * Processes the start of the group element.
     *
     * @param se Start element
     */
    private void handleGroupElementStart(StartElement se)
    {
        int length = regExpression.length();
        groupMin = "1";
        groupMax = "1";
        boolean capture = false;

        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute a = it.next();

            String name = a.getName().getLocalPart();
            String value = a.getValue();

            if (name.equals("min"))
            {
                groupMin = value;
            }
            else if (name.equals("max"))
            {
                groupMax = value;
            }
            else if (name.equals("capture") && value.equals("true"))
            {
                capture = true;
            }
        }

        if (capture)
        {
            regExpression.insert(length, "(");
        }
        else
        {
            regExpression.insert(length, "(?:");
        }
    }

    /**
     * Processes the end of the group element.
     *
     * @param ee End element
     */
    private void handleGroupElementEnd(EndElement ee)
    {
        regExpression.append(")");
        handleMinMax(groupMin, groupMax);
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
                if (Integer.parseInt(min) > Integer.parseInt(max))
                {
                    max = min;
                }
                
                regExpression.append("{").append(min).append(",").append(max).append("}");
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
}
