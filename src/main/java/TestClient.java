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

        Pattern pattern = ef.getExpression("phone");
        Matcher matcher = pattern.matcher("(801) 796-3438");

        if (matcher.find())
        {
            for (int i = 1; i <= matcher.groupCount(); i++)
            {
                System.out.println(matcher.group(i));
            }
        }

        pattern = ef.getExpression("url");
        matcher = pattern.matcher("http://www.regexml.org:8080/test?param=1#anchor");

        if (matcher.find())
        {
            for (int i = 1; i <= matcher.groupCount(); i++)
            {
                System.out.println(matcher.group(i));
            }
        }
    }
}
