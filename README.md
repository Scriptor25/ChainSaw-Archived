# ChainSaw
ChainSaw - A programming language

# WARNING

This language and its documentation is still in early development and missing many key features. The purpose of this language is yet to be found and **every one using it is doomed to despair till its rise above all other languages out there...** kinda...

# Code Examples

You find a complete version of the raytracer from the ["Raytracing in a weekend"](https://raytracing.github.io/) book under `csaw/rtx` and sometimes there maybe some code left inside the `csaw/test.csaw` from testing...

## How to use

### Interpreter Shell

After running the main method without any arguments you should see '>>' in the terminal. Now you can start using the interpreter shell. Additional available commands are:

 - `--exit`: Exit the shell
 - `--reset`: Reset the interpreter, i.e. delete all existing variables and functions
 - `--clear`: Clear the terminal window (poorly implemented)
 - `--path`: Print out the path from where the shell was started
 - `--pause`: For debugging. To use it set a breakpoint at line 36 in the CSaw main method

### File Interpreter

To run a file simply execute the main method with the filename as the first and only argument.

## Syntax

The language syntax can be both challanging and easy, depending on what you plan to do and how complex you want to have it :)

### Functions

To define a function, use following syntax:

    <name>: <type> [<name>: <type> ...] -> <super> var { ... }

First you define the name of the function. After a colon, you can define the return type. After that you can define the functions parameter list in brackets, each parameter consists of the name, then a colon and last the type. After the parameter list you can define on what `thing` type you have to execute it. More on that later. After that you can define if the function has var args, that means that next to its defined parameters there can be other arguments after that, those are then collected into a list object called `varargs`. Now you can decide if you want to define the function. If not, just put a semicolon, else define the function body enclosed by curly brackets.

Examples:

    hello_world { out("Hello World!%n"); }

    hello [name: string] { out("Hello, %s!%n", name); }

    say -> person var { out("%s says %s", my.name, varargs); }

    fib: number [n: number] { ret n == 0 ? 0 : n == 1 ? 1 : fib(n - 1) + fib(n - 2); }

You can also override or create custom operators, e.g. if you define the type `vec3` and you want to easily add two of them, you could do something like follows:

    (+): vec3 [a: vec3, b: vec3] {
        ret vec3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

It just works like a normal function declaration, with the difference being that you have to write the operator in parenthesis.

### Constructor

Constructors for `thing`s are the same as normal functions except for no return type and a dollar sign to indicate that the function is a constructor:

    $vec3 [x: number, y: number, z: number] {
        my.x = x;
        my.y = y;
        my.z = z;
    }

### Things

You can define your own "types", or better, "structs", or, what they are called here in ChainSaw, "things". Doing this is quite simple, like follows:

    thing: <name> {
        <name>: <type>,
        <name>: <type>,
        ...
    }

Like with the functions you can also first declare it by not defining the body and instead putting a semicolon.

### Include Other Scripts

To include the contents of another ChainSaw program, use the `inc` statement:

    inc "hello.csaw";

    inc "scripts/main/world.csaw";

    inc "../../program/main.csaw";

The path has to be relative by the time being, detection for absolute paths is marked as TODO on my personal (only in my mind existing) TODO list :)

### Flow Control

If, while, for and switch are just like in any other C language:

    if (is_true()) {
        ...
    } else out("Nope.");

    if (ok())
        out("worked!");

    while (someone_there()) talk();

    for (number i = 0; i < 100; i++) {
        out("I'm %d years old!", i);
        eat_cake();
    }

    switch (month) {
        case 1:
            ret "January";
        case 2:
            ret "February";

        default:
            ret "To lazy to add any other month";
    }

### Return

To return from a function, use `ret` with the value you want to return.

    ret "Hi";

    ret 0.12345;

    ret vec3(0, 1, 2);

    ret hello_world();

### Variables

Declare a variable:

    <type> <name> = <value>

Example:

    number n = 1.234;

    string name = "Dude";

To get the value from a variable, just put the variables name:

    string hi = "hello"
    out(hi)             --> output: hello

You can also reassign a variable:

    hi = "hi"

This will return the value the variable was set to:

    out(hi = "hi")          --> output: hi

### Binary Operators

You can following operators for binary operations:

 - `+`: add
 - `-`: subtract
 - `*`: multiply
 - `/`: divide
 - `+=`: add and assign
 - `-=`: subtract and assign
 - `*=`: multiply and assign
 - `/=`: divide and assign
 - `==`: equal
 - `!=`: not equal
 - `<`: less than
 - `>`: greater than
 - `<=`: less than equal
 - `>=`: greater than equal

### Unary Operators

For unary operations:

 - `!`: not
 - `-`: negative

 - `++`: add one
 - `--`: subtract one

### Call Functions

You call a function just like with every other programming language:

    <name>(<args>...);

I don't think I have to explain much more.

### Conditions

I still don't know what they are really called, but just take a look for yourself:

    x ? y : z

or

    <condition> ? <then> : <else>

Like I said...

### Members

Works for function calls and thing-fields:

    <object>.<member>

## Types

### Primitives

There are only four primitives by the time being:

 - `char`
 - `number`
 - `string`
 - `void`

`char`, `number` and `string` are relatively simple to understand. Constant values for `char`s are defined as `'a'`, `'#'` and so on, `number`s are defined like `1`, `0.123`, and so on and last but not least `string`s are defined like `"Hello"`, `"World"`, etc.

`void` is a little bit more complex as it not only serves for the no-return function type but also as a type that can contain any other type, somewhat like the `any` "type" in languages like JavaScript.

### Native Types

When using the Java API you can create custom native types by annotating classes with the `CSawNative` annotation. Everything else is done in the background by the API. One thing to notice is that even though return types of functions can be written as normal java types, the parameters must be of `Value` types from the ChainSaw Java API (package `io.scriptor.chainsaw.runtime.value`). This is due to a technical issue that will be fixed somewhere in near future.

There are some builtin native functions and types that are located in the package `io.scriptor.chainsaw.lang`. I will not list them here because the will definitly change from day to day and it would be a support nightmare to update this list after every little change.
