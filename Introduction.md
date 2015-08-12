# Project Description #

The regexml Java library provides a way to define regular expressions in a verbose XML vocabulary rather than the terse and cryptic format currently in use. By defining and documenting complex expressions in XML, regular expressions are easier to understand, debug, and maintain.

# Library Installation #

In order to use the regexml Java library, simply add the _regexml-x.x.x.jar_ file to your project's classpath. Once added, four public classes and one interface will be available to your application. Each of these classes and the interface are described in detail in the following section.

# Java API #

The regexml library API is very simple. There are only four classes and one interface to learn as shown here:

| **Class / Interface** | **Description** |
|:----------------------|:----------------|
| `Resource`            | Interface implemented by classes that encapsulate a regexml expressions file (i.e., `ClassPathResource` and `FileSystemResource`). |
| `ClassPathResource`   | Represents an XML file available on the classpath that contains regexml expressions (implements the `Resource` interface). |
| `FileSystemResource`  | Represents an XML file available on the file system that contains regexml expressions (implements the `Resource` interface). |
| `Expression`          | Provides access to a regexml expression's `java.util.regex.Pattern` object and a text representation of the expression in standard regular expression syntax. |
| `ExpressionFactory`   | Parses the XML file represented by a `ClassPathResource` or `FileSystemResource` object and creates a `java.util.regex.Pattern` object for each expression defined in the XML. The `Pattern` object can then be used to extract portions of a string or match text against the pattern. |

