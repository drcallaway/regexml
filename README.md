# regexml 

NOTE: This project has been archived and is no longer under development. See original project here: https://code.google.com/archive/p/regexml/

## Project Description
The regexml Java library provides a way to define regular expressions in a verbose XML vocabulary rather than the terse and cryptic format currently in use. By defining and documenting complex expressions in XML, regular expressions are easier to understand, debug, and maintain.

## Library Installation
In order to use the regexml Java library, simply add the regexml-x.x.x.jar file to your project's classpath. Once added, four public classes and one interface will be available to your application. Each of these classes and the interface are described in detail in the following section.

## Java API
The regexml library API is very simple. There are only four classes and one interface to learn as shown here:

| Class / Interface | Description | |:----------------------|:----------------| | Resource | Interface implemented by classes that encapsulate a regexml expressions file (i.e., ClassPathResource and FileSystemResource). | | ClassPathResource | Represents an XML file available on the classpath that contains regexml expressions (implements the Resource interface). | | FileSystemResource | Represents an XML file available on the file system that contains regexml expressions (implements the Resource interface). | | Expression | Provides access to a regexml expression's java.util.regex.Pattern object and a text representation of the expression in standard regular expression syntax. | | ExpressionFactory | Parses the XML file represented by a ClassPathResource or FileSystemResource object and creates a java.util.regex.Pattern object for each expression defined in the XML. The Pattern object can then be used to extract portions of a string or match text against the pattern. |

The javadoc is available here. Now let's take a look at each of these classes and interface.

### Resource Interface and Classes
The Resource interface represents an object through which a regexml expressions file can be accessed. The ClassPathResource and FileSystemResource classes implement Resource and are used to access a regexml expressions file via the classpath or file system, respectively. ClassPathResource is instantiated like this:

```Resource cpResource = new ClassPathResource("expressions.xml");```

FileSystemResource is instantiated like this:

```Resource fsResource = new FileSystemResource("/home/user/expressions.xml");```

Or like this:

```File file = new File("/home/user/expressions.xml"); Resource fsResource = new FileSystemResource(file);```

Depending on the type of resource implementation you choose, the regexml expressions file can be accessed from either the classpath or the file system. Keep in mind that after an application is packaged for distribution, it's usually easier to access the expressions file from the classpath rather than the file system.

### Expression Class
The Expression class encapsulates information related to a single regexml expression. This class exposes the following public methods:

| Method | Description | |:-----------|:----------------| | getId() | Gets the ID for this expression as specified in the regexml expressions file. | | getPattern() | Gets the java.util.regex.Pattern object associated with this expression. | | getRegExString() | Gets a text representation of the expression in traditional regular expression syntax. |

Typically, you will only be interested in retrieving Pattern objects in order to evaluate regular expressions. In that case, the ExpressionFactory class provides a convenience method called getPattern() that allows you to bypass the Expression object and retrieve the Pattern object directly. Regardless, the Expression object is available for times when you need to access the expression's ID or see the expression in traditional regular expression syntax.

### ExpressionFactory Class
The ExpressionFactory class compiles regexml expressions into standard java.util.regex.Pattern objects, caches them, and makes them available to client applications via the getExpression() and getPattern() methods. Given that regexml expressions are converted to standard Pattern objects, performance of regexml expressions is equivalent to that of standard regular expressions.

ExpressionFactory can be instantiated like this:

```ExpressionFactory expressionFactory = new ExpressionFactory(new ClassPathResource("expressions.xml"));```

Furthermore, there is a second ExpressionFactory constructor that accepts a boolean value indicating whether or not the given expressions file should be validated against the regexml schema. To validate the expressions file during instantiation, pass a second parameter to the constructor like this:

```ExpressionFactory expressionFactory = new ExpressionFactory(new ClassPathResource("expressions.xml"), true);```

ExpressionFactory exposes the following public methods:

| Method | Description | |:-----------|:----------------| | getExpression(String id) | Gets the Expression object associated with the given expression ID. | | getPattern(String id) | Gets the java.util.regex.Pattern object associated with the given expression ID. |

Once the ExpressionFactory is instantiated, it can be used to evaluate regexml expressions. For example, given this regexml expressions file:

```<regexml xmlns="http://schemas.regexml.org/expressions"> <expression id="phone"> <start/> <match equals="("/> <match equals="\d" min="3"/> <!-- area code --> <match equals=") "/> <match equals="\d" min="3"/> <!-- prefix --> <match equals="-"/> <match equals="\d" min="4"/> <end/> </expression> </regexml>```

We can validate a phone number with the following Java code:

```
Expression expr = expressionFactory.getExpression("phone");

java.util.regex.Pattern pattern = expr.getPattern(); java.util.regex.Matcher matcher = pattern.matcher("(801) 555-1212");

if (matcher.matches()) { System.out.println("Thanks for the valid phone number!"); } else { System.out.println("This phone number is invalid! Please use this format: (XXX) XXX-XXXX"); }

System.out.println("Expression in regular expression syntax: " + expr.getRegExString()); 
```

The output from this program looks like this:

Thanks for the valid phone number!

Expression in regular expression syntax: ^\((\d{3})\) (\d{3})-(\d{4})$

The code above retrieves an Expression object because it's used to output the expression in regular expression syntax. If all we want to do is validate a phone number, we can bypass the Expression object and retrieve the java.util.regex.Pattern object directly like this:


```
java.util.regex.Pattern pattern = expressionFactory.getPattern("phone");

java.util.regex.Matcher matcher = pattern.matcher("(801) 555-1212");



if (matcher.matches())

{

    System.out.println("Thanks for the valid phone number!");

}

else

{

    System.out.println("This phone number is invalid! Please use this format: (XXX) XXX-XXXX");

}
```

In addition to validating a phone number, we can also extract information from it by adding capture attributes to the regexml expression like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="phone">

        <start/>

        <match equals="("/>

        <match equals="\d" min="3" capture="true"/> <!-- area code -->

        <match equals=") "/>

        <match equals="\d" min="3" capture="true"/> <!-- prefix -->

        <match equals="-"/>

        <match equals="\d" min="4"/>

        <end/>

    </expression>

</regexml>
```

The expression above will allow us to extract the area code and prefix from any valid phone number. The Java code looks like this:


```java.util.regex.Pattern pattern = expressionFactory.getPattern("phone");

java.util.regex.Matcher matcher = pattern.matcher("(801) 555-1212");



if (matcher.find())

{

    for (int i = 1; i <= matcher.groupCount(); i++) //iterate through all captured matches

    {

        System.out.println(matcher.group(i));

    }

}
```

The output from this program looks like this:


801

555

The Matcher object creates a group for each <match/> element whose capture attribute is set. For more information on using the Java Pattern and Matcher classes, see the javadocs here:

http://java.sun.com/javase/6/docs/api/java/util/regex/Pattern.html'>http://java.sun.com/javase/6/docs/api/java/util/regex/Pattern.html
http://java.sun.com/javase/6/docs/api/java/util/regex/Matcher.html'>http://java.sun.com/javase/6/docs/api/java/util/regex/Matcher.html

For more details on the regexml classes, see the http://regexml.googlecode.com/svn/trunk/javadoc/index.html'>regexml javadoc.


## Expressions File
The expressions file is the heart of the regexml library. It is in this file that regular expressions are defined in a simple XML format. Only one expressions file is needed per application since a single file can contain any number of regular expressions.


## Create the File
In order to simplify creation of the regexml expressions file, a schema is provided that describes the structure of a valid file. In order to retrieve the XML schema, you'll need to open the regexml JAR in a compression utility (e.g., WinZip, jZip, StuffIt, Zipeg, etc.) or the Java command-line jar utility and extract the file named regexml.xsd. Using the jar utility that ships with Java, you can extract the schema with this command:


jar -xf regexml-x.x.x.jar regexml.xsd

Once the schema is available in the file system, you can reference it from any XML editor that supports schemas by associating it with the following namespace:


http://schemas.regexml.org/expressions
For example, in IntelliJ IDEA (my IDE of choice), bring up the "Settings" dialog box and select "Resources". Then click the "Add..." button in the "Configure External Resources" section and enter a URI of http://schemas.regexml.org/expressions as well as the path to the schema file under Location. Once the regexml namespace is associated with the schema, IDEA will assist you when creating regexml expression files by showing the valid elements and attributes available at any point in the document. Just be sure to use a .xml file extension and include the regexml expressions namespace at the beginning of the file like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

</regexml>
```

