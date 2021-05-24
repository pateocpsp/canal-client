package com.pateo.qingcloud.canal.utils;

import org.yaml.snakeyaml.Yaml;
import java.util.Map;

public class YamlUtils {


    public static Map<String, Object> parseYaml2Map(String str) {
        Yaml yaml = new Yaml();
        return (Map<String, Object>) yaml.load(cleanYaml(str));
    }

    private static String cleanYaml(String yamlText) {
        String tmpText = yamlText.replaceAll("^---.*\n", "---\n");
        tmpText = tmpText.replaceAll("!ruby.*\n", "\n");
        return tmpText;
    }

    /**
     * ignore
     */
    private static boolean ignore(String yamlText) {
        if (yamlText == null) {
            return false;
        }
        return yamlText.matches("---.*\\{}\n") || yamlText.matches("---.*\\[]\n") || yamlText.matches("-.*\\{}\n") || yamlText.matches("-.*\\[]\n");
    }

}
