Regular expressions are great at parsing portions of text out of a string or determining whether text matches a specific pattern. However, this power comes at a cost. Regular expressions can be very complex to write and difficult to document. Many regular expression libraries don't support in-line comments at all. And even when they are supported, comments can seem to blend in with the rest of the expression (see example below). Additionally, there are numerous special characters to remember that must be escaped in order to use them literally. The purpose of this project is to simplify the process of creating and maintaining complex regular expressions by allowing them to be defined in a more verbose XML vocabulary. For example, a regexml expression for extracting information from a URL could look like this:

```
<regexml xmlns="http://schemas.regexml.org/expressions">
    <expression id="url">
        <start/>
        <match equals="[A-Za-z]" max="*" capture="true"/> <!-- scheme (e.g., http) -->
        <match equals=":"/>
        <match equals="//" min="0"/> <!-- mailto: and news: URLs do not require forward slashes -->
        <match equals="[0-9.\-A-Za-z@]" max="*" capture="true"/> <!-- domain (e.g., www.regexml.org) -->
        <group min="0">
            <match equals=":"/>
            <match equals="\d" max="5" capture="true"/> <!-- port number -->
        </group>
        <group min="0" capture="true"> <!-- resource (e.g., /sample/resource) -->
            <match equals="/"/>
            <match except="[?#]" max="*"/>
        </group>
        <group min="0">
            <match equals="?"/>
            <match except="#" min="0" max="*" capture="true"/> <!-- query string -->
        </group>
        <group min="0">
            <match equals="#"/>
            <match equals="." min="0" max="*" capture="true"/> <!-- anchor tag -->
        </group>
        <end/>
    </expression>
</regexml>
```

This example extracts the scheme, domain, port, resource, query string, and anchor tag from a standard URL (see the `capture` attributes above). Character sets (or _classes_) are defined using the same syntax as regular expressions with the exception that regexml automatically escapes some characters that regular expressions do not (e.g., the characters `$()*+?^{|` do not need escaping in regexml). Elements can be grouped for the purpose of capturing blocks of text or defining how many times a group may appear. In the example above, most groups are made optional by assigning a "min" value of zero. If not specified, the `min`, `max`, and `capture` attributes default to 1, 1, and false, respectively. Notice that since they are defined in XML, in-line comments can be included in order to document complex expressions. And of course, a schema is provided to simplify the creation of valid regexml documents in any XML editor.

Given a file named _url\_expression.xml_ containing the XML shown above and residing in the classpath, Java code like this can be used to extract the captured text:

```
import org.regexml.ExpressionFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleClient
{
    public static void main(String[] args)
    {
        ExpressionFactory ef = new ExpressionFactory(new ClassPathResource("url_expression.xml"));

        Pattern pattern = ef.getPattern("url"); //matches id attribute in <expression> element
        Matcher matcher = pattern.matcher("http://www.regexml.org:8080/sample/resource.html?param=1#anchor");

        if (matcher.find())
        {
            for (int i = 1; i <= matcher.groupCount(); i++)
            {
                System.out.println(matcher.group(i));
            }
        }
    }
}
```

The regexml expression is compiled into a standard `java.util.regex.Pattern` object at the time the `ExpressionFactory` is instantiated. The `Pattern` object is cached so that it can be re-used any number of times without additional compilation. As long as they are assigned unique IDs, any number of expressions can be defined within a single XML file. The output from the code above looks like this:

```
http
www.regexml.org
8080
/sample/resource.html
param=1
anchor
```

Now let's see how that same "url" expression would look as a standard regular expression without comments:

```
^([A-Za-z]+):(?://)?([0-9.\-A-Za-z@]+)(?::(\d{1,5}))?(/[^\?#]+)?(?:\?([^#]*))?(?:#(.*))?$
```

And here's how it looks with in-line comments included (for those regular expression implementations that support comments):

```
^(#scheme (e.g., http\))([A-Za-z]+):(?://)?(#domain (e.g., www.regexml.org\))([0-9.\-A-Za-z@]+)(?::(#port number)(\d{1,5}))?
(#resource (e.g., /about/project/license.html\))(/[^\?#]+)?(#query string)(?:\?([^#]*))?(#URL anchor tag)(?:#(.*))?$
```

That certainly is a lot shorter than the regexml expression. However, it's brevity also makes it more difficult to understand and debug. There are more cryptic characters and syntax to remember (e.g., `^`, `{1,5}`, `?:`, `+`, `$`, etc.) and more characters that must be escaped when used literally (e.g., `\)`, `\?`). In fact, regular expressions require a dozen characters to be escaped when used literally: `$()*+.?[\^{|` On the other hand, regexml requires escaping of only five characters: `.\[<"`

Regular expressions are great for simple expressions that are small and easy to follow. For these types of expressions, it's usually easier to include them inline in code rather than defining them in a separate file. However, for more complex expressions where readability and maintainability are at a premium, regexml offers a syntax that is easier to document and comprehend.

The initial version of this library is provided in Java. Based on interest, it may be ported to other platforms, such as .NET, in the future. See the [Introduction Wiki](http://code.google.com/p/regexml/wiki/Introduction) for complete instructions on using the regexml library.