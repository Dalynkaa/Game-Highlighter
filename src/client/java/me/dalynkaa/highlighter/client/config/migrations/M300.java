package me.dalynkaa.highlighter.client.config.migrations;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.config.ServerEntry;
import me.dalynkaa.highlighter.client.config.StorageManager;
import me.dalynkaa.highlighter.util.LogUtil;

/**
 * Migration for version 3.0.0
 * Adds new fields to ServerEntry:
 * - useServerSettings (default: true) - keeps existing behavior
 * - serverConfigDialogShown (default: false) - shows dialog to let users choose new server config feature
 */
public class M300 implements Migration {
    private final LogUtil logger = Highlighter.LOGGER.child("Migration").child("M300");

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public void apply(StorageManager storageManager) throws Exception {
        logger.info("Performing migration to version {}", getVersion());
        logger.debug("Adding new server configuration fields to existing server entries");

        try {
            int updatedEntries = 0;
            
            for (ServerEntry serverEntry : storageManager.getServerStorage().getAllServers()) {
                boolean needsUpdate = false;
                
                // Проверяем и добавляем useServerSettings если его нет
                // Gson автоматически установит default значения, но мы можем проверить
                if (!hasUseServerSettingsField(serverEntry)) {
                    // При загрузке из JSON без этого поля Gson установит false,
                    // но мы хотим true по умолчанию для существующих серверов
                    serverEntry.setUseServerSettings(true);
                    needsUpdate = true;
                    logger.debug("Set useServerSettings=true for server: {}", serverEntry.getServerName());
                }
                
                // Проверяем и добавляем serverConfigDialogShown если его нет  
                if (!hasServerConfigDialogShownField(serverEntry)) {
                    // Для существующих серверов устанавливаем false, чтобы показать диалог
                    // и дать пользователю выбор использовать ли новую функцию серверной конфигурации
                    serverEntry.setServerConfigDialogShown(false);
                    needsUpdate = true;
                    logger.debug("Set serverConfigDialogShown=false for server: {} (will show dialog)", serverEntry.getServerName());
                }
                
                if (needsUpdate) {
                    storageManager.getServerStorage().saveServerEntry(serverEntry);
                    updatedEntries++;
                }
            }
            
            logger.info("Updated {} server entries with new configuration fields", updatedEntries);
            
        } catch (Exception e) {
            logger.error("Migration failed", e);
            throw e;
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.SERVER;
    }
    
    /**
     * Проверяет есть ли поле useServerSettings в объекте
     * (проверка через рефлексию или попытку получить значение)
     */
    private boolean hasUseServerSettingsField(ServerEntry serverEntry) {
        try {
            // Если поле есть и правильно инициализировано, метод отработает без исключений
            boolean value = serverEntry.isUseServerSettings();
            // Если мы дошли до сюда, поле существует
            return true;
        } catch (Exception e) {
            // Если произошло исключение, поле отсутствует или не инициализировано
            return false;
        }
    }
    
    /**
     * Проверяет есть ли поле serverConfigDialogShown в объекте
     */
    private boolean hasServerConfigDialogShownField(ServerEntry serverEntry) {
        try {
            // Если поле есть и правильно инициализировано, метод отработает без исключений
            boolean value = serverEntry.isServerConfigDialogShown();
            // Если мы дошли до сюда, поле существует
            return true;
        } catch (Exception e) {
            // Если произошло исключение, поле отсутствует или не инициализировано
            return false;
        }
    }
}