The javadoc is available [here](http://regexml.googlecode.com/svn/trunk/javadoc/index.html). Now let's take a look at each of these classes and interface.

## Resource Interface and Classes ##

The `Resource` interface represents an object through which a regexml expressions file can be accessed. The `ClassPathResource` and `FileSystemResource` classes implement `Resource` and are used to access a regexml expressions file via the classpath or file system, respectively. `ClassPathResource` is instantiated like this:

```
Resource cpResource = new ClassPathResource("expressions.xml");
```

`FileSystemResource` is instantiated like this:

```
Resource fsResource = new FileSystemResource("/home/user/expressions.xml");
```

Or like this:

```
File file = new File("/home/user/expressions.xml");
Resource fsResource = new FileSystemResource(file);
```

Depending on the type of resource implementation you choose, the regexml expressions file can be accessed from either the classpath or the file system. Keep in mind that after an application is packaged for distribution, it's usually easier to access the expressions file from the classpath rather than the file system.

## Expression Class ##

The `Expression` class encapsulates information related to a single regexml expression. This class exposes the following public methods:

| **Method** | **Description** |
|:-----------|:----------------|
| `getId()`  | Gets the ID for this expression as specified in the regexml expressions file. |
| `getPattern()` | Gets the `java.util.regex.Pattern` object associated with this expression. |
| `getRegExString()` | Gets a text representation of the expression in traditional regular expression syntax. |

Typically, you will only be interested in retrieving `Pattern` objects in order to evaluate regular expressions. In that case, the `ExpressionFactory` class provides a convenience method called `getPattern()` that allows you to bypass the `Expression` object and retrieve the `Pattern` object directly. Regardless, the `Expression` object is available for times when you need to access the expression's ID or see the expression in traditional regular expression syntax.

## ExpressionFactory Class ##

The `ExpressionFactory` class compiles regexml expressions into standard `java.util.regex.Pattern` objects, caches them, and makes them available to client applications via the `getExpression()` and `getPattern()` methods. Given that regexml expressions are converted to standard `Pattern` objects, performance of regexml expressions is equivalent to that of standard regular expressions.

`ExpressionFactory` can be instantiated like this:

```
ExpressionFactory expressionFactory = new ExpressionFactory(new ClassPathResource("expressions.xml"));
```

Furthermore, there is a second `ExpressionFactory` constructor that accepts a boolean value indicating whether or not the given expressions file should be validated against the regexml schema. To validate the expressions file during instantiation, pass a second parameter to the constructor like this:

```
ExpressionFactory expressionFactory = new ExpressionFactory(new ClassPathResource("expressions.xml"), true);
```

`ExpressionFactory` exposes the following public methods:

| **Method** | **Description** |
|:-----------|:----------------|
| `getExpression(String id)` | Gets the `Expression` object associated with the given expression ID. |
| `getPattern(String id)` | Gets the `java.util.regex.Pattern` object associated with the given expression ID. |

Once the `ExpressionFactory` is instantiated, it can be used to evaluate regexml expressions. For example, given this regexml expressions file:

```
<regexml xmlns="http://schemas.regexml.org/expressions">
    <expression id="phone">
        <start/>
        <match equals="("/>
        <match equals="\d" min="3"/> <!-- area code -->
        <match equals=") "/>
        <match equals="\d" min="3"/> <!-- prefix -->
        <match equals="-"/>
        <match equals="\d" min="4"/>
        <end/>
    </expression>
</regexml>
```

We can validate a phone number with the following Java code:

```
Expression expr = expressionFactory.getExpression("phone");

java.util.regex.Pattern pattern = expr.getPattern();
java.util.regex.Matcher matcher = pattern.matcher("(801) 555-1212");

if (matcher.matches())
{
    System.out.println("Thanks for the valid phone number!");
}
else
{
    System.out.println("This phone number is invalid! Please use this format: (XXX) XXX-XXXX");
}

System.out.println("Expression in regular expression syntax: " + expr.getRegExString());
```

The output from this program looks like this:

> `Thanks for the valid phone number!`<br>
<blockquote><code>Expression in regular expression syntax: ^\((\d{3})\) (\d{3})-(\d{4})$</code></blockquote>

The code above retrieves an <code>Expression</code> object because it's used to output the expression in regular expression syntax. If all we want to do is validate a phone number, we can bypass the <code>Expression</code> object and retrieve the <code>java.util.regex.Pattern</code> object directly like this:<br>
<br>
<pre><code>java.util.regex.Pattern pattern = expressionFactory.getPattern("phone");<br>
java.util.regex.Matcher matcher = pattern.matcher("(801) 555-1212");<br>
<br>
if (matcher.matches())<br>
{<br>
    System.out.println("Thanks for the valid phone number!");<br>
}<br>
else<br>
{<br>
    System.out.println("This phone number is invalid! Please use this format: (XXX) XXX-XXXX");<br>
}<br>
</code></pre>

In addition to validating a phone number, we can also extract information from it by adding <code>capture</code> attributes to the regexml expression like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="phone"&gt;<br>
        &lt;start/&gt;<br>
        &lt;match equals="("/&gt;<br>
        &lt;match equals="\d" min="3" capture="true"/&gt; &lt;!-- area code --&gt;<br>
        &lt;match equals=") "/&gt;<br>
        &lt;match equals="\d" min="3" capture="true"/&gt; &lt;!-- prefix --&gt;<br>
        &lt;match equals="-"/&gt;<br>
        &lt;match equals="\d" min="4"/&gt;<br>
        &lt;end/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

The expression above will allow us to extract the area code and prefix from any valid phone number. The Java code looks like this:<br>
<br>
<pre><code>java.util.regex.Pattern pattern = expressionFactory.getPattern("phone");<br>
java.util.regex.Matcher matcher = pattern.matcher("(801) 555-1212");<br>
<br>
if (matcher.find())<br>
{<br>
    for (int i = 1; i &lt;= matcher.groupCount(); i++) //iterate through all captured matches<br>
    {<br>
        System.out.println(matcher.group(i));<br>
    }<br>
}<br>
</code></pre>

The output from this program looks like this:<br>
<br>
<blockquote><code>801</code><br>
<code>555</code></blockquote>

The <code>Matcher</code> object creates a group for each <code>&lt;match/&gt;</code> element whose <code>capture</code> attribute is set. For more information on using the Java <code>Pattern</code> and <code>Matcher</code> classes, see the javadocs here:<br>
<br>
<a href='http://java.sun.com/javase/6/docs/api/java/util/regex/Pattern.html'>http://java.sun.com/javase/6/docs/api/java/util/regex/Pattern.html</a><br>
<a href='http://java.sun.com/javase/6/docs/api/java/util/regex/Matcher.html'>http://java.sun.com/javase/6/docs/api/java/util/regex/Matcher.html</a>

For more details on the regexml classes, see the <a href='http://regexml.googlecode.com/svn/trunk/javadoc/index.html'>regexml javadoc</a>.<br>
<br>
<h1>Expressions File</h1>

The expressions file is the heart of the regexml library. It is in this file that regular expressions are defined in a simple XML format. Only one expressions file is needed per application since a single file can contain any number of regular expressions.<br>
<br>
<h2>Create the File</h2>

In order to simplify creation of the regexml expressions file, a schema is provided that describes the structure of a valid file. In order to retrieve the XML schema, you'll need to open the regexml JAR in a compression utility (e.g., WinZip, jZip, StuffIt, Zipeg, etc.) or the Java command-line <i>jar</i> utility and extract the file named <i>regexml.xsd</i>. Using the <i>jar</i> utility that ships with Java, you can extract the schema with this command:<br>
<br>
<blockquote><code>jar -xf regexml-x.x.x.jar regexml.xsd</code></blockquote>

Once the schema is available in the file system, you can reference it from any XML editor that supports schemas by associating it with the following namespace:<br>
<br>
<blockquote><code>http://schemas.regexml.org/expressions</code></blockquote>

For example, in IntelliJ IDEA (my IDE of choice), bring up the "Settings" dialog box and select "Resources". Then click the "Add..." button in the "Configure External Resources" section and enter a URI of <code>http://schemas.regexml.org/expressions</code> as well as the path to the schema file under Location. Once the regexml namespace is associated with the schema, IDEA will assist you when creating regexml expression files by showing the valid elements and attributes available at any point in the document. Just be sure to use a <i>.xml</i> file extension and include the regexml expressions namespace at the beginning of the file like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Other schema-aware XML editors should have similar ways to associate a namespace with a schema file. The structure of the expressions file is discussed in detail in the following sections.<br>
<br>
<h2>File Format</h2>

Detailed documentation regarding the regexml expressions file format is available <a href='ExpressionsFileFormat.md'>here</a>.<br>
<br>
<h2>Expression Matching Examples</h2>

The following sections provide examples of various regexml expressions.<br>
<br>
<h3>Empty File</h3>

The simplest valid expressions file is a file that defines no expressions like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Even though it doesn't define any expressions, this file still satisfies the schema because it uses the <code>&lt;regexml&gt;</code> root element and the correct regexml expressions namespace.<br>
<br>
<h3>Literal Match</h3>

About the simplest expressions file that may be useful is one that includes a single expression that performs a literal match.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="a"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i><br>
Regular expression: <code>a</code>

<h3>Ignore Case Matches</h3>

There are two ways to ignore case (i.e., capitalization) when performing text comparisons (by default, text comparisons are case sensitive). First, the ignore case directive can be applied to an entire expression by setting the <code>expression</code> element's <code>ignoreCase</code> attribute like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example" ignoreCase="true"&gt;<br>
        &lt;match equals="a"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i>, <i>A</i><br>
Regular expression: <code>a</code>

Notice that the traditional regular expression doesn't change in this case. That is due to the fact that the "ignore case" option is set by passing a parameter to the constructor of the <code>java.util.regex.Pattern</code> object. Therefore, the <code>Pattern</code> object returned from the <code>ExpressionFactory</code> will be configured to ignore case in all text comparisons. The second way to ignore case is to set the <code>ignoreCase</code> attribute on individual <code>match</code> or <code>group</code> elements like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="a" ignoreCase="true"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i>, <i>A</i><br>
Regular expression: <code>(?i)a(?-i)</code>

In this instance, the "ignore case" directive is part of the regular expression rather than a setting within the <code>Pattern</code> object. Case sensitivity can be turned on for an entire group like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="a"/&gt;<br>
        &lt;group ignoreCase="true"&gt;<br>
            &lt;match equals="b" min="0"/&gt;<br>
            &lt;match equals="c"/&gt;<br>
        &lt;/group&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>ac</i>, <i>aC</i>, <i>abc</i>, <i>aBc</i>, <i>aBC</i>, <i>abC</i><br>
Regular expression: <code>a(?i:b?c)</code>

<h3>Multiple Matches</h3>

Since matches are automatically concatenated (unless included in an "or" group discussed later), the following two regexml expressions are equivalent.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="a"/&gt;<br>
        &lt;match equals="b"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="ab"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>ab</i><br>
Regular expression: <code>ab</code>

<h3>Optional Match</h3>

A match can be made optional by setting it's <code>min</code> attribute to zero (<code>min</code> defaults to one). For example, the <code>a</code> in the following regexml expression is required for a match but the <code>b</code> is optional.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="a"/&gt;<br>
        &lt;match equals="b" min="0"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i>, <i>ab</i><br>
Regular expression: <code>ab?</code>

<h3>Exact Quantity Match</h3>

Whenever the <code>min</code> attribute is greater than the <code>max</code> attribute, <code>max</code> is automatically set equal to <code>min</code>. This behavior allows an exact number of matches to be specified by setting only the <code>min</code> attribute (since <code>max</code> defaults to one, any <code>min</code> amount greater than one will cause <code>max</code> to be assigned the same value).<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="a" min="3"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>aaa</i><br>
Regular expression: <code>a{3}</code>

<h3>Maximum Quantity Match</h3>

If only a <code>max</code> value is set, the default <code>min</code> value of one will be used.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="a" max="3"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i>, <i>aa</i>, <i>aaa</i><br>
Regular expression: <code>a{1,3}</code>

<h3>Quantity Range Match</h3>

Setting the <code>min</code> and <code>max</code> attributes allows a quantity range to be specified.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="a" min="2" max="4"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>aa</i>, <i>aaa</i>, <i>aaaa</i><br>
Regular expression: <code>a{2,4}</code>

<h3>Unbounded Quantity Range Match</h3>

Typically, the <code>min</code> and <code>max</code> attributes will be set to non-negative integer values. However, to support unbounded maximum values, the value of the <code>max</code> attribute can be set to an asterisk (<code>*</code>). This indicates an infinite number.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="a" min="2" max="*"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>aa</i>, <i>aaa</i>, <i>aaaa</i>, ...<br>
Regular expression: <code>a{2,}</code>

<h3>Group Quantity Match</h3>

The <code>min</code> and <code>max</code> quantity attributes work with groups the same as they do with individual matches.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;group&gt;<br>
            &lt;match equals="a"/&gt;            <br>
        &lt;/group&gt;<br>
        &lt;group min="0"&gt;<br>
            &lt;match equals="b"/&gt;<br>
            &lt;match equals="c"/&gt;<br>
        &lt;/group&gt;<br>
        &lt;group max="2"&gt;<br>
            &lt;match equals="d"/&gt;<br>
        &lt;/group&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>ad</i>, <i>add</i>, <i>abcd</i>, <i>abcdd</i><br>
Regular expression: <code>(?:a)(?:bc)?(?:d){1,2}</code>

<h3>Character Class Match</h3>

A <i>character class</i> defines a set of characters enclosed in brackets that is used to match one of many characters. If no quantity modifiers are specified (i.e., <code>min</code> and <code>max</code> attributes), a character class matches a single character. For example, the character class <code>[abc]</code> matches a single character <i>a</i>, <i>b</i>, or <i>c</i>. Applying quantity modifiers allows any number of characters to be matched.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="[abc]" max="2"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i>, <i>b</i>, <i>c</i>, <i>aa</i>, <i>ab</i>, <i>ac</i>, <i>ba</i>, <i>bb</i>, <i>bc</i>, <i>ca</i>, <i>cb</i>, <i>cc</i><br>
Regular expression: <code>[abc]{1,2}</code>

<h3>Character Class Range Match</h3>

The hyphen character is used to represent a range of characters within a character class. For example, the character class <code>[a-z]</code> represents all lowercase letters. Since it has special meaning, the hyphen must be escaped within character classes if wished to be used literally. For example, the character class <code>[a\-z]</code> represents the characters <i>a</i>, <i>-</i>, and <i>z</i> rather than a range of letters from <i>a</i> to <i>z</i>.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="[a-c1-3]"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i>, <i>b</i>, <i>c</i>, <i>1</i>, <i>2</i>, <i>3</i><br>
Regular expression: <code>[a-c1-3]</code>

Characters can be removed from a range using the <code>except</code> attribute like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="[a-z]" except="[d-w]"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i>, <i>b</i>, <i>c</i>, <i>x</i>, <i>y</i>, <i>z</i><br>
Regular expression: <code>[[a-z]&amp;&amp;[^d-w]]</code>

See the "Negated Match" section below for more information about the <code>except</code> attribute.<br>
<br>
<h3>Predefined Character Class Matches</h3>

For convenience, regular expressions offer a few predefined character classes. The following predefined character classes are available:<br>
<br>
<table><thead><th> <b>Characters</b> </th><th> <b>Description</b> </th></thead><tbody>
<tr><td> <code>.</code>    </td><td> Any character      </td></tr>
<tr><td> <code>\d</code>   </td><td> Digit <code>[0-9]</code> </td></tr>
<tr><td> <code>\D</code>   </td><td> Non-digit <code>[^0-9]</code> </td></tr>
<tr><td> <code>\s</code>   </td><td> Whitespace character <code>[ \t\n\f\r]</code> </td></tr>
<tr><td> <code>\S</code>   </td><td> Non-whitespace character <code>[^ \t\n\f\r]</code> </td></tr>
<tr><td> <code>\w</code>   </td><td> Word character <code>[a-zA-Z_0-9]</code> </td></tr>
<tr><td> <code>\W</code>   </td><td> Non-word character <code>[^a-zA-Z_0-9]</code> </td></tr>
<tr><td> <code>\b</code>   </td><td> Word boundary anchor (break between words) </td></tr>
<tr><td> <code>\B</code>   </td><td> Non-word boundary anchor </td></tr></tbody></table>

As shown in the table above, the predefined character class <code>\d</code> is equivalent to the user-defined character class <code>[0-9]</code>. Often times regexml expressions can be simplified through the use of these predefined character classes.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="."/&gt;<br>
        &lt;match equals="\d" min="3"/&gt;<br>
        &lt;match equals="\s"/&gt;<br>
        &lt;match equals="\w" min="3"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: any character followed by three digits followed by one whitespace character followed by three word characters<br>
Regular expression: <code>.\d{3}\s\w{3}</code>

<h3>Negated Match</h3>

The <code>except</code> attribute is the opposite of the match element's <code>equals</code> attribute. Rather than "match this", it basically means "match everything except this". It's important to keep in mind that the <code>except</code> attribute always represents a character class.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="\d"/&gt;<br>
        &lt;match except="\d"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: one digit followed by one non-digit character<br>
Regular expression: <code>\d[^\d]</code>

If the <code>equals</code> attribute is a character class, the <code>equals</code> and <code>except</code> attributes can be used together in order to exclude characters from the matching set.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="[a-z]" except="[d-w]"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i>, <i>b</i>, <i>c</i>, <i>x</i>, <i>y</i>, <i>z</i><br>
Regular expression: <code>[[a-z]&amp;&amp;[^d-w]]</code>

If <code>equals</code> is not a character class, the <code>except</code> attribute will be ignored. Since the <code>except</code> attribute always represents a character class, the opening and closing brackets are optional as shown here:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="[a-g]" except="bdf"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i>, <i>c</i>, <i>e</i>, <i>g</i><br>
Regular expression: <code>[[a-g]&amp;&amp;[^bdf]]</code>

Though they are optional, including the brackets may make the expression easier for others to understand by reinforcing the fact that the <code>except</code> attribute is a character class. For this reason, dropping the brackets may be most appropriate when excluding only a single character like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="[a-z]" except="d"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: letters from <i>a</i> through <i>z</i> except for <i>d</i><br>
Regular expression: <code>[[a-z]&amp;&amp;[^d]]</code>

<h3>Nested Groups Match</h3>

Regexml supports nesting of groups to any level.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;group min="0"&gt;<br>
            &lt;match equals="a"/&gt;<br>
            &lt;group min="0"&gt;<br>
                &lt;match equals="b"/&gt;<br>
                &lt;match equals="[cd]"/&gt;<br>
            &lt;/group&gt;<br>
            &lt;match equals="e"/&gt;<br>
        &lt;/group&gt;<br>
        &lt;match equals="f"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>aef</i>, <i>abcef</i>, <i>abdef</i>, <i>f</i><br>
Regular expression: <code>(?:a(?:b[cd])?e)?f</code>

<h3>"Or" Group Match</h3>

Using the <code>group</code> elements <code>operator</code> attribute, a collection of <code>match</code> or <code>group</code> elements can be combined using an "or" operation instead of the default "and". This allows a match to occur on the first match <i>or</i> the second <i>or</i> the third, and so on.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;group operator="or"&gt;<br>
            &lt;match equals="a"/&gt;<br>
            &lt;group&gt;<br>
                &lt;match equals="b"/&gt;<br>
                &lt;match equals="[cd]"/&gt;<br>
            &lt;/group&gt;<br>
            &lt;match equals="e"/&gt;<br>
        &lt;/group&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>a</i>, <i>bc</i>, <i>bd</i>, <i>e</i><br>
Regular expression: <code>(?:a|(?:b[cd])|e)</code>

<h3>Expressions with XML-restricted Characters Match</h3>

Since including the less-than symbol (<code>&lt;</code>) and the double-quote (<code>"</code>) in a regexml expression will cause problems with the XML parser, these characters must be escaped when used. The less-than symbol is escaped with <code>&amp;lt;</code> and the double-quote is escaped with <code>&amp;quot;</code>.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="&amp;lt;start matchLineBreaks=&amp;quot;true&amp;quot;/&gt;" capture="true"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <code>&lt;</code><i>start matchLineBreaks="true"/</i><code>&gt;</code><br>
Regular expression: <code>&lt;start matchLineBreaks="true"/&gt;</code>

You can avoid having to escape double-quotes if you use single quotes (a.k.a., apostrophe) to enclose an attribute that contains one or more double-quotes.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals='"Hello!"'/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>"Hello!"</i><br>
Regular expression: <code>"Hello!"</code>

Normally, single-quotes need not be escaped. However, if single-quotes are used instead of double-quotes to enclose the value of an attribute, any single quotes within the value must be escaped.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals='"I like O&amp;apos;Reilly books!"'/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>"I like O'Reilly books!"</i><br>
Regular expression: <code>"I like O'Reilly books!"</code>

<h3>Start/End of Line Match</h3>

The <code>start</code> and <code>end</code> elements can be used to "anchor" an expression to the beginning or end of a line. For example, the following expression matches a zip code only if nothing precedes or follows it.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="zipcode"&gt;<br>
        &lt;start/&gt;<br>
        &lt;match equals="\d" min="5"/&gt; &lt;!-- 5 digit zip code --&gt;<br>
        &lt;group min="0"&gt;<br>
            &lt;match equals="-"/&gt;<br>
            &lt;match equals="\d" min="4"/&gt; &lt;!-- optional "plus 4" --&gt;<br>
        &lt;/group&gt;<br>
        &lt;end/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: any zip code as long as it's the only item in the text being evaluated<br>
Regular expression: <code>^\d{5}(?:-\d{4})?$</code>

The <code>start</code> and <code>end</code> elements can also be used within groups like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;group operator="or"&gt;<br>
            &lt;group&gt;<br>
                &lt;start/&gt;<br>
                &lt;match equals="alpha"/&gt;<br>
                &lt;match equals="." min="0" max="*"/&gt;<br>
            &lt;/group&gt;<br>
            &lt;group&gt;<br>
                &lt;match equals="." min="0" max="*"/&gt;<br>
                &lt;match equals="omega"/&gt;<br>
                &lt;end/&gt;<br>
            &lt;/group&gt;<br>
        &lt;/group&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: any string beginning with <i>alpha</i> or ending with <i>omega</i><br>
Regular expression: <code>(?:(?:^alpha.*)|(?:.*omega$))</code>

<h3>Whole Word Match</h3>

The word boundary anchor (<code>\b</code>) can be used to perform "whole word" matches.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="\bScript\b"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>Script</i><br>
Does not match: <i>JavaScript</i>, <i>Scriptaculous</i><br>
Regular expression: <code>\bScript\b</code>

<h3>Partial Word Match</h3>

The non-word boundary anchor (<code>\B</code>) can be used to perform "partial word" matches.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="\BScript"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>JavaScript</i><br>
Does not match: <i>Script</i><br>
Regular expression: <code>\BScript</code>

In this example, only a string that does not come at the beginning or end of a word is matched.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="\Bnation\B"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>internationalization</i><br>
Does not match: <i>nation</i>, <i>nationalize</i>, <i>international</i><br>
Regular expression: <code>\Bnation\B</code>

<h3>Capture Match</h3>

The values matched within a <code>match</code> or <code>group</code> element can be captured using the <code>capture</code> attribute.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="zipcode"&gt;<br>
        &lt;start/&gt;<br>
        &lt;match equals="\d" min="5" capture="true"/&gt; &lt;!-- 5 digit zip code --&gt;<br>
        &lt;group min="0"&gt;<br>
            &lt;match equals="-"/&gt;<br>
            &lt;match equals="\d" min="4" capture="true"/&gt; &lt;!-- optional "plus 4" --&gt;<br>
        &lt;/group&gt;<br>
        &lt;end/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Captures: the 5 digit and "plus 4" parts of a zip code<br>
Regular expression: <code>^(\d{5})(?:-(\d{4}))?$</code>

The following Java code extracts the captured elements from a zip code of <i>98765-4321</i>:<br>
<br>
<pre><code>java.util.regex.Pattern pattern = expressionFactory.getPattern("zipcode");<br>
java.util.regex.Matcher matcher = pattern.matcher("98765-4321");<br>
<br>
if (matcher.find())<br>
{<br>
    for (int i = 1; i &lt;= matcher.groupCount(); i++) //iterate through all captured matches<br>
    {<br>
        System.out.println(matcher.group(i));<br>
    }<br>
}<br>
</code></pre>

The output from this code looks like this:<br>
<br>
<blockquote><code>98765</code><br>
<code>4321</code></blockquote>

Note that if only a five digit zip code was provided (e.g., "98765"), the optional "plus 4" group would have been null and the output would look like this:<br>
<br>
<blockquote><code>98765</code><br>
<code>null</code></blockquote>

<h3>Match on Previous Match</h3>

You can use previous captured matches to match later in an expression by escaping a number associated with the captured match. The first captured match is 1, the second is 2, and so on. For example, suppose we want to match on the time of day where the hour and millisecond are the same and the minute and second are the same like "11:24:24:11".<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="\d\d" capture="true"/&gt;<br>
        &lt;match equals=":"/&gt;<br>
        &lt;match equals="\d\d" capture="true"/&gt;<br>
        &lt;match equals=":"/&gt;<br>
        &lt;match equals="\2"/&gt; &lt;!-- match on second capture --&gt;<br>
        &lt;match equals=":"/&gt;<br>
        &lt;match equals="\1"/&gt; &lt;!-- match on first capture --&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>11:24:24:11</i>, <i>12:51:51:12</i>, <i>05:33:33:05</i>, etc.<br>
Regular expression: <code>(\d\d):(\d\d):\2:\1</code>

<h3>Lazy Quantifier Match</h3>

Normally, a <code>match</code> element will perform <i>greedy</i> matches. This means that it will attempt to match as much as it can before performing the next match. For example, consider the following input text:<br>
<br>
<blockquote><code>&lt;p&gt;Paragraph1&lt;/p&gt;&lt;p&gt;Paragraph2&lt;/p&gt;&lt;p&gt;Paragraph3&lt;/p&gt;</code></blockquote>

Now consider using the following regexp expression to process the text above:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="&amp;lt;p&gt;"/&gt;<br>
        &lt;match equals="." max="*" capture="true"/&gt;<br>
        &lt;match equals="&amp;lt;/p&gt;"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>Paragraph1<code>&lt;</code>/p<code>&gt;&lt;</code>p<code>&gt;</code>Paragraph2<code>&lt;</code>/p<code>&gt;&lt;</code>p>Paragraph3</i><br>
Regular expression: <code>&lt;p&gt;(.+)&lt;/p&gt;</code>

The result skips the first two <code>&lt;/p&gt;</code> strings because a greedy match will match as much text as possible before finding a suitable ending match (in this case, the third <code>&lt;/p&gt;</code> string). If we only want to match text until the first occurrence of a possible ending match, then we must make the match <i>lazy</i> by setting the <code>match</code> element's <code>lazy</code> attribute like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="&amp;lt;p&gt;"/&gt;<br>
        &lt;match equals="." max="*" capture="true" lazy="true"/&gt;<br>
        &lt;match equals="&amp;lt;/p&gt;"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>Paragraph1</i><br>
Regular expression: <code>&lt;p&gt;(.+?)&lt;/p&gt;</code>

Setting the <code>lazy</code> attribute allows us to match on the first terminating paragraph element rather than the last. The <code>lazy</code> attribute is also supported by the <code>group</code> element when matching a group multiple times.<br>
<br>
<h3>Atomic Group Match</h3>

Regular expression engines perform their matching by stepping through text from left to right one character at a time. The engine maintains a "backtracking position" so that when it discovers a portion of text doesn't match, it can backtrack to its previous position and attempt a different match (if the match can be performed another way). There are times when backtracking is unnecessary and, therefore, inefficient. Atomic groups provide a way of telling the regular expression engine to not backtrack. This kind of group is called <i>atomic</i> because once a single match is achieved, alternative matches in the group will not be checked. You can read more about atomic grouping here:<br>
<br>
<a href='http://www.regular-expressions.info/atomic.html'>http://www.regular-expressions.info/atomic.html</a>

An atomic group is specified using the <code>atomic</code> attribute. The <code>match</code> and <code>group</code> elements both support this attribute. In the following example, a one to five digit number surrounded by word boundaries is matched.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="\b"/&gt;<br>
        &lt;match equals="\d" max="5" atomic="true"/&gt;<br>
        &lt;match equals="\b"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: any one to five digit number immediately preceded by and followed by a word boundary<br>
Regular expression: <code>\b(?&gt;\d{1,5})\b</code>

First consider applying the above expression <i>without</i> the <code>atomic</code> attribute against this text:<br>
<br>
<blockquote><code>Hello 123.</code></blockquote>

In this case, the regular expression will find a match for the first <code>\b</code> at the space between Hello and 123. It will then use the <code>\d</code> portion of the expression to match the numbers 123. The match will then fail when the period is discovered (since the period is not a digit or a word boundary). At this point, we can safely say that the text does not match the expression. However, without marking the <code>\d</code> match as atomic, the regular expression engine will backtrack and needlessly attempt to match the <code>\b</code> after the 2 and then again after the 1. Including the <code>atomic</code> attribute will avoid unnecessary backtracking and allow the match to fail more quickly.<br>
<br>
The following example shows a typical case where using the <code>atomic</code> attribute with a group makes for more efficient matching.<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;group operator="or" atomic="true"&gt;<br>
            &lt;match equals="abc"/&gt;            <br>
            &lt;match equals="def"/&gt;<br>
        &lt;/group&gt;<br>
        &lt;match equals="\b"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: <i>abc</i> and <i>def</i> if immediately followed by a word boundary<br>
Regular expression: <code>\b(?&gt;(?:abc|def))\b</code>

Consider what happens when the expression above is applied to this text:<br>
<br>
<blockquote><code>abcd</code></blockquote>

In this case, the regular expression engine will match the first three characters <code>abc</code> and then move on to attempt a match against a word boundary. This match will fail and, because of the <code>atomic</code> attribute, the regular expression engine will give up immediately and indicate that the text doesn't match the expression. If the <code>atomic</code> attribute had not been set, the regular expression engine would have unnecessarily backtracked and attempted to match the text against the <code>def</code> alternative.<br>
<br>
<h3>Lookaround Match</h3>

<i>Lookahead</i> and <i>lookbehind</i> groups (collectively known as <i>lookaround</i> groups) are used to match text without including it in the match results. For example, consider the following text:<br>
<br>
<blockquote><code>This is a &lt;p&gt;paragraph&lt;/p&gt; in HTML. Here is &lt;p&gt;another&lt;/p&gt;.</code></blockquote>

Say we wanted to match the text between the paragraph tags without including the tags themselves in the match. This can be done using positive lookarounds like this:<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example"&gt;<br>
        &lt;match equals="&amp;lt;p&gt;" lookbehind="positive"/&gt;<br>
        &lt;match equals="." max="*" lazy="true"/&gt;<br>
        &lt;match equals="&amp;lt;/p&gt;" lookahead="positive"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: text between <code>&lt;p&gt;</code> and <code>&lt;/p&gt;</code> tags<br>
Regular expression: <code>(?&lt;=&lt;p&gt;).+?(?=&lt;/p&gt;)</code>

The <code>lookahead</code> and <code>lookbehind</code> attributes are supported by both <code>group</code> and <code>match</code> elements. Valid values for these attributes are <code>positive</code>, <code>negative</code>, and <code>none</code>. A <i>positive</i> lookaround succeeds if the text matches while a <i>negative</i> lookaround succeeds only if the text doesn't match. Note that we use the <code>lazy</code> attribute for reasons described in the section on lazy matches above. The following code outputs the matching text:<br>
<br>
<pre><code>ExpressionFactory ef = new ExpressionFactory(new FileSystemResource("example.xml"), true);<br>
Pattern pattern = ef.getPattern("example");<br>
Matcher matcher = pattern.matcher("This is a &lt;p&gt;paragraph&lt;/p&gt; in HTML. Here is &lt;p&gt;another&lt;/p&gt;.");<br>
<br>
while (matcher.find())<br>
{<br>
    System.out.println(matcher.group());<br>
}<br>
</code></pre>

The output from this program looks like this:<br>
<br>
<blockquote><code>paragraph</code><br>
<code>another</code></blockquote>

Notice that the paragraph tags were not included even though they were used to match the text. Lookaround groups enable this type of functionality. Finally, the following regexml expression is an example of a negative lookahead. It finds all instances of the letter "p" that are <i>not</i> followed by a greater than symbol (>):<br>
<br>
<pre><code>&lt;regexml xmlns="http://schemas.regexml.org/expressions"&gt;<br>
    &lt;expression id="example" ignoreCase="true"&gt;<br>
        &lt;match equals="p"/&gt;<br>
        &lt;match equals="&gt;" lookahead="negative"/&gt;<br>
    &lt;/expression&gt;<br>
&lt;/regexml&gt;<br>
</code></pre>

Matches: all instances of the letter <i>p</i> not followed by a <i>></i><br>
Regular expression: <code>p(?!&gt;)</code>

You can learn more about lookarounds here:<br>
<br>
<a href='http://www.regular-expressions.info/lookaround.html'>http://www.regular-expressions.info/lookaround.html</a>

<h3>Other Matching Constructs</h3>

See the page <a href='MatchingConstructs.md'>here</a> for information about more matching constructs.