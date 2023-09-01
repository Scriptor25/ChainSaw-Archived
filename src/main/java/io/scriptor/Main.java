package io.scriptor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import io.scriptor.chainsaw.Lexer;
import io.scriptor.chainsaw.Parser;
import io.scriptor.chainsaw.runtime.Interpreter;

class Main {

    public static void main(String[] args) throws IOException {

        if (args.length == 0) { // interpreter shell

            var interpreter = new Interpreter();
            while (true) {

                System.out.print(">> ");
                String source = System.console().readLine();

                if (source.equals("--exit"))
                    break;
                if (source.equals("--clear")) {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                    continue;
                }

                try {
                    var tokens = Lexer.tokenize(source);
                    var parser = new Parser(tokens);
                    var program = parser.parseProgram();

                    System.out.println(interpreter.evaluate(program));
                } catch (Exception e) {
                    System.out.println("Runtime Error: " + e.getMessage());
                }
            }

        } else if (args.length == 1) { // run file

            String source = "";
            try (var reader = new BufferedReader(new FileReader(args[0]))) {

                var builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    builder.append(line).append('\n');

                source = builder.toString();
            }

            var tokens = Lexer.tokenize(source);
            var parser = new Parser(tokens);
            var program = parser.parseProgram();

            var interpreter = new Interpreter();
            interpreter.evaluate(program);

        } else
            System.out.println("Use 'csaw' to run the interpreter shell, or 'csaw <filepath>' to run a file");
    }
}