Other schema-aware XML editors should have similar ways to associate a namespace with a schema file. The structure of the expressions file is discussed in detail in the following sections.


## File Format
Detailed documentation regarding the regexml expressions file format is available here.


## Expression Matching Examples
The following sections provide examples of various regexml expressions.


## Empty File
The simplest valid expressions file is a file that defines no expressions like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

</regexml>
```

Even though it doesn't define any expressions, this file still satisfies the schema because it uses the <regexml> root element and the correct regexml expressions namespace.


## Literal Match
About the simplest expressions file that may be useful is one that includes a single expression that performs a literal match.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="a"/>

    </expression>

</regexml>
```

Matches: a
Regular expression: a

## Ignore Case Matches
There are two ways to ignore case (i.e., capitalization) when performing text comparisons (by default, text comparisons are case sensitive). First, the ignore case directive can be applied to an entire expression by setting the expression element's ignoreCase attribute like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example" ignoreCase="true">

        <match equals="a"/>

    </expression>

</regexml>
```

Matches: a, A

Regular expression: a

Notice that the traditional regular expression doesn't change in this case. That is due to the fact that the "ignore case" option is set by passing a parameter to the constructor of the java.util.regex.Pattern object. Therefore, the Pattern object returned from the ExpressionFactory will be configured to ignore case in all text comparisons. The second way to ignore case is to set the ignoreCase attribute on individual match or group elements like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="a" ignoreCase="true"/>

    </expression>

</regexml>
```

Matches: a, A

Regular expression: (?i)a(?-i)

In this instance, the "ignore case" directive is part of the regular expression rather than a setting within the Pattern object. Case sensitivity can be turned on for an entire group like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="a"/>

        <group ignoreCase="true">

            <match equals="b" min="0"/>

            <match equals="c"/>

        </group>

    </expression>

</regexml>
```

Matches: ac, aC, abc, aBc, aBC, abC

Regular expression: a(?i:b?c)

## Multiple Matches
Since matches are automatically concatenated (unless included in an "or" group discussed later), the following two regexml expressions are equivalent.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="a"/>

        <match equals="b"/>

    </expression>

</regexml>

<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="ab"/>

    </expression>

</regexml>
```

Matches: ab

Regular expression: ab

## Optional Match
A match can be made optional by setting it's min attribute to zero (min defaults to one). For example, the a in the following regexml expression is required for a match but the b is optional.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="a"/>

        <match equals="b" min="0"/>

    </expression>

</regexml>
```

Matches: a, ab

Regular expression: ab?

## Exact Quantity Match
Whenever the min attribute is greater than the max attribute, max is automatically set equal to min. This behavior allows an exact number of matches to be specified by setting only the min attribute (since max defaults to one, any min amount greater than one will cause max to be assigned the same value).


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="a" min="3"/>

    </expression>

</regexml>
```

Matches: aaa

Regular expression: a{3}

## Maximum Quantity Match
If only a max value is set, the default min value of one will be used.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="a" max="3"/>

    </expression>

