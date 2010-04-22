package test;

import org.regexml.Expression;
import org.regexml.ExpressionFactory;
import org.regexml.resource.FileSystemResource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Temporary test client.
 */
public class TestClient
{
    public static void main(String[] args)
    {
        ExpressionFactory ef = new ExpressionFactory(new FileSystemResource("test.xml"), true);

        Expression exp = ef.getExpression("zipcode");

        System.out.println("RegEx: " + exp.getRegExString());
        Pattern pattern = exp.getPattern();
        Matcher matcher = pattern.matcher("98765-4321");

        if (matcher.find())
        {
            for (int i = 1; i <= matcher.groupCount(); i++)
            {
                System.out.println(matcher.group(i));
            }
        }

        exp = ef.getExpression("url");

        System.out.println("RegEx: " + exp.getRegExString());
        pattern = exp.getPattern();
        matcher = pattern.matcher("http://www.regexml.org:8080/sample/resource.html?param=1#anchor");

        if (matcher.find())
        {
            for (int i = 1; i <= matcher.groupCount(); i++)
            {
                System.out.println(matcher.group(i));
            }
        }
    }
}
