package io.scriptor.chainsaw;

import io.scriptor.chainsaw.runtime.Interpreter;

import java.io.File;
import java.io.IOException;

class CSaw {

    public static void main(String[] args) throws IOException {

        if (args.length == 0) { // interpreter shell
            final var executionPath = System.getProperty("user.dir");

            var interpreter = new Interpreter(executionPath);
            while (true) {

                System.out.print(">> ");
                final var source = System.console().readLine().trim();

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
                        case "--pause":
                            System.out.println("Resuming");
                            break;
                    }

                    continue;
                }

                final var tokens = Lexer.tokenize(source);
                final var parser = new Parser(tokens);
                final var program = parser.parseProgram();

                final var result = interpreter.evaluateProgram(program);
                if (result != null && result.getValue() != null)
                    System.out.println(result);
            }
        }

        if (args.length == 1) { // run file

            final var executionPath = new File(args[0]).getParent();
            final var source = FileUtil.readFile(args[0]);

            final var tokens = Lexer.tokenize(source);
            final var parser = new Parser(tokens);
            final var program = parser.parseProgram();

            final var interpreter = new Interpreter(executionPath);
            interpreter.evaluateProgram(program);

            final var result = interpreter.evaluateFunction(null, "main");
            if (result != null && result.getValue() != null)
                System.out.println(result);

            return;
        }

        System.out
                .println("Use with no args to run the interpreter shell, or add a (.csaw) file path to run this file");
    }
}