</regexml>
```

Matches: a, aa, aaa

Regular expression: a{1,3}

## Quantity Range Match
Setting the min and max attributes allows a quantity range to be specified.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="a" min="2" max="4"/>

    </expression>

</regexml>
```

Matches: aa, aaa, aaaa

Regular expression: a{2,4}

## Unbounded Quantity Range Match
Typically, the min and max attributes will be set to non-negative integer values. However, to support unbounded maximum values, the value of the max attribute can be set to an asterisk (*). This indicates an infinite number.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="a" min="2" max="*"/>

    </expression>

</regexml>
```

Matches: aa, aaa, aaaa, ...

Regular expression: a{2,}

## Group Quantity Match
The min and max quantity attributes work with groups the same as they do with individual matches.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <group>

            <match equals="a"/>            

        </group>

        <group min="0">

            <match equals="b"/>

            <match equals="c"/>

        </group>

        <group max="2">

            <match equals="d"/>

        </group>

    </expression>

</regexml>
```

Matches: ad, add, abcd, abcdd

Regular expression: (?:a)(?:bc)?(?:d){1,2}

## Character Class Match
A character class defines a set of characters enclosed in brackets that is used to match one of many characters. If no quantity modifiers are specified (i.e., min and max attributes), a character class matches a single character. For example, the character class [abc] matches a single character a, b, or c. Applying quantity modifiers allows any number of characters to be matched.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="[abc]" max="2"/>

    </expression>

</regexml>
```

Matches: a, b, c, aa, ab, ac, ba, bb, bc, ca, cb, cc

Regular expression: [abc]{1,2}

## Character Class Range Match
The hyphen character is used to represent a range of characters within a character class. For example, the character class [a-z] represents all lowercase letters. Since it has special meaning, the hyphen must be escaped within character classes if wished to be used literally. For example, the character class [a-z] represents the characters a, -, and z rather than a range of letters from a to z.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="[a-c1-3]"/>

    </expression>

</regexml>
```

Matches: a, b, c, 1, 2, 3

Regular expression: [a-c1-3]

Characters can be removed from a range using the except attribute like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="[a-z]" except="[d-w]"/>

    </expression>

</regexml>
```

Matches: a, b, c, x, y, z

Regular expression: [[a-z]&&[^d-w]]

See the "Negated Match" section below for more information about the except attribute.


## Predefined Character Class Matches
For convenience, regular expressions offer a few predefined character classes. The following predefined character classes are available:


Characters Description . Any character \d Digit [0-9] \D Non-digit [^0-9] \s Whitespace character [ \t\n\f\r] \S Non-whitespace character [^ \t\n\f\r] \w Word character [a-zA-Z_0-9] \W Non-word character [^a-zA-Z_0-9] \b Word boundary anchor (break between words) \B Non-word boundary anchor
As shown in the table above, the predefined character class \d is equivalent to the user-defined character class [0-9]. Often times regexml expressions can be simplified through the use of these predefined character classes.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="."/>

        <match equals="\d" min="3"/>

        <match equals="\s"/>

        <match equals="\w" min="3"/>

    </expression>

</regexml>
```

Matches: any character followed by three digits followed by one whitespace character followed by three word characters

Regular expression: .\d{3}\s\w{3}

## Negated Match
The except attribute is the opposite of the match element's equals attribute. Rather than "match this", it basically means "match everything except this". It's important to keep in mind that the except attribute always represents a character class.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="\d"/>

        <match except="\d"/>

    </expression>

</regexml>
```

Matches: one digit followed by one non-digit character

Regular expression: \d[^\d]

If the equals attribute is a character class, the equals and except attributes can be used together in order to exclude characters from the matching set.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="[a-z]" except="[d-w]"/>

    </expression>

</regexml>
```

Matches: a, b, c, x, y, z

Regular expression: [[a-z]&&[^d-w]]

If equals is not a character class, the except attribute will be ignored. Since the except attribute always represents a character class, the opening and closing brackets are optional as shown here:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="[a-g]" except="bdf"/>

    </expression>

