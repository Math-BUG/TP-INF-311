package com.grupo10.inf311.docscan;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LanguageToolResponse {

    @SerializedName("software")
    private SoftwareInfo software;

    @SerializedName("warnings")
    private WarningsInfo warnings;

    @SerializedName("language")
    private LanguageInfo language;

    @SerializedName("matches")
    private List<Match> matches;

    // Getters para acessar os dados
    public SoftwareInfo getSoftware() { return software; }
    public WarningsInfo getWarnings() { return warnings; }
    public LanguageInfo getLanguage() { return language; }
    public List<Match> getMatches() { return matches; }

    // Classes aninhadas para representar as diferentes seções do JSON

    public static class SoftwareInfo {
        @SerializedName("name")
        private String name;
        @SerializedName("version")
        private String version;
        @SerializedName("buildDate")
        private String buildDate;
        @SerializedName("apiVersion")
        private int apiVersion;
        @SerializedName("premium")
        private boolean premium;
        @SerializedName("premiumHint")
        private String premiumHint;
        @SerializedName("status")
        private String status;

        // Getters
        public String getName() { return name; }

        public String getVersion() { return version; }
        public String getBuildDate() { return buildDate; }
        public int getApiVersion() { return apiVersion; }
        public boolean isPremium() { return premium; }
        public String getPremiumHint() { return premiumHint; }
        public String getStatus() { return status; }
    }

    public static class WarningsInfo {
        @SerializedName("incompleteResults")
        private boolean incompleteResults;

        // Getter
        public boolean isIncompleteResults() { return incompleteResults; }
    }

    public static class LanguageInfo {
        @SerializedName("name")
        private String name;
        @SerializedName("code")
        private String code;
        @SerializedName("detectedLanguage")
        private DetectedLanguageInfo detectedLanguage;

        // Getters
        public String getName() { return name; }
        public String getCode() { return code; }
        public DetectedLanguageInfo getDetectedLanguage() { return detectedLanguage; }
    }

    public static class DetectedLanguageInfo {
        @SerializedName("name")
        private String name;
        @SerializedName("code")
        private String code;
        @SerializedName("confidence")
        private double confidence;

        // Getters
        public String getName() { return name; }
        public String getCode() { return code; }
        public double getConfidence() { return confidence; }
    }

    public static class Match {
        @SerializedName("message")
        private String message;
        @SerializedName("shortMessage")
        private String shortMessage;
        @SerializedName("offset")
        private int offset;
        @SerializedName("length")
        private int length;
        @SerializedName("replacements")
        private List<Replacement> replacements;
        @SerializedName("context")
        private ContextInfo context;
        @SerializedName("sentence")
        private String sentence;
        @SerializedName("type")
        private TypeInfo type;
        @SerializedName("rule")
        private Rule rule;
        @SerializedName("ignoreForIncompleteSentence")
        private boolean ignoreForIncompleteSentence;
        @SerializedName("contextForSureMatch")
        private int contextForSureMatch;


        // Getters
        public String getMessage() { return message; }
        public String getShortMessage() { return shortMessage; }
        public int getOffset() { return offset; }
        public int getLength() { return length; }
        public List<Replacement> getReplacements() { return replacements; }
        public ContextInfo getContext() { return context; }
        public String getSentence() { return sentence; }
        public TypeInfo getType() { return type; }
        public Rule getRule() { return rule; }
        public boolean isIgnoreForIncompleteSentence() { return ignoreForIncompleteSentence; }
        public int getContextForSureMatch() { return contextForSureMatch; }
    }

    public static class Replacement {
        @SerializedName("value")
        private String value;
        // Getter
        public String getValue() { return value; }
    }

    public static class ContextInfo {
        @SerializedName("text")
        private String text;
        @SerializedName("offset")
        private int offset;
        @SerializedName("length")
        private int length;

        // Getters
        public String getText() { return text; }
        public int getOffset() { return offset; }
        public int getLength() { return length; }
    }

    public static class TypeInfo {
        @SerializedName("typeName")
        private String typeName;

        // Getter
        public String getTypeName() { return typeName; }
    }

    public static class Rule {
        @SerializedName("id")
        private String id;
        @SerializedName("description")
        private String description;
        @SerializedName("issueType")
        private String issueType;
        @SerializedName("category")
        private Category category;
        @SerializedName("isPremium")
        private boolean isPremium;

        // Getters
        public String getId() { return id; }
        public String getDescription() { return description; }
        public String getIssueType() { return issueType; }
        public Category getCategory() { return category; }
        public boolean isPremium() { return isPremium; }
    }

    public static class Category {
        @SerializedName("id")
        private String id;
        @SerializedName("name")
        private String name;

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
    }
}