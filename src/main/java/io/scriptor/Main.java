package io.scriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.scriptor.chainsaw.Lexer;
import io.scriptor.chainsaw.Parser;
import io.scriptor.chainsaw.StringEater;
import io.scriptor.chainsaw.runtime.Interpreter;

class Main {

    public static void main(String[] args) throws IOException {

        var loader = Thread.currentThread().getContextClassLoader();
        var stream = loader.getResourceAsStream("test.csaw");
        var reader = new BufferedReader(new InputStreamReader(stream));

        var source = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            source.append(line).append('\n');

        var lexer = new Lexer(new StringEater(source.toString()));
        var tokens = lexer.tokenize();

        var parser = new Parser(tokens);
        var program = parser.parseProgram();

        // System.out.println(program);

        var interpreter = new Interpreter(program);
        interpreter.evaluate();
    }
}