</regexml>
```

Matches: a, c, e, g

Regular expression: [[a-g]&&[^bdf]]

Though they are optional, including the brackets may make the expression easier for others to understand by reinforcing the fact that the except attribute is a character class. For this reason, dropping the brackets may be most appropriate when excluding only a single character like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="[a-z]" except="d"/>

    </expression>

</regexml>
```

Matches: letters from a through z except for d

Regular expression: [[a-z]&&[^d]]

## Nested Groups Match
Regexml supports nesting of groups to any level.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <group min="0">

            <match equals="a"/>

            <group min="0">

                <match equals="b"/>

                <match equals="[cd]"/>

            </group>

            <match equals="e"/>

        </group>

        <match equals="f"/>

    </expression>

</regexml>
```

Matches: aef, abcef, abdef, f

Regular expression: (?:a(?:b[cd])?e)?f

## "Or" Group Match
Using the group elements operator attribute, a collection of match or group elements can be combined using an "or" operation instead of the default "and". This allows a match to occur on the first match or the second or the third, and so on.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <group operator="or">

            <match equals="a"/>

            <group>

                <match equals="b"/>

                <match equals="[cd]"/>

            </group>

            <match equals="e"/>

        </group>

    </expression>

</regexml>
```

Matches: a, bc, bd, e

Regular expression: (?:a|(?:b[cd])|e)

