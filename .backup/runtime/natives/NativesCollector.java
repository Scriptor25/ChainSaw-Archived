package io.scriptor.chainsaw.runtime.natives;

import java.lang.reflect.Modifier;
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

                List<FuncParam> params = new Vector<>();
                for (var p : func.getParameters()) {
                    if (p.isVarArgs()) {
                        continue;
                    }

                    String id = p.getName();
                    Class<?> type = p.getType();

                    params.add(new FuncParam(id, Type.parseType(env, type)));
                }

                env.registerNativeFunction(
                        func.getName(),
                        Type.parseType(env, func.getReturnType()),
                        params,
                        func.isVarArgs(),
                        (func.getModifiers() & Modifier.STATIC) == 0
                                ? Type.parseType(env, func.getDeclaringClass())
                                : null,
                        func);
            }
        }

        result.close();
    }
}
