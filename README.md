# Java-Compiler

A simplified Java class compiler developed for academic purposes. This tool compiles a restricted subset of Java code into executable bytecode, mimicking the behavior of `javac`. 

It includes support for lexical, syntactic, and semantic analysis, and generates intermediate OLLIR and Jasmin representations.

## ✨ Features

The main goal of this project was to build a compiler that can transform simplified Java class definitions into bytecode, allowing the resulting `.class` files to be run and produce expected outputs. The process includes:

- Lexical analysis with comment ignoring
- Syntactic analysis with error recovery
- Semantic analysis with thorough validations
- OLLIR and Jasmin intermediate representations
- Final bytecode generation
- Comprehensive testing included in the `test` directory


## 🧩 Compiler Structure

### 🔍 Lexical Analysis
Identifies tokens and ignores comments.

### 📐 Syntactic Analysis
Parsing, including error recovery mechanisms that can handle and skip up to 10 syntactic errors before terminating.

### ✅ Semantic Analysis
Semantic rules follow the course specification and validate:

#### Variables
- Declaration before use (including arrays)
- Unique definitions
- Type compatibility in assignments
- Function calls on valid class types
- Proper variable scope

#### Functions
- Correct argument count and types
- Valid return values and usage in expressions
- Return initialization
- Ensures `void` functions don’t return a value

#### Arrays
- Indexed expressions are initialized and are integers
- Only array variables are indexed
- `.length` used only on arrays

#### Control Structures
- `if`/`while` conditions must be boolean

#### Classes
- Functions called only on initialized instances
- Class instantiation must lead to function usage

#### Operations
- Logical ops (`&&`, `!`) used with booleans
- Comparison (`<`) used with integers
- Arrays not used in arithmetic or conditional expressions

## ⚙️ Code Generation

### OLLIR
Generated via a custom visitor pattern (similar to PreOrder traversal) that walks the AST and builds a large output string per node type, resulting in correct OLLIR code.

### Jasmin
Generated from OLLIR using provided libraries. Code generation loops through all methods and emits appropriate Jasmin instructions.

## ⚠️ Known Limitations

- Lacks advanced optimizations
- No support for function overloading
