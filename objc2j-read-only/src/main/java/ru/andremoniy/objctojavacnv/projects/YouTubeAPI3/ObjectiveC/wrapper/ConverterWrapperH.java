package ru.andremoniy.objctojavacnv.projects.YouTubeAPI3.ObjectiveC.wrapper;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.andremoniy.objctojavacnv.ConverterProperties;
import ru.andremoniy.objctojavacnv.Preprocessor;
import ru.andremoniy.objctojavacnv.Utils;
import ru.andremoniy.objctojavacnv.antlr.output.ObjchLexer;
import ru.andremoniy.objctojavacnv.antlr.output.ObjchParser;
import ru.andremoniy.objctojavacnv.builder.ClassBuilder;
import ru.andremoniy.objctojavacnv.context.ProjectContext;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * User: Andremoniy
 * Date: 18.06.12
 * Time: 9:32
 */
public class ConverterWrapperH {

    public static final Logger log = LoggerFactory.getLogger(ConverterWrapperH.class);

    private static List<String[]> fieldsList = new LinkedList<>();

    private ConverterWrapperH() {
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static StringBuilder convert_h(String fileName, ProjectContext projectContext, StringBuilder originalImportsSb, StringBuilder importsSb) throws IOException, RecognitionException {
        projectContext.h_counter++;

        File phfile = new File(fileName + "p");
        File hfile = phfile.exists() ? phfile : new File(fileName);

        final boolean categoryClass = hfile.getName().contains("+");
        final String categoryName = categoryClass ? hfile.getName().substring(hfile.getName().indexOf("+") + 1, hfile.getName().lastIndexOf(".")) : null;

        // new file with java code
        String pureClassName = hfile.getName().substring(0, hfile.getName().lastIndexOf(".")).replace("-", "_");
        String className = pureClassName;
        File hjfile = new File(hfile.getParent() + File.separator + className + ".java");
        hjfile.createNewFile();

        String packageName = "mab.yt3.wrapper";// step_01

        String input = FileUtils.readFileToString(hfile, ConverterProperties.PROPERTIES.getProperty(ConverterProperties.ENCODING));
        input = input.replace("///", "//");

        input = Preprocessor.replace(input, projectContext, fileName);

        // ANTLR parsing
        CharStream cs = new ANTLRStringStream(input);
        ObjchLexer lexer = new ObjchLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream();
        tokens.setTokenSource(lexer);
        ObjchParser parser = new ObjchParser(tokens);

        ObjchParser.code_return result = parser.code();

        //StringBuilder sb = new StringBuilder();

        if (originalImportsSb == null) originalImportsSb = new StringBuilder();

        Utils.addOriginalImports(input, projectContext);

        ClassBuilder cb = new ClassBuilder();

        if (!categoryClass) {
            cb.setPackage(packageName);
            cb.addImport("java.util.LinkedHashMap");
        }

        if (result.getTree() == null) return cb.sb(); // this is not a error, but file contains only commentaries
        CommonTree tree = (CommonTree) result.getTree();

        // 1. StringBuilder for main class:
        ClassBuilder mainCb = new ClassBuilder();

        // 2. StringBuilder for addinitianl not-inner interfaces
        ClassBuilder addCb = new ClassBuilder();

        // find interface with name, which is equals to file name:
        CommonTree mainInterface = null;
        if (tree.getType() == ObjchLexer.INTERFACE) {
            mainInterface = processInterface(projectContext, pureClassName, className, packageName, cb, addCb, mainInterface, tree);
        }

        projectContext.newClass(className, categoryName);

        if (mainInterface != null) {
            cb.sb().append("\n");
            process_interface(projectContext, mainInterface, mainCb, false, false, cb);
        }

        if (importsSb == null) {
            importsSb = new StringBuilder();
        }
        Utils.addAdditionalImports(projectContext);

        if (!categoryClass) {
            mainCb.a("}\n"); // end of class

            cb.a(importsSb);
        }

        cb.a(mainCb);

        writeStringToJavaFile(categoryClass, hjfile, cb);

        return cb.sb();

    }

    private static void writeStringToJavaFile(boolean categoryClass, File hjfile, ClassBuilder cb) throws IOException {
        if (!categoryClass)
            FileUtils.writeStringToFile(hjfile, cb.sb().toString(), ConverterProperties.PROPERTIES.getProperty(ConverterProperties.ENCODING));
    }

    private static CommonTree processInterface(ProjectContext projectContext, String pureClassName, String className, String packageName, ClassBuilder cb, ClassBuilder addCb, CommonTree mainInterface, CommonTree childTree) {
        CommonTree nameTree = (CommonTree) childTree.getFirstChildWithType(ObjchLexer.INTERFACE_NAME);
        String interfaceName = nameTree.getChild(0).getText();
        if (interfaceName.equals(pureClassName)) {
            mainInterface = childTree;
        } else {
            projectContext.newClass("I" + interfaceName, null);
            projectContext.addImports(interfaceName, packageName + "." + className + "." + projectContext.classCtx.className());
            projectContext.addImports("I" + interfaceName, packageName + "." + className + "." + projectContext.classCtx.className());
            process_interface(projectContext, childTree, addCb, true, true, cb);
        }
        return mainInterface;
    }

    private static void process_interface(ProjectContext projectContext, CommonTree interfaceTree, ClassBuilder cb2, boolean finish, boolean innerClass, ClassBuilder cb) {
        h_process_interface1(cb2, interfaceTree, projectContext, innerClass, cb);

        process_interface_body(cb2.sb(), interfaceTree, projectContext, false);// TODO djzhang used
    }

    // TODO djzhang [class body]
    private static void process_interface_body(StringBuilder sb2, CommonTree tree, ProjectContext projectContext, boolean skipInterface) {
        String currentGroupModifier = "";
        for (Object child : tree.getChildren()) {
            CommonTree childTree = (CommonTree) child;
            switch (childTree.token.getType()) {
                case ObjchParser.FIELD:
                    // todo: must be static!
                    h_process_field(sb2, (CommonTree) child, projectContext, currentGroupModifier, skipInterface);
                    break;
                case ObjchParser.METHOD:
                    h_process_method(sb2, (CommonTree) child, projectContext);
                    break;
            }
        }
    }

    private static void h_process_default_constructor(StringBuilder sb, CommonTree tree, ProjectContext projectCtx) {
        sb.
                append("\n").
                append("\t").
                append("public ").
                append(projectCtx.classCtx.className).append("(");

        sb.append(")");

        sb.append(" {  \n");
        // add method body

        h_process_default_method_body(sb);

        sb.append("    }\n").append("\n");


    }

    private static void h_process_default_method_body(StringBuilder sb) {
        for (String[] row : fieldsList) {
            String isClassString = row[3];
            boolean isClass = Boolean.valueOf(isClassString);
            String typeName = row[0];
            String variableName = row[2];
            if (isClass) {
                sb.append("\t").
                        append("    ").
                        append("this.").
                        append(variableName).
                        append(" = ").
                        append("new ").
                        append(typeName).
                        append("()").
                        append(";").
                        append("\n");
            } else {
                sb.append("\t").
                        append("    ").
                        append("this.").
                        append(variableName).
                        append(" = ").
                        append("\"\"").
                        append(";").
                        append("\n");
            }

        }
    }

    private static void h_process_method(StringBuilder sb, CommonTree tree, ProjectContext projectCtx) {
        String type = "";
        String name = "";
        String modifier;
        Map<String, String> params = new LinkedHashMap<>();
        for (Object child : tree.getChildren()) {
            switch (((CommonTree) child).token.getType()) {
                case ObjchParser.MODIFIER:
                    modifier = ((CommonTree) child).getChild(0).toString();
                    if (modifier.equals("+")) return;
                    break;
                case ObjchParser.TYPE:
                    type = "void";
                    break;
                case ObjchParser.METHOD_NAME:
                    name = ((CommonTree) child).getChild(0).toString();
                    break;
                case ObjchParser.METHOD_PARAMS:
                    h_process_method_params(params, (CommonTree) child, projectCtx);
                    break;
            }
        }

        h_process_default_constructor(sb, tree, projectCtx);
        h_process_for_initFromDictionary(sb, projectCtx, type, name, params);
    }


    private static void h_process_for_initFromDictionary(StringBuilder sb, ProjectContext projectCtx, String type, String name, Map<String, String> params) {
        String transType = Utils.transformType(type, projectCtx.classCtx);
        sb.
                append("\n").
                append("\t").
                append("public ").
                append(transType).append(" ").append(
                projectCtx.classCtx.categoryName != null ? "_" + projectCtx.classCtx.categoryName + "_" : "").
                append(name).append("(");


        sb.append("LinkedHashMap<String, Object> dict");
        sb.append(")");

        sb.append(" {  \n");
        // add method body
        h_process_method_for_initFromDictionary(sb);
        sb.append("    }\n").append("\n");
    }

    private static void h_process_method_for_initFromDictionary(StringBuilder sb) {
        for (String[] row : fieldsList) {
            String isClassString = row[3];
            boolean isClass = Boolean.valueOf(isClassString);
            String typeName = row[0];
            String variableName = row[2];
            if (isClass) {
                sb.append("\t").
                        append("    ").
                        append("this.").
                        append(variableName).
                        append(" = ").
                        append("new ").
                        append(typeName).
                        append("()").
                        append(";").
                        append("\n");
            } else {

                sb.append("\t").
                        append("    ").
                        append("this.").
                        append(variableName).
                        append(" = ").
                        append("getValueFromMap(dict, ").
                        append("\"").
                        append(variableName).
                        append("\"").
                        append(")").
                        append(";").
                        append("\n");
            }
        }
    }

    private static void h_process_method_params(Map<String, String> params, CommonTree tree, ProjectContext projectContext) {
        for (Object child : tree.getChildren()) {
            switch (((CommonTree) child).token.getType()) {
                case ObjchParser.METHOD_PARAM:
                    h_process_method_param(params, (CommonTree) child, projectContext);
                    break;
            }
        }
    }

    private static void h_process_method_param(Map<String, String> params, CommonTree tree, ProjectContext projectCtx) {
        String type = "";
        String name = "";
        for (Object child : tree.getChildren()) {
            switch (((CommonTree) child).token.getType()) {
                case ObjchParser.TYPE:
                    type = ((CommonTree) child).getChild(0).toString();
                    break;
                case ObjchParser.PARAM_NAME:
                    name = ((CommonTree) child).getChild(0).toString();
                    break;
            }
        }
        params.put(name, Utils.transformType(type, projectCtx.classCtx));
    }

    private static void h_process_interface1(ClassBuilder cb1, CommonTree tree, ProjectContext context, boolean innerClass, ClassBuilder cb2) {
        String interfaceName = "";
        String superclassName = "MABYT3_Object";
        String category = "";
        for (Object child : tree.getChildren()) {
            switch (((CommonTree) child).token.getType()) {
                case ObjchParser.INTERFACE_NAME:
                    interfaceName = ((CommonTree) child).getChild(0).toString();
                    break;
            }
        }

        if (superclassName.length() > 0 && !superclassName.startsWith("NS")) {
            // add import of that class:
            Set<String> superClassPathList = context.imports.get(superclassName);
            if (superClassPathList != null) {
                for (String superClassPath : superClassPathList) {
                    if (superClassPath != null) cb2.addImport(superClassPath);
                }
            }
        }

        cb1.abstractClass(innerClass, true, interfaceName, superclassName);

    }

    private static void h_process_field(StringBuilder sb, CommonTree tree, ProjectContext projectCtx, String currentGroupModifier, boolean isStatic) {// TODO djzhang class field
        String type = "";
        List<String> fieldNameList = new ArrayList<>();
        for (Object child : tree.getChildren()) {
            switch (((CommonTree) child).token.getType()) {
                case ObjchParser.TYPE:
                    type = ((CommonTree) child).getChild(0).toString();
                    break;
                case ObjchParser.FIELD_NAME:
                    fieldNameList.add(((CommonTree) child).getChild(0).toString());
                    break;
            }
        }

        String transformedType = Utils.transformType(type, projectCtx.classCtx);

        String[] fieldRow = new String[]{type, transformedType, fieldNameList.get(0), String.valueOf(transformedType.equals(type))};
        fieldsList.add(fieldRow);

        sb.
                append("\t").
                append(transformedType).
                append(" ");
        boolean f = true;
        for (String fieldName : fieldNameList) {
            if (!f) {
                sb.append(", ");
            }
            f = false;
            sb.append(fieldName);
        }
        sb.append(";\n");
    }


}
