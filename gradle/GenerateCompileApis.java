import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GenerateCompileApis implements Opcodes {
    private interface Factory {
        AnnotationVisitor create(JsonObject annotation);
    }

    private static String text(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private static int number(JsonObject obj, String key) {
        return obj.get(key).getAsInt();
    }

    private static JsonArray array(JsonObject obj, String key) {
        return obj.has(key) ? obj.getAsJsonArray(key) : new JsonArray();
    }

    private static String[] strings(JsonObject obj, String key) {
        List<String> list = new ArrayList<>();
        for (JsonElement el : array(obj, key)) {
            list.add(el.getAsString());
        }
        return list.toArray(new String[0]);
    }

    private static Object scalar(JsonObject obj) {
        String value = text(obj, "value");
        String type = text(obj, "type");
        if ("Long".equals(type)) return Long.valueOf(value);
        if ("Integer".equals(type)) return Integer.valueOf(value);
        if ("Float".equals(type)) return Float.valueOf(value);
        if ("Double".equals(type)) return Double.valueOf(value);
        if ("Byte".equals(type)) return Byte.valueOf(value);
        if ("Short".equals(type)) return Short.valueOf(value);
        if ("Character".equals(type)) return value.charAt(0);
        throw new IllegalArgumentException(obj.toString());
    }

    private static Object constant(JsonElement el) {
        if (el == null || el.isJsonNull()) return null;
        if (el.isJsonObject()) return scalar(el.getAsJsonObject());
        return el.getAsString();
    }

    private static void annotationValue(AnnotationVisitor visitor, String name, JsonElement value) {
        if (value.isJsonArray()) {
            AnnotationVisitor nested = visitor.visitArray(name);
            for (JsonElement el : value.getAsJsonArray()) {
                annotationValue(nested, null, el);
            }
            nested.visitEnd();
        } else if (value.isJsonObject()) {
            JsonObject obj = value.getAsJsonObject();
            String kind = text(obj, "kind");
            if ("scalar".equals(kind)) {
                visitor.visit(name, scalar(obj));
            } else if ("type".equals(kind)) {
                visitor.visit(name, Type.getType(text(obj, "descriptor")));
            } else if ("enum".equals(kind)) {
                visitor.visitEnum(name, text(obj, "descriptor"), text(obj, "name"));
            } else if ("annotation".equals(kind)) {
                JsonObject ann = obj.getAsJsonObject("annotation");
                writeAnnotation(visitor.visitAnnotation(name, text(ann, "descriptor")), ann);
            } else {
                throw new IllegalArgumentException(obj.toString());
            }
        } else if (value.getAsJsonPrimitive().isBoolean()) {
            visitor.visit(name, value.getAsBoolean());
        } else {
            visitor.visit(name, value.getAsString());
        }
    }

    private static void writeAnnotation(AnnotationVisitor visitor, JsonObject annotation) {
        if (visitor == null) return;
        for (Map.Entry<String, JsonElement> entry : annotation.getAsJsonObject("values").entrySet()) {
            annotationValue(visitor, entry.getKey(), entry.getValue());
        }
        visitor.visitEnd();
    }

    private static void annotations(JsonObject obj, String key, Factory factory) {
        for (JsonElement el : array(obj, key)) {
            JsonObject ann = el.getAsJsonObject();
            writeAnnotation(factory.create(ann), ann);
        }
    }

    private static byte[] compile(JsonObject cls) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(
                number(cls, "version"),
                number(cls, "access"),
                text(cls, "name"),
                text(cls, "signature"),
                text(cls, "superName"),
                strings(cls, "interfaces")
        );
        if (cls.has("outerClass")) {
            cw.visitOuterClass(text(cls, "outerClass"), text(cls, "outerMethod"), text(cls, "outerMethodDescriptor"));
        }
        annotations(cls, "annotations", a -> cw.visitAnnotation(text(a, "descriptor"), a.get("visible").getAsBoolean()));
        annotations(cls, "typeAnnotations", a -> cw.visitTypeAnnotation(
                number(a, "typeRef"), TypePath.fromString(text(a, "typePath")), text(a, "descriptor"), a.get("visible").getAsBoolean()));
        for (JsonElement el : array(cls, "innerClasses")) {
            JsonObject inner = el.getAsJsonObject();
            cw.visitInnerClass(text(inner, "name"), text(inner, "outerName"), text(inner, "innerName"), number(inner, "access"));
        }
        for (JsonElement el : array(cls, "fields")) {
            JsonObject f = el.getAsJsonObject();
            FieldVisitor fv = cw.visitField(
                    number(f, "access"),
                    text(f, "name"),
                    text(f, "descriptor"),
                    text(f, "signature"),
                    constant(f.get("constant"))
            );
            annotations(f, "annotations", a -> fv.visitAnnotation(text(a, "descriptor"), a.get("visible").getAsBoolean()));
            fv.visitEnd();
        }
        for (JsonElement el : array(cls, "methods")) {
            JsonObject m = el.getAsJsonObject();
            int access = number(m, "access");
            String descriptor = text(m, "descriptor");
            MethodVisitor mv = cw.visitMethod(access, text(m, "name"), descriptor, text(m, "signature"), strings(m, "exceptions"));
            for (JsonElement pe : array(m, "parameters")) {
                JsonObject p = pe.getAsJsonObject();
                mv.visitParameter(text(p, "name"), number(p, "access"));
            }
            annotations(m, "annotations", a -> mv.visitAnnotation(text(a, "descriptor"), a.get("visible").getAsBoolean()));
            if (m.has("annotationDefault")) {
                AnnotationVisitor av = mv.visitAnnotationDefault();
                annotationValue(av, null, m.get("annotationDefault"));
                av.visitEnd();
            }
            if ((access & (ACC_ABSTRACT | ACC_NATIVE)) == 0) {
                mv.visitCode();
                mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
                mv.visitInsn(DUP);
                mv.visitLdcInsn("Compile-only API header; use the real server or library at runtime");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitInsn(ATHROW);
                int locals = (access & ACC_STATIC) == 0 ? 1 : 0;
                for (Type t : Type.getArgumentTypes(descriptor)) {
                    locals += t.getSize();
                }
                mv.visitMaxs(3, locals);
            }
            mv.visitEnd();
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static SortedMap<String, JsonObject> declarations(Path input, Path schema, Set<Path> active) throws Exception {
        if (!active.add(schema)) {
            throw new IllegalArgumentException("Cyclic API declaration inheritance: " + schema);
        }
        JsonObject data;
        try (Reader reader = Files.newBufferedReader(schema, StandardCharsets.UTF_8)) {
            data = JsonParser.parseReader(reader).getAsJsonObject();
        }
        SortedMap<String, JsonObject> classes = new TreeMap<>();
        if (data.has("base")) {
            classes.putAll(declarations(input, input.resolve(text(data, "base") + ".json"), active));
        }
        for (JsonElement entry : array(data, "classes")) {
            JsonObject cls = entry.getAsJsonObject();
            classes.put(text(cls, "name"), cls);
        }
        active.remove(schema);
        return classes;
    }

    public static void main(String[] args) throws Exception {
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        Files.createDirectories(output);
        List<Path> schemas;
        try (Stream<Path> stream = Files.list(input)) {
            schemas = stream.filter(p -> p.toString().endsWith(".json")).sorted().collect(Collectors.toList());
        }
        for (Path schema : schemas) {
            SortedMap<String, JsonObject> classes = declarations(input, schema, new HashSet<>());
            String name = schema.getFileName().toString().replace(".json", ".jar");
            Path jar = output.resolve(name);
            try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
                for (JsonObject cls : classes.values()) {
                    JarEntry entry = new JarEntry(text(cls, "name") + ".class");
                    entry.setTime(0);
                    out.putNextEntry(entry);
                    out.write(compile(cls));
                    out.closeEntry();
                }
            }
            System.out.println("Generated compile API " + name + " (" + classes.size() + " declarations)");
        }
    }
}
