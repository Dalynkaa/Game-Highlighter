package me.dalynkaa.highlighter.client.config.migrations;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.config.StorageManager;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import me.dalynkaa.highlighter.client.utilities.data.PrefixSource;
import me.dalynkaa.highlighter.util.LogUtil;

public class M202 implements Migration {
    private final LogUtil logger = Highlighter.LOGGER.child("Migration").child("M201");

    @Override
    public String getVersion() {
        return "2.0.2";
    }

    @Override
    public void apply(StorageManager storageManager) throws Exception {
        logger.info("Performing migration to version {}", getVersion());
        logger.debug("Starting data migration process");

        try {
            for (Prefix prefix : storageManager.getPrefixStorage().getPrefixes()) {
                if (prefix.getSource() == null){
                    prefix.setSource(PrefixSource.LOCAL);
                }
            }
        } catch (Exception e) {
            logger.error("Migration failed", e);
            throw e;
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.PREFIX;
    }
}
