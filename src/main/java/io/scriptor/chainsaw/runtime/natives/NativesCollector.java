package io.scriptor.chainsaw.runtime.natives;

import java.util.List;
import java.util.Vector;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.scriptor.chainsaw.runtime.Environment;
import io.scriptor.chainsaw.runtime.function.FuncParam;
import io.scriptor.chainsaw.runtime.type.NativeType;
import io.scriptor.chainsaw.runtime.type.Type;

public class NativesCollector {

    public static void registerNatives(Environment env) {
        ScanResult result = new ClassGraph().enableMethodInfo().enableClassInfo().enableAnnotationInfo().scan();

        var typeInfos = result.getClassesWithAnnotation(CSawType.class);
        for (var info : typeInfos) {
            var type = info.loadClass();

            var annotation = type.getAnnotation(CSawType.class);
            NativeType.create(env, type, annotation.alias());
        }

        var functionInfos = result.getClassesWithMethodAnnotation(CSawFunction.class);
        for (var tinfo : functionInfos) {
            for (var info : tinfo.getMethodInfo()) {
                var func = info.loadClassAndGetMethod();
                if (!func.isAnnotationPresent(CSawFunction.class))
                    continue;

                var annotation = func.getAnnotation(CSawFunction.class);

                List<FuncParam> params = new Vector<>();
                for (var p : annotation.params()) {
                    String[] parts = p.split(": +");
                    params.add(new FuncParam(parts[0], Type.parseType(env, parts[1])));
                }

                env.registerNativeFunction(
                        annotation.id(),
                        Type.parseType(env, annotation.result()),
                        params,
                        annotation.vararg(),
                        annotation.constructor(),
                        Type.parseType(env, annotation.memberOf()),
                        func);
            }
        }

        result.close();
    }
}
