package me.dalynkaa.highlighter.client.config.migrations;

import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.config.ServerEntry;
import me.dalynkaa.highlighter.client.config.StorageManager;
import me.dalynkaa.highlighter.util.LogUtil;

import java.util.*;

public class MigrationManager {
    private final List<Migration> migrations = new ArrayList<>();
    private final StorageManager storageManager;
    private final LogUtil logger;

    public MigrationManager(StorageManager storageManager) {
        this.storageManager = storageManager;
        this.logger = Highlighter.LOGGER.child("Migration");
        registerMigrations();
    }

    private void registerMigrations() {
        migrations.add(new M201());
        migrations.add(new M202());

        migrations.sort(Comparator.comparing(Migration::getVersion, this::compareVersions));
        logger.debug("Registered {} migrations", migrations.size());
    }

    /**
     * Runs all required migrations
     */
    public void runMigrations() {
        logger.info("Checking if migrations are needed...");

        int appliedCount = 0;
        for (Migration migration : migrations) {
            if (shouldApplyMigration(migration)) {
                logger.info("Applying migration to version {}...", migration.getVersion());

                try {
                    migration.apply(storageManager);
                    appliedCount++;

                    if (migration.getStorageType() == Migration.StorageType.PREFIX ||
                            migration.getStorageType() == Migration.StorageType.BOTH) {
                        if (compareVersions(storageManager.getPrefixStorage().getVersion(), migration.getVersion()) < 0) {
                            storageManager.getPrefixStorage().setVersion(migration.getVersion());
                            storageManager.getPrefixStorage().save();
                            logger.debug("Updated prefix storage version to {}", migration.getVersion());
                        }
                    }

                    if (migration.getStorageType() == Migration.StorageType.SERVER ||
                            migration.getStorageType() == Migration.StorageType.BOTH) {
                        for (ServerEntry serverEntry : storageManager.getServerStorage().getAllServers()) {
                            if (compareVersions(serverEntry.getVersion(), migration.getVersion()) < 0) {
                                serverEntry.setVersion(migration.getVersion());
                                storageManager.getServerStorage().saveServerEntry(serverEntry);
                                logger.debug("Updated server entry {} version to {}",
                                        serverEntry.getServerName(), migration.getVersion());
                            }
                        }
                    }

                    logger.info("Migration to version {} successfully applied", migration.getVersion());
                } catch (Exception e) {
                    logger.error("Error applying migration {}: {}", migration.getVersion(), e.getMessage(), e);
                    break;
                }
            }
        }

        if (appliedCount > 0) {
            logger.info("Applied {} migrations", appliedCount);
        } else {
            logger.info("No migrations required");
        }
    }

    /**
     * Determines if a migration needs to be applied
     */
    private boolean shouldApplyMigration(Migration migration) {
        boolean needMigration = false;

        // Check prefixStorage
        if (migration.getStorageType() == Migration.StorageType.PREFIX ||
                migration.getStorageType() == Migration.StorageType.BOTH) {
            String prefixVersion = storageManager.getPrefixStorage().getVersion();
            if (compareVersions(prefixVersion, migration.getVersion()) < 0) {
                needMigration = true;
            }
        }

        // Check server storages
        if (migration.getStorageType() == Migration.StorageType.SERVER ||
                migration.getStorageType() == Migration.StorageType.BOTH) {
            for (ServerEntry serverEntry : storageManager.getServerStorage().getAllServers()) {
                String serverVersion = serverEntry.getVersion();
                if (compareVersions(serverVersion, migration.getVersion()) < 0) {
                    needMigration = true;
                    break;
                }
            }
        }

        return needMigration;
    }

    /**
     * Compares two versions in the format "x.y.z"
     * @return negative number if v1 < v2, positive if v1 > v2, 0 if equal
     */
    private int compareVersions(String v1, String v2) {
        if (v1 == null) return -1;
        if (v2 == null) return 1;

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (p1 != p2) {
                return p1 - p2;
            }
        }

        return 0;
    }

    /**
     * Gets the latest version after all migrations
     */
    public String getLatestVersion() {
        if (migrations.isEmpty()) {
            return Highlighter.MOD_VERSION;
        }

        return migrations.get(migrations.size() - 1).getVersion();
    }
}

