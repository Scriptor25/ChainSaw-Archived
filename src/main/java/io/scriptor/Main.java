package io.scriptor;

import io.scriptor.chainsaw.Lexer;
import io.scriptor.chainsaw.Parser;
import io.scriptor.chainsaw.runtime.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class Main {

    public static void main(String[] args) throws IOException {

        if (args.length == 0) { // interpreter shell
            String executionPath = System.getProperty("user.dir");

            var interpreter = new Interpreter(executionPath);
            while (true) {

                System.out.print(">> ");
                String source = System.console().readLine().trim();

                if (source.startsWith("--")) {
                    switch (source) {
                        case "--exit":
                            return;
                        case "--reset":
                            interpreter = new Interpreter(executionPath);
                            break;
                        case "--clear":
                            System.out.print("\033[H\033[2J");
                            System.out.flush();
                            break;
                        case "--path":
                            System.out.println(executionPath);
                            break;
                    }

                    continue;
                }

                var tokens = Lexer.tokenize(source);
                var parser = new Parser(tokens);
                var program = parser.parseProgram();

                var result = interpreter.evaluateProgram(program);
                if (result != null && result.getValue() != null)
                    System.out.println(result);
            }
        }

        if (args.length == 1) { // run file

            String executionPath = new File(args[0]).getParent();

            System.out.println(executionPath);

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

            var interpreter = new Interpreter(executionPath);
            interpreter.evaluateProgram(program);
            var result = interpreter.evaluateFunction(null, "main");
            if (result != null && result.getValue() != null)
                System.out.println(result);

            return;
        }

        System.out.println("Use 'csaw' to run the interpreter shell, or 'csaw <filepath>' to run a file");
    }
}