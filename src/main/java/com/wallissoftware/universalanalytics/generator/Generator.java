package com.wallissoftware.universalanalytics.generator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Generator {

    public class Category {
        String id;

        String title;

        String description;

        List<Field> fields = new ArrayList<>();

        @Override
        public String toString() {
            return "Category [id=" + id + ", title=" + title + ", description=" + description + ", fields=" + fields
                    + "]\n";
        }

    }

    public class Field {
        String id;

        String name;

        String description;

        String fieldName;

        String valueType;

        String defaultValue;

        String exampleValue;

        String protocolParameter;

        String maxLength;

        String supportedHitTypes;

        @Override
        public String toString() {
            return "Field [id=" + id + ", name=" + name + ", description=" + description + ", fieldName=" + fieldName
                    + ", valueType=" + valueType + ", defaultValue=" + defaultValue + ", exampleValue=" + exampleValue
                    + ", protocolParameter=" + protocolParameter + ", maxLength=" + maxLength + ", supportedHitTypes="
                    + supportedHitTypes + "]\n";
        }


    }

    public static void main(final String[] args) throws MalformedURLException, IOException {

        new Generator();




    }

    static String toCamelCase(final String s){
        final String[] parts = s.split(" ");
        String camelCaseString = "";
        for (final String part : parts){
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return camelCaseString;
    }

    static String toProperCase(final String s) {
        if (s.isEmpty()) {
            return "";
        }
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }

    public static String toTitleCase(final String input) {
        final StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }

    private List<Category> categories = new ArrayList<>();

    private String rawHtml;

    int cursor = 0;

    public Generator() throws MalformedURLException, IOException {
        this.rawHtml = IOUtils.toString(new URL(
                "https://developers.google.com/analytics/devguides/collection/analyticsjs/field-reference"));

        final int startPos = rawHtml.indexOf("<h2 id=\"create\">");
        final int endPos = rawHtml.indexOf("<div id=\"gc-content-footer\">");
        rawHtml = rawHtml.substring(startPos, endPos - 1);

        Category nextCategory = getNextCategory();
        while (nextCategory != null) {
            categories.add(nextCategory);
            nextCategory = getNextCategory();
        }

        /* writeFile("com/wallissoftware/universalanalytics/shared/options/AnalyticsOptions.java",
                 generateBaseInterface(categories));

         for (final Category cat : categories) {
             writeFile(
                     "com/wallissoftware/universalanalytics/shared/options/"
                             + toTitleCase(cat.id).replace("Enhanced-ecomm", "EnhancedEcommerce") + "Options.java",
                             generateInterface(cat));
         }*/

        writeFile("com/wallissoftware/universalanalytics/shared/options/AnalyticsOptions.java",
                generateBaseClass(categories));

        for (final Category cat : categories) {
            writeFile(
                    "com/wallissoftware/universalanalytics/shared/options/"
                            + toTitleCase(cat.id).replace("Enhanced-ecomm", "EnhancedEcommerce") + "Options.java",
                            generateClass(cat));
        }

    }

    private String generateBaseClass(final List<Category> categories) throws IOException {
        final String template = IOUtils.toString(getClass().getClassLoader().getResource("baseclazz.txt"));
        final StringBuilder inner = new StringBuilder();
        //final StringBuilder imports = new StringBuilder();
        for (final Category cat : categories) {
            //imports.append("import com.wallissoftware.universalanalytics.shared.options.").append(toTitleCase(cat.id)).append("Options;\n");
            inner.append("public ").append(toTitleCase(cat.id)).append("Options ");
            inner.append(cat.id).append("Options");
            inner.append("() {\n");
            inner.append("return new ").append(toTitleCase(cat.id)).append("Options(getOptionsCallback());\n");
            inner.append("}\n\n");
        }
        return template.replace("$clazzInner", inner.toString()).replace("nhanced-ecomm", "nhancedEcommerce");

    }

    private String generateBaseInterface(final List<Category> categories) throws IOException {
        final String template = IOUtils.toString(getClass().getClassLoader().getResource("baseinterface.txt"));
        final StringBuilder inner = new StringBuilder();
        for (final Category cat : categories) {
            if (!cat.description.isEmpty()) {
                inner.append("/**\n");
                inner.append("* ");
                inner.append(cat.description).append("\n");
                inner.append("**/\n");
            }
            inner.append(toTitleCase(cat.id)).append("Options ");
            inner.append(cat.id).append("Options");
            inner.append("();\n\n");

        }
        return template.replace("$interfaceInner", inner.toString()).replace("nhanced-ecomm", "nhancedEcommerce");

    }

    private String generateClass(final Category category) throws IOException {
        final String template = IOUtils.toString(getClass().getClassLoader().getResource("clazz.txt"));
        final StringBuilder javaDoc = new StringBuilder();
        if (!category.description.isEmpty()) {
            javaDoc.append("/**\n");
            javaDoc.append("* ");
            javaDoc.append(category.description).append("\n");
            javaDoc.append("**/\n");
        }
        final String iface = toTitleCase(category.id).replace("Enhanced-ecomm", "EnhancedEcommerce") + "Options";
        final String name = toCamelCase(category.id).replace("Enhanced-ecomm", "EnhancedEcommerce")
                + "Options";

        final StringBuilder inner = new StringBuilder();
        for (final Field field: category.fields) {
            if (field.valueType.equals("function")) {
                continue;
            }
            inner.append("/**\n");

            for (String dLine : field.description.split("\n")) {
                dLine = dLine.trim();
                if (!dLine.isEmpty()) {
                    inner.append("* ").append(dLine).append("\n");
                }
            }
            inner.append("* Default Value: ").append(field.defaultValue).append("<br>\n");
            inner.append("* Example Value: ").append(field.exampleValue).append("\n");

            inner.append("**/\n");
            inner.append("public ").append(name).append(" ");
            inner.append(toCamelCase(field.name)).append("(");
            switch (field.valueType) {
            case "currency":
            case "text":
                inner.append("String ");
                break;
            case "float":
                inner.append("float ");
                break;
            case "integer":
                inner.append("int ");
                break;
            case "boolean":
                inner.append("boolean ");
                break;
            }
            inner.append(toCamelCase(field.name)).append(") {\n");
            switch (field.valueType) {
            case "currency":
            case "text":
                inner.append("putText(");
                break;
            case "integer":
            case "float":
                inner.append("putNumber(");
                break;
            case "boolean":
                inner.append("putBoolean(");
                break;
            }
            inner.append("\"").append(field.fieldName).append("\", ").append(toCamelCase(field.name)).append(");\n");
            inner.append("return this;\n");
            inner.append("}\n\n");

        }
        return template.replace("$javaDoc", javaDoc.toString()).replace("$name", name)
                .replace("$clazzInner", inner.toString()).replace("$interface", iface);
    }

    private String generateInterface(final Category category) throws IOException {
        final String template = IOUtils.toString(getClass().getClassLoader().getResource("interface.txt"));
        final StringBuilder javaDoc = new StringBuilder();
        if (!category.description.isEmpty()) {
            javaDoc.append("/**\n");
            javaDoc.append("* ");
            javaDoc.append(category.description).append("\n");
            javaDoc.append("**/\n");
        }
        final String name = toCamelCase(category.id).replace("Enhanced-ecomm", "EnhancedEcommerce") + "Options";

        final StringBuilder inner = new StringBuilder();
        for (final Field field: category.fields) {
            if (field.valueType.equals("function")) {
                continue;
            }
            inner.append("/**\n");

            for (String dLine : field.description.split("\n")) {
                dLine = dLine.trim();
                if (!dLine.isEmpty()) {
                    inner.append("* ").append(dLine).append("\n");
                }
            }
            inner.append("* Default Value: ").append(field.defaultValue).append("<br>\n");
            inner.append("* Example Value: ").append(field.exampleValue).append("\n");

            inner.append("**/\n");
            inner.append(name).append(" ");
            inner.append(toCamelCase(field.name)).append("(");
            switch (field.valueType) {
            case "currency":
            case "text":
                inner.append("String ");
                break;
            case "float":
                inner.append("float ");
                break;
            case "integer":
                inner.append("int ");
                break;
            case "boolean":
                inner.append("boolean ");
                break;
            }
            inner.append(toCamelCase(field.name)).append(");\n\n");


        }
        return template.replace("$javaDoc", javaDoc.toString()).replace("$name", name)
                .replace("$interfaceInner", inner.toString());

    }

    private Category getNextCategory() {
        final Category category = new Category();
        jumpTo("<h2 id=\"");
        if (cursor < 0) {
            return null;
        }
        category.id = getTo("\"");
        jumpTo(">");
        category.title = getTo("</h2");
        jumpTo(">");
        category.description = getTo("<h3").trim();
        Field nextField = getNextField();
        while (nextField != null) {
            category.fields.add(nextField);
            nextField = getNextField();
        }
        return category;
    }

    private Field getNextField() {
        final Field field = new Field();
        final int nextCatePos = rawHtml.indexOf("<h2 id=\"", cursor);
        final int nextFieldPos = rawHtml.indexOf("<h3 id=\"", cursor);
        if (nextFieldPos == -1 || (nextCatePos != -1 && nextFieldPos > nextCatePos)) {
            return null;
        }
        jumpTo("<h3 id=\"");
        field.id = getTo("\"");
        jumpTo(">");
        field.name = stripTags(getTo("</h3")).replace("?", "").replace("-", " ");
        jumpTo("<p>");
        field.description = getTo("<table");

        jumpTo("<tr>");
        final String[] headers = getTo("</tr>").split("(<th>|</th>)");
        final List<String> tableHeaders = new ArrayList<>();

        for (final String h : headers) {
            if (!h.trim().isEmpty()) {
                tableHeaders.add(h.trim());
            }
        }

        jumpTo("<tr>");
        final String[] values = getTo("</tr>").split("(<td>|</td>)");
        final List<String> tableValues = new ArrayList<>();
        for (final String v : values) {
            if (!v.trim().isEmpty()) {
                tableValues.add(stripTags(v.trim()));
            }
        }

        for (int i = 0; i < tableHeaders.size(); i++) {
            switch (tableHeaders.get(i)) {
            case "Field Name":
                field.fieldName = tableValues.get(i);
                break;
            case "Value Type":
                field.valueType = tableValues.get(i);
                break;
            case "Default Value":
                field.defaultValue = tableValues.get(i);
                break;
            case "Protocol Parameter":
                field.protocolParameter = tableValues.get(i);
                break;
            case "Max Length":
                field.maxLength = tableValues.get(i);
                break;
            case "Supported Hit Types":
                field.supportedHitTypes = tableValues.get(i);
                break;

            }
        }
        jumpTo("Example value:");
        field.exampleValue = getTo("<br>");
        return field;
    }

    private String getTo(final String search) {
        final int endPos = rawHtml.indexOf(search, cursor);
        final String result = rawHtml.substring(cursor, endPos);
        cursor = endPos;
        return result.trim();
    }

    private void jumpTo(final String search) {
        cursor = rawHtml.indexOf(search, cursor);
        if (cursor > -1) {
            cursor += search.length();
        }

    }

    private String stripTags(final String input) {
        return input.replaceAll("<[^>]*>", "");
    }

    private void writeFile(final String fileName, final String text) throws IOException {
        FileUtils.writeStringToFile(new File("z://wave/gwt-universal-analytics/src/main/java/" + fileName), text);

    }


}
