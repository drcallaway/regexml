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
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: dcallaway
 * Date: Apr 1, 2010
 * Time: 4:20:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionFactory
{
    private static final String[] escapeChars = {"$", "(", ")", "*", "+", "?", "^", "{", "|"};

    private Map<String, Pattern> map = new HashMap<String, Pattern>();
    private XMLInputFactory inputFactory = null;
    private StringBuilder regExpression;
    private String expressionId;
    private String groupMin;
    private String groupMax;
    private boolean escape = true;

    public ExpressionFactory(Resource resource)
    {
        this(resource.getReader());
    }

    public ExpressionFactory(Reader reader)
    {
        inputFactory = XMLInputFactory.newInstance();
        loadConfiguration(reader);
    }

    public Pattern getExpression(String id) throws ExpressionNotFoundException
    {
        if (!map.containsKey(id))
        {
            throw new ExpressionNotFoundException();
        }

        return map.get(id);
    }

    private void loadConfiguration(Reader reader)
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

    private void handleStartElement(StartElement se)
    {
        String name = se.getName().getLocalPart();

        if (name.equals("regexml"))
        {
            handleRegXmlElement(se);
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

    private void handleRegXmlElement(StartElement se)
    {
        for (Iterator<Attribute> it = se.getAttributes(); it.hasNext();)
        {
            Attribute a = it.next();

            String name = a.getName().getLocalPart();
            String value = a.getValue();

            if (name.equals("escape") && value.equals("false"))
            {
                escape = false;
            }
        }
    }
    
    private void handleExpressionElementStart(StartElement se)
    {
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

    private void handleExpressionElementEnd(EndElement ee)
    {
        Pattern pattern = Pattern.compile(regExpression.toString());
        map.put(expressionId, pattern);
    }

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

    private void handleGroupElementEnd(EndElement ee)
    {
        regExpression.append(")");
        handleMinMax(groupMin, groupMax);
    }

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
     * @param text Text containing characters to escape
     * @return Escaped text
     */
    private String autoEscape(String text)
    {
        if (escape)
        {
            for (int i = 0; i < escapeChars.length; i++)
            {
                if (text.contains(escapeChars[i]))
                {
                    text = text.replaceAll("\\" + escapeChars[i], "\\\\" + escapeChars[i]);
                }
            }
        }

        return text;
    }
}
