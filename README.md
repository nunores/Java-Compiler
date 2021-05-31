# GROUP: 4d
- NAME1: Diogo André Barbosa Nunes, NR1: 201808546, GRADE1: 16, CONTRIBUTION1: 25%
- NAME2: João Miguel Gomes Gonçalves, NR2: 201806796, GRADE2: 16, CONTRIBUTION2: 25%
- NAME3: Marina Tostões Fernandes Leitão Dias, NR3: 201806787, GRADE3: 16, CONTRIBUTION3: 25%
- NAME4: Nuno Filipe Ferreira de Sousa Resende, NR4: 201806825, GRADE4: 16, CONTRIBUTION4: 25%

**GLOBAL Grade of the project: 16**
We believe that this project easily reflects the work that was put into by our team. Although we are missing the optimizations, we made sure to implement all the necessary features, going through each checkpoint throughly.

**SUMMARY:** (Describe what your tool does and its main features.)

The objective of this project was to implement a tool that would essentially compile a simplified java class definition into bytecodes so that it would be possible to run it and produce the expected results, as the original javac would.
The compiler follows a very logical structure, being divided in lexical analysis with comment ignoring, followed by the syntactic analysis with error recovery mechanisms, OLLIR and jasmin representations.

**DEALING WITH SYNTACTIC ERRORS:** (Describe how the syntactic error recovery of your tool works. Does it exit after the first error?)



**SEMANTIC ANALYSIS:** (Refer the semantic rules implemented by your tool.)

**CODE GENERATION:** (describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)

**TASK DISTRIBUTION:** (Identify the set of tasks done by each member of the project. You can divide this by checkpoint it if helps)

**PROS:** (Identify the most positive aspects of your tool)

**CONS:** (Identify the most negative aspects of your tool)

# Compilers Project

For this project, you need to [install Gradle](https://gradle.org/install/)

## Project setup

Copy your ``.jjt`` file to the ``javacc`` folder. If you change any of the classes generated by ``jjtree`` or ``javacc``, you also need to copy them to the ``javacc`` folder.

Copy your source files to the ``src`` folder, and your JUnit test files to the ``test`` folder.

## Compile

To compile the program, run ``gradle build``. This will compile your classes to ``classes/main/java`` and copy the JAR file to the root directory. The JAR file will have the same name as the repository folder.

### Run

To run you have two options: Run the ``.class`` files or run the JAR.

### Run ``.class``

To run the ``.class`` files, do the following:

```cmd
java -cp "./build/classes/java/main/" <class_name> <arguments>
```

Where ``<class_name>`` is the name of the class you want to run and ``<arguments>`` are the arguments to be passed to ``main()``.

### Run ``.jar``

To run the JAR, do the following command:

```cmd
java -jar <jar filename> <arguments>
```

Where ``<jar filename>`` is the name of the JAR file that has been copied to the root folder, and ``<arguments>`` are the arguments to be passed to ``main()``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``build/reports/tests/test/index.html``.

## Checkpoint 1
For the first checkpoint the following is required:

1. [x] Convert the provided e-BNF grammar into JavaCC grammar format in a .jj file
2. [x] Resolve grammar conflicts (projects with global LOOKAHEAD > 1 will have a penalty)
3. [x] Proceed with error treatment and recovery mechanisms for the while expression
4. [x] Convert the .jj file into a .jjt file
5. [x] Include missing information in nodes (i.e. tree annotation). E.g. include class name in the class Node.
6. [x] Generate a JSON from the AST

## Checkpoint 2
For the second checkpoint the following is required:

1. [x] Implement the interfaces that will allow the generation of the JSON files representing the source code and the necessary symbol tables
2. [x] Implement the Semantic Analsis and generate the LLIR code, OLLIR, from the AST
3. [x] Generate grom the OLLIR the JVM code accepted by jasmin corresponding tothe invocation of functions in Java--
4. [x] Generate from the OLLIR JVM code accepted by jasmin for arithmetic expressions


### JavaCC to JSON
To help converting the JavaCC nodes into a JSON format, we included in this project the JmmNode interface, which can be seen in ``src-lib/pt/up/fe/comp/jmm/JmmNode.java``. The idea is for you to use this interface along with your SimpleNode class. Then, one can easily convert the JmmNode into a JSON string by invoking the method JmmNode.toJson().

Please check the SimpleNode included in this repository to see an example of how the interface can be implemented, which implements all methods except for the ones related to node attributes. How you should store the attributes in the node is left as an exercise.

### Reports
We also included in this project the class ``src-lib/pt/up/fe/comp/jmm/report/Report.java``. This class is used to generate important reports, including error and warning messages, but also can be used to include debugging and logging information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage in which the error occurred.


### Parser Interface

We have included the interface ``src-lib/pt/up/fe/comp/jmm/JmmParser.java``, which you should implement in a class that has a constructor with no parameters (please check ``src/Main.java`` for an example). This class will be used to test your parser. The interface has a single method, ``parse``, which receives a String with the code to parse, and returns a JmmParserResult instance. This instance contains the root node of your AST, as well as a List of Report instances that you collected during parsing.

To configure the name of the class that implements the JmmParser interface, use the file ``parser.properties``.