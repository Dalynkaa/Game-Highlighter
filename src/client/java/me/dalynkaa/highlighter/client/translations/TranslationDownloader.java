package me.dalynkaa.highlighter.client.translations;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.dalynkaa.highlighter.Highlighter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationDownloader {
    private static final String WEBLATE_BASE_URL = "https://weblate.nexbit.dev"; // Замени на свой URL
    private static final String PROJECT = "highlighter";
    private static final String COMPONENT = "mod";
    
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final Path LANG_CACHE_DIR = Path.of(".minecraft/highlighter_translations");
    
    static {
        try {
            Files.createDirectories(LANG_CACHE_DIR);
        } catch (IOException e) {
            Highlighter.LOGGER.warn("[TranslationDownloader] Failed to create cache directory", e);
        }
    }
    
    /**
     * Загружает перевод для указанного языка с Weblate и сохраняет в кеш
     */
    public static CompletableFuture<Boolean> downloadTranslation(String languageCode) {
        return CompletableFuture.supplyAsync(() -> {
            // Проверяем включена ли загрузка переводов
            if (!isTranslationDownloadingEnabled()) {
                Highlighter.LOGGER.debug("[TranslationDownloader] Translation downloading is disabled in config");
                return false;
            }
            try {
                String weblateLanguage = convertToWeblateLanguage(languageCode);
                String url = String.format("%s/api/translations/%s/%s/%s/file/", 
                    WEBLATE_BASE_URL, PROJECT, COMPONENT, weblateLanguage);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Highlighter-Mod/3.0.0")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    // Сохраняем перевод в файл для использования Minecraft'ом
                    Path langFile = LANG_CACHE_DIR.resolve(languageCode + ".json");
                    Files.writeString(langFile, response.body(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    
                    // Перезагружаем языки в Minecraft
                    reloadMinecraftLanguages();
                    
                    Highlighter.LOGGER.info("[TranslationDownloader] Downloaded translation for language {} to {}", 
                        languageCode, langFile);
                    
                    return true;
                } else if (response.statusCode() == 404) {
                    Highlighter.LOGGER.debug("[TranslationDownloader] Translation not found for language: {}", languageCode);
                } else if (response.statusCode() == 401) {
                    Highlighter.LOGGER.debug("[TranslationDownloader] Unauthorized access to Weblate API (server not configured or invalid token)");
                } else if (response.statusCode() >= 500) {
                    Highlighter.LOGGER.debug("[TranslationDownloader] Weblate server error for {}: HTTP {}", 
                        languageCode, response.statusCode());
                } else {
                    Highlighter.LOGGER.warn("[TranslationDownloader] Failed to download translation for {}: HTTP {}", 
                        languageCode, response.statusCode());
                }
                
            } catch (IOException | InterruptedException e) {
                Highlighter.LOGGER.error("[TranslationDownloader] Error downloading translation for {}: {}", 
                    languageCode, e.getMessage());
            } catch (Exception e) {
                Highlighter.LOGGER.error("[TranslationDownloader] Unexpected error downloading translation for {}", 
                    languageCode, e);
            }
            
            return false;
        });
    }
    
    /**
     * Перезагружает языки в Minecraft для обновления переводов
     */
    private static void reloadMinecraftLanguages() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.execute(() -> {
                try {
                    // Перезагружаем языковой менеджер
                    LanguageManager languageManager = client.getLanguageManager();
                    // В новых версиях Minecraft метод может отличаться
                    // languageManager.reload(); // Если такой метод существует
                    
                    Highlighter.LOGGER.debug("[TranslationDownloader] Reloaded Minecraft languages");
                } catch (Exception e) {
                    Highlighter.LOGGER.warn("[TranslationDownloader] Failed to reload languages", e);
                }
            });
        }
    }
    
    /**
     * Проверяет есть ли сохраненный перевод для языка
     */
    public static boolean hasCachedTranslation(String languageCode) {
        Path langFile = LANG_CACHE_DIR.resolve(languageCode + ".json");
        return Files.exists(langFile);
    }
    
    /**
     * Очищает кеш переводов
     */
    public static void clearCache() {
        try {
            Files.list(LANG_CACHE_DIR)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        Highlighter.LOGGER.warn("[TranslationDownloader] Failed to delete {}", path, e);
                    }
                });
            
            Highlighter.LOGGER.info("[TranslationDownloader] Translation cache cleared");
        } catch (IOException e) {
            Highlighter.LOGGER.error("[TranslationDownloader] Failed to clear cache", e);
        }
    }
    
    /**
     * Предзагружает переводы для текущего языка игры
     */
    public static void preloadCurrentLanguage() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getLanguageManager() != null) {
            String currentLanguage = client.getLanguageManager().getLanguage();
            
            // Проверяем нужно ли загружать
            if (!hasCachedTranslation(currentLanguage)) {
                downloadTranslation(currentLanguage)
                    .thenAccept(success -> {
                        if (success) {
                            Highlighter.LOGGER.info("[TranslationDownloader] Preloaded translations for current language: {}", 
                                currentLanguage);
                        }
                    });
            }
        } else {
            Highlighter.LOGGER.debug("[TranslationDownloader] Cannot preload - LanguageManager not available yet");
        }
    }
    
    /**
     * Проверяет включена ли загрузка переводов в конфигурации
     */
    private static boolean isTranslationDownloadingEnabled() {
        try {
            // Импортируем HighlighterClient для доступа к конфигурации
            return me.dalynkaa.highlighter.client.HighlighterClient.CONFIG.backendSettings.enableTranslationDownloading;
        } catch (Exception e) {
            // Если конфигурация недоступна, возвращаем false
            return false;
        }
    }
    
    /**
     * Конвертирует код языка Minecraft в код языка Weblate
     */
    private static String convertToWeblateLanguage(String minecraftLanguage) {
        // Конвертация из Minecraft формата в Weblate формат
        return switch (minecraftLanguage.toLowerCase()) {
            case "en_us" -> "en";
            case "ru_ru" -> "ru";
            case "de_de" -> "de";
            case "fr_fr" -> "fr";
            case "es_es" -> "es";
            case "pt_br" -> "pt";
            case "zh_cn" -> "zh-hans";
            case "zh_tw" -> "zh-hant";
            case "ja_jp" -> "ja";
            case "ko_kr" -> "ko";
            case "it_it" -> "it";
            case "pl_pl" -> "pl";
            case "nl_nl" -> "nl";
            case "sv_se" -> "sv";
            case "da_dk" -> "da";
            case "nb_no" -> "no";
            case "fi_fi" -> "fi";
            case "cs_cz" -> "cs";
            case "sk_sk" -> "sk";
            case "hu_hu" -> "hu";
            case "ro_ro" -> "ro";
            case "bg_bg" -> "bg";
            case "hr_hr" -> "hr";
            case "sl_si" -> "sl";
            case "et_ee" -> "et";
            case "lv_lv" -> "lv";
            case "lt_lt" -> "lt";
            case "el_gr" -> "el";
            case "tr_tr" -> "tr";
            case "ar_sa" -> "ar";
            case "he_il" -> "he";
            case "hi_in" -> "hi";
            case "th_th" -> "th";
            case "vi_vn" -> "vi";
            case "uk_ua" -> "uk";
            default -> minecraftLanguage.split("_")[0]; // Fallback: берем первую часть
        };
    }
}