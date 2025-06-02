package me.dalynkaa.highlighter.client.config.migrations;

import me.dalynkaa.highlighter.client.config.StorageManager;

public interface Migration {
    String getVersion();
    void apply(StorageManager storageManager) throws Exception;

    // Перечисление для определения типа хранилища
    enum StorageType {
        PREFIX,
        SERVER,
        BOTH
    }

    // По умолчанию применяем к обоим типам
    default StorageType getStorageType() {
        return StorageType.BOTH;
    }
}