## Expressions with XML-restricted Characters Match
Since including the less-than symbol (<) and the double-quote (") in a regexml expression will cause problems with the XML parser, these characters must be escaped when used. The less-than symbol is escaped with &lt; and the double-quote is escaped with &quot;.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="&lt;start matchLineBreaks=&quot;true&quot;/>" capture="true"/>

    </expression>

</regexml>
```

Matches: <start matchLineBreaks="true"/>

Regular expression: <start matchLineBreaks="true"/>

You can avoid having to escape double-quotes if you use single quotes (a.k.a., apostrophe) to enclose an attribute that contains one or more double-quotes.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals='"Hello!"'/>

    </expression>

</regexml>
```

Matches: "Hello!"

Regular expression: "Hello!"

Normally, single-quotes need not be escaped. However, if single-quotes are used instead of double-quotes to enclose the value of an attribute, any single quotes within the value must be escaped.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals='"I like O&apos;Reilly books!"'/>

    </expression>

</regexml>
```

Matches: "I like O'Reilly books!"

Regular expression: "I like O'Reilly books!"

## Start/End of Line Match
The start and end elements can be used to "anchor" an expression to the beginning or end of a line. For example, the following expression matches a zip code only if nothing precedes or follows it.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="zipcode">

        <start/>

        <match equals="\d" min="5"/> <!-- 5 digit zip code -->

        <group min="0">

            <match equals="-"/>

            <match equals="\d" min="4"/> <!-- optional "plus 4" -->

        </group>

        <end/>

    </expression>

</regexml>
```

Matches: any zip code as long as it's the only item in the text being evaluated

Regular expression: ^\d{5}(?:-\d{4})?$

The start and end elements can also be used within groups like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <group operator="or">

            <group>

                <start/>

                <match equals="alpha"/>

                <match equals="." min="0" max="*"/>

            </group>

            <group>

                <match equals="." min="0" max="*"/>

                <match equals="omega"/>

                <end/>

            </group>

        </group>

    </expression>

</regexml>
```

Matches: any string beginning with alpha or ending with omega

Regular expression: (?:(?:^alpha.*)|(?:.*omega$))

## Whole Word Match
The word boundary anchor (\b) can be used to perform "whole word" matches.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="\bScript\b"/>

    </expression>

</regexml>
```

Matches: Script

Does not match: JavaScript, Scriptaculous

Regular expression: \bScript\b

## Partial Word Match
The non-word boundary anchor (\B) can be used to perform "partial word" matches.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="\BScript"/>

    </expression>

</regexml>
```

Matches: JavaScript

Does not match: Script

Regular expression: \BScript

In this example, only a string that does not come at the beginning or end of a word is matched.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="\Bnation\B"/>

    </expression>

</regexml>
```

Matches: internationalization

Does not match: nation, nationalize, international

Regular expression: \Bnation\B

## Capture Match
The values matched within a match or group element can be captured using the capture attribute.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="zipcode">

        <start/>

        <match equals="\d" min="5" capture="true"/> <!-- 5 digit zip code -->

        <group min="0">

            <match equals="-"/>

            <match equals="\d" min="4" capture="true"/> <!-- optional "plus 4" -->

        </group>

        <end/>

    </expression>

</regexml>
```

Captures: the 5 digit and "plus 4" parts of a zip code

Regular expression: ^(\d{5})(?:-(\d{4}))?$

The following Java code extracts the captured elements from a zip code of 98765-4321:


```java.util.regex.Pattern pattern = expressionFactory.getPattern("zipcode");

java.util.regex.Matcher matcher = pattern.matcher("98765-4321");



if (matcher.find())

{

    for (int i = 1; i <= matcher.groupCount(); i++) //iterate through all captured matches

    {

        System.out.println(matcher.group(i));

    }

}
```

The output from this code looks like this:


98765

4321

Note that if only a five digit zip code was provided (e.g., "98765"), the optional "plus 4" group would have been null and the output would look like this:


98765

null

## Match on Previous Match
You can use previous captured matches to match later in an expression by escaping a number associated with the captured match. The first captured match is 1, the second is 2, and so on. For example, suppose we want to match on the time of day where the hour and millisecond are the same and the minute and second are the same like "11:24:24:11".


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="\d\d" capture="true"/>

        <match equals=":"/>

        <match equals="\d\d" capture="true"/>

        <match equals=":"/>

        <match equals="\2"/> <!-- match on second capture -->

        <match equals=":"/>

        <match equals="\1"/> <!-- match on first capture -->

    </expression>

</regexml>
```

Matches: 11:24:24:11, 12:51:51:12, 05:33:33:05, etc.

Regular expression: (\d\d):(\d\d):\2:\1

## Lazy Quantifier Match
Normally, a match element will perform greedy matches. This means that it will attempt to match as much as it can before performing the next match. For example, consider the following input text:


```<p>Paragraph1</p><p>Paragraph2</p><p>Paragraph3</p>```

Now consider using the following regexp expression to process the text above:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="&lt;p>"/>

        <match equals="." max="*" capture="true"/>

        <match equals="&lt;/p>"/>

    </expression>

</regexml>
```

Matches: Paragraph1</p><p>Paragraph2</p><p>Paragraph3

Regular expression: <p>(.+)</p>

The result skips the first two </p> strings because a greedy match will match as much text as possible before finding a suitable ending match (in this case, the third </p> string). If we only want to match text until the first occurrence of a possible ending match, then we must make the match lazy by setting the match element's lazy attribute like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="&lt;p>"/>

        <match equals="." max="*" capture="true" lazy="true"/>

        <match equals="&lt;/p>"/>

    </expression>

</regexml>
```

Matches: Paragraph1

Regular expression: <p>(.+?)</p>

Setting the lazy attribute allows us to match on the first terminating paragraph element rather than the last. The lazy attribute is also supported by the group element when matching a group multiple times.


## Atomic Group Match
Regular expression engines perform their matching by stepping through text from left to right one character at a time. The engine maintains a "backtracking position" so that when it discovers a portion of text doesn't match, it can backtrack to its previous position and attempt a different match (if the match can be performed another way). There are times when backtracking is unnecessary and, therefore, inefficient. Atomic groups provide a way of telling the regular expression engine to not backtrack. This kind of group is called atomic because once a single match is achieved, alternative matches in the group will not be checked. You can read more about atomic grouping here:

http://www.regular-expressions.info/atomic.html'>http://www.regular-expressions.info/atomic.html

An atomic group is specified using the atomic attribute. The match and group elements both support this attribute. In the following example, a one to five digit number surrounded by word boundaries is matched.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="\b"/>

        <match equals="\d" max="5" atomic="true"/>

        <match equals="\b"/>

    </expression>

</regexml>
```

Matches: any one to five digit number immediately preceded by and followed by a word boundary

Regular expression: \b(?>\d{1,5})\b

First consider applying the above expression without the atomic attribute against this text:


Hello 123.

In this case, the regular expression will find a match for the first \b at the space between Hello and 123. It will then use the \d portion of the expression to match the numbers 123. The match will then fail when the period is discovered (since the period is not a digit or a word boundary). At this point, we can safely say that the text does not match the expression. However, without marking the \d match as atomic, the regular expression engine will backtrack and needlessly attempt to match the \b after the 2 and then again after the 1. Including the atomic attribute will avoid unnecessary backtracking and allow the match to fail more quickly.

The following example shows a typical case where using the atomic attribute with a group makes for more efficient matching.


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <group operator="or" atomic="true">

            <match equals="abc"/>            

            <match equals="def"/>

        </group>

        <match equals="\b"/>

    </expression>

</regexml>
```

Matches: abc and def if immediately followed by a word boundary

Regular expression: \b(?>(?:abc|def))\b

Consider what happens when the expression above is applied to this text:


abcd

In this case, the regular expression engine will match the first three characters abc and then move on to attempt a match against a word boundary. This match will fail and, because of the atomic attribute, the regular expression engine will give up immediately and indicate that the text doesn't match the expression. If the atomic attribute had not been set, the regular expression engine would have unnecessarily backtracked and attempted to match the text against the def alternative.


## Lookaround Match
Lookahead and lookbehind groups (collectively known as lookaround groups) are used to match text without including it in the match results. For example, consider the following text:


This is a <p>paragraph</p> in HTML. Here is <p>another</p>.

Say we wanted to match the text between the paragraph tags without including the tags themselves in the match. This can be done using positive lookarounds like this:


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example">

        <match equals="&lt;p>" lookbehind="positive"/>

        <match equals="." max="*" lazy="true"/>

        <match equals="&lt;/p>" lookahead="positive"/>

    </expression>

</regexml>
```

Matches: text between <p> and </p> tags

Regular expression: (?<=<p>).+?(?=</p>)

The lookahead and lookbehind attributes are supported by both group and match elements. Valid values for these attributes are positive, negative, and none. A positive lookaround succeeds if the text matches while a negative lookaround succeeds only if the text doesn't match. Note that we use the lazy attribute for reasons described in the section on lazy matches above. The following code outputs the matching text:


```ExpressionFactory ef = new ExpressionFactory(new FileSystemResource("example.xml"), true);

Pattern pattern = ef.getPattern("example");

Matcher matcher = pattern.matcher("This is a <p>paragraph</p> in HTML. Here is <p>another</p>.");



while (matcher.find())

{

    System.out.println(matcher.group());

}
```

The output from this program looks like this:


paragraph

another

Notice that the paragraph tags were not included even though they were used to match the text. Lookaround groups enable this type of functionality. Finally, the following regexml expression is an example of a negative lookahead. It finds all instances of the letter "p" that are not followed by a greater than symbol (>):


```<regexml xmlns="http://schemas.regexml.org/expressions">

    <expression id="example" ignoreCase="true">

        <match equals="p"/>

        <match equals=">" lookahead="negative"/>

    </expression>

</regexml>
```

Matches: all instances of the letter p not followed by a >

Regular expression: p(?!>)

You can learn more about lookarounds here:

http://www.regular-expressions.info/lookaround.html'>http://www.regular-expressions.info/lookaround.html

