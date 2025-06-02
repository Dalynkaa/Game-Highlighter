package me.dalynkaa.highlighter.client.config.migrations;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.config.StorageManager;
import me.dalynkaa.highlighter.util.LogUtil;

public class M201 implements Migration {
    private final LogUtil logger = Highlighter.LOGGER.child("Migration").child("M201");

    @Override
    public String getVersion() {
        return "2.0.1";
    }

    @Override
    public void apply(StorageManager storageManager) throws Exception {
        logger.info("Performing migration to version {}", getVersion());

        // Пример миграции данных с подробным логированием
        logger.debug("Starting data migration process");

        try {
            // Здесь будет реальная логика миграции
            logger.debug("Migration logic executed successfully");
        } catch (Exception e) {
            logger.error("Migration failed", e);
            throw e;
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.BOTH;
    }
}
