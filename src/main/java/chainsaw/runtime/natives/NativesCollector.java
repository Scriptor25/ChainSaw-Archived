package chainsaw.runtime.natives;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;

import chainsaw.runtime.Environment;
import chainsaw.runtime.function.Function;
import chainsaw.runtime.function.NativeImplementation;
import chainsaw.runtime.function.Pair;
import chainsaw.runtime.type.NativeType;
import chainsaw.runtime.type.Type;
import io.github.classgraph.ClassGraph;

public class NativesCollector {

    private NativesCollector() {
    }

    public static void collect(Environment env) {
        try (final var result = new ClassGraph().enableClassInfo().enableAnnotationInfo().scan()) {
            for (final var info : result.getClassesWithAnnotation(CSawNative.class)) {
                final var cls = info.loadClass();

                var annotation = cls.getAnnotation(CSawNative.class);
                if (annotation == null)
                    continue;

                var methods = cls.getMethods();
                var constructors = cls.getConstructors();

                var alias = annotation.alias().trim();
                var nativeType = NativeType.create(env, cls, alias.isEmpty() ? cls.getSimpleName() : alias);

                for (var mthd : methods) {
                    var isOverride = mthd.isAnnotationPresent(CSawOverride.class);

                    List<Pair<String, Type>> parameters = new Vector<>();
                    for (int i = 0; i < mthd.getParameterCount(); i++) {
                        var param = mthd.getParameters()[i];
                        parameters.add(new Pair<>(param.getName(), Type.parseType(env, param.getType())));
                    }

                    if (isOverride)
                        parameters.remove(0);
                    if (mthd.isVarArgs())
                        parameters.remove(parameters.size() - 1);

                    Function.get(env,
                            false,
                            mthd.getName(),
                            Type.parseType(env, mthd.getReturnType()),
                            parameters,
                            mthd.isVarArgs(),
                            isOverride
                                    ? Type.parseType(env, alias)
                                    : Modifier.isStatic(mthd.getModifiers()) ? null : nativeType,
                            new NativeImplementation(mthd));
                }

                for (var cons : constructors) {
                    List<Pair<String, Type>> parameters = new Vector<>();
                    for (var param : cons.getParameters())
                        parameters.add(
                                new Pair<String, Type>(param.getName(), Type.parseType(env, param.getType())));

                    if (cons.isVarArgs())
                        parameters.remove(parameters.size() - 1);

                    Function.get(env,
                            true,
                            alias,
                            Type.parseType(env, cls),
                            parameters,
                            cons.isVarArgs(),
                            null,
                            new NativeImplementation(cons));
                }
            }
        }
    }

}
