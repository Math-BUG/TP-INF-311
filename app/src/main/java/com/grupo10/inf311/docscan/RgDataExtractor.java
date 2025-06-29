package com.grupo10.inf311.docscan;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RgDataExtractor {
    private static final String TAG = "RgDataExtractor";

    public static Map<String, String> extractRgData(String ocrText) {
        Map<String, String> data = new HashMap<>();

        // Log original text for debugging
        Log.d("RgDataExtractor", "Original OCR text:\n" + ocrText);

        // Normalize text - convert to uppercase for consistent matching
        String normalizedText = ocrText.toUpperCase();

        // Extract RG number using regex pattern matching
        extractRg(normalizedText, data);

        // Extract CPF using standard pattern (999.999.999-99)
        extractCpf(normalizedText, data);

        // Extract name by excluding known fields and phrases
        extractName(normalizedText, data);


        // Log extracted data
        Log.d("RgDataExtractor", "Extracted data: " + data);

        return data;
    }

    private static void extractRg(String text, Map<String, String> data) {
        // Padrão principal para RG no formato MG-xx.xxx.xxx
        String rgPattern = "\\b[A-Z]{2}-\\d{2}\\.\\d{3}\\.\\d{3}\\b";

        // Padrões alternativos
        String rgPatternAlt = "\\b\\d{2}\\.\\d{3}\\.\\d{3}\\b"; // sem o estado
        String rgPatternAlt2 = "\\b\\d{8,9}\\b"; // apenas números

        String[] lines = text.split("\n");

        // Procura primeiro pelo padrão principal em qualquer linha
        for (String line : lines) {
            if (extractPatternToData(line, rgPattern, "rg_numero", data)) {
                Log.d("RgDataExtractor", "RG found with main pattern: " + data.get("rg_numero"));
                return;
            }
        }

        // Procura por linhas que contenham indicadores de RG
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("REGISTRO") || line.contains("RG") ||
                    line.contains("IDENTIDADE") || line.contains("REGISTRO GERAL")) {

                // Verifica esta linha e as próximas
                for (int j = i; j < Math.min(i + 3, lines.length); j++) {
                    if (extractPatternToData(lines[j], rgPattern, "rg_numero", data)) {
                        return;
                    }
                    if (extractPatternToData(lines[j], rgPatternAlt, "rg_numero", data)) {
                        return;
                    }
                    if (extractPatternToData(lines[j], rgPatternAlt2, "rg_numero", data)) {
                        return;
                    }
                }
            }
        }

        // Fallback: busca direta por padrões alternativos
        extractPatternToData(text, rgPatternAlt, "rg_numero", data);
        if (!data.containsKey("rg_numero")) {
            extractPatternToData(text, rgPatternAlt2, "rg_numero", data);
        }
    }

    private static void extractCpf(String text, Map<String, String> data) {
        // Standard CPF pattern (999.999.999-99)
        String cpfPattern = "\\b\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}\\b";

        // Alternative pattern without separators
        String cpfPatternAlt = "\\b\\d{11}\\b";

        // Try to find CPF after label
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("CPF")) {
                // Check this line and next lines
                for (int j = i; j < Math.min(i + 3, lines.length); j++) {
                    if (extractPatternToData(lines[j], cpfPattern, "cpf", data)) {
                        return;
                    }
                    if (extractPatternToData(lines[j], cpfPatternAlt, "cpf", data)) {
                        return;
                    }
                }
            }
        }

        // Direct pattern matching as fallback
        extractPatternToData(text, cpfPattern, "cpf", data);
        if (!data.containsKey("cpf")) {
            extractPatternToData(text, cpfPatternAlt, "cpf", data);
        }
    }

    private static void extractName(String text, Map<String, String> data) {
        Log.d("RgDataExtractor", "Extracting name from: " + text);

        String[] lines = text.split("\n");

        // Primeiro, vamos procurar pela linha com o número do RG
        // Padrão para RG no formato MG-xx.xxx.xxx
        String rgNumberPattern = "\\b[A-Z]{2}-\\d{2}\\.\\d{3}\\.\\d{3}\\b";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Verifica se a linha contém o padrão do número do RG
            if (line.matches(".*" + rgNumberPattern + ".*")) {
                Log.d("RgDataExtractor", "Found RG number line: " + line);

                // Extrai o número do RG
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(rgNumberPattern);
                java.util.regex.Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    data.put("rg_numero", matcher.group(0));
                    Log.d("RgDataExtractor", "RG number extracted: " + matcher.group(0));
                }

                // O nome deve estar na próxima linha
                if (i + 1 < lines.length) {
                    String nextLine = lines[i + 1].trim();

                    // Verifica se a próxima linha não contém outros campos conhecidos
                    if (!nextLine.isEmpty() &&
                            !nextLine.matches(".*\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}.*") && // Não é CPF
                            !nextLine.matches(".*\\d{2}/\\d{2}/\\d{4}.*") && // Não é data
                            !nextLine.contains("NASCIMENTO") &&
                            !nextLine.contains("CPF") &&
                            !nextLine.contains("DATA") &&
                            !isNumeric(nextLine)) {

                        data.put("NOME", nextLine);
                        Log.d("RgDataExtractor", "Name extracted from line after RG: " + nextLine);
                        return;
                    }
                }
            }
        }

        // Se não encontrou pelo método acima, tenta os padrões existentes
        String[] namePatterns = {
                "NOME[:\\s]+([A-ZÀ-ÖØ-Ý\\s]+)",
                "Nome[:\\s]+([A-ZÀ-ÖØ-Ý\\s]+)",
                "NOME DO PORTADOR[:\\s]+([A-ZÀ-ÖØ-Ý\\s]+)"
        };

        for (String pattern : namePatterns) {
            if (extractPatternToData(text, pattern, "NOME", data)) {
                String name = data.get("NOME");
                name = name.replaceAll("\\s+", " ").trim();
                data.put("NOME", name);
                Log.d("RgDataExtractor", "Name extracted via pattern: " + name);
                return;
            }
        }

        // Fallback: procura por uma linha que pareça um nome
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 5 && line.equals(line.toUpperCase()) &&
                    !line.contains("RG") && !line.contains("CPF") &&
                    !line.contains("NASC") && !line.contains("DATA") &&
                    !line.matches(".*\\d.*") && // Não contém números
                    !isNumeric(line)) {
                data.put("NOME", line);
                Log.d("RgDataExtractor", "Name extracted via fallback: " + line);
                return;
            }
        }

        data.put("NOME", "Nome não encontrado");
        Log.d("RgDataExtractor", "Name not found in text");
    }

    // Helper method to check if a string is numeric
    private static boolean isNumeric(String str) {
        return str.replaceAll("[0-9,.\\s-]", "").isEmpty();
    }


    private static boolean extractPatternToData(String text, String regex, String key, Map<String, String> data) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            data.put(key, matcher.group(0));
            return true;
        }
        return false;
    }
}