package me.dalynkaa.highlighter.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.HighlighterClient;
import me.dalynkaa.highlighter.client.utilities.data.HighlightedPlayer;
import me.dalynkaa.highlighter.client.utilities.data.Prefix;
import me.dalynkaa.highlighter.client.utilities.data.PrefixSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static me.dalynkaa.highlighter.Highlighter.MOD_VERSION;
import static me.dalynkaa.highlighter.client.config.StorageManager.mainConfigPath;

public class PrefixStorage {
    @Getter
    @Expose
    @SerializedName("prefixes")
    private LinkedHashSet<Prefix> prefixes;

    @Expose
    @SerializedName("version")
    @Getter @Setter
    private String version;

    @Expose(serialize = false)
    private static String fileName = "prefixes.json";

    @Expose(serialize = false)
    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public PrefixStorage(LinkedHashSet<Prefix> prefixes, String version) {
        this.version = version;
        this.prefixes = prefixes;
    }

    public static PrefixStorage read() {
        File configFile = mainConfigPath.resolve(fileName).toFile();
        if (configFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
                try {
                    PrefixStorage co = gson.fromJson(reader, PrefixStorage.class);
                    return Objects.requireNonNullElseGet(co, () -> new PrefixStorage(new LinkedHashSet<>(), MOD_VERSION));
                } catch (JsonSyntaxException j) {
                    boolean isDeleted = configFile.delete();
                    if (isDeleted) {
                        Highlighter.LOGGER.debug("[Configuration] Deleted Corrupted File!");
                    } else {
                        Highlighter.LOGGER.debug("[Configuration] Failed to Delete Corrupted File!");
                    }
                }
            } catch (FileNotFoundException ignored) {
            }
        }
        PrefixStorage prefixStorage = new PrefixStorage(new LinkedHashSet<>(), MOD_VERSION);
        prefixStorage.save();
        return prefixStorage;
    }

    public void save() {
        Highlighter.LOGGER.debug("[Configuration] Saving PrefixStorage...");
        File configFile = mainConfigPath.resolve(fileName).toFile();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            String json = gson.toJson(this);
            writer.write(json);
            Highlighter.LOGGER.debug("[Configuration] Prefix saved {}", configFile.getAbsolutePath());
        } catch (Exception exception) {
            exception.printStackTrace();
            Highlighter.LOGGER.debug("[Configuration] Failed to save PrefixStorage: {}", exception.getMessage());
        }
    }

    /**
     * Добавляет префикс в хранилище
     */
    public void addPrefix(Prefix prefix) {
        if (prefixes == null) {
            prefixes = new LinkedHashSet<>();
        }
        this.prefixes.add(prefix);
        save();
    }

    /**
     * Обновляет или добавляет префикс в хранилище
     */
    public void setPrefix(Prefix prefix) {
        if (!containsPrefix(prefix.getPrefixId())) {
            addPrefix(prefix);
            return;
        }
        for (Prefix p : this.prefixes) {
            if (p.getPrefixId().equals(prefix.getPrefixId())) {
                this.prefixes.remove(p);
                this.prefixes.add(prefix);
                break;
            }
        }
        save();
    }

    /**
     * Добавляет или обновляет префиксы из загруженной конфигурации
     * Локальные префиксы с тем же ID будут перезаписаны, если они имеют приоритет ниже
     */
    public void mergeServerPrefixes(Collection<Prefix> serverPrefixes) {
        if (serverPrefixes == null || serverPrefixes.isEmpty()) {
            return;
        }

        // Устанавливаем источник для всех серверных префиксов
        for (Prefix serverPrefix : serverPrefixes) {
            serverPrefix.setSource(PrefixSource.SERVER);

            // Проверяем, существует ли такой префикс локально
            Prefix existingPrefix = getPrefix(serverPrefix.getPrefixId());

            if (existingPrefix == null) {
                addPrefix(serverPrefix);
            } else if (existingPrefix.getSource() == PrefixSource.LOCAL &&
                      existingPrefix.getPriority() <= serverPrefix.getPriority()) {
                setPrefix(serverPrefix);
            }
        }

        // Ремонтируем индексы после слияния
        repairPrefixIndexes();
        save();

        Highlighter.LOGGER.debug("[Configuration] Merged {} server prefixes", serverPrefixes.size());
    }

    /**
     * Удаляет все серверные префиксы
     */
    public void removeAllServerPrefixes() {
        List<Prefix> toRemove = prefixes.stream()
                .filter(p -> p.getSource() == PrefixSource.SERVER)
                .collect(Collectors.toList());

        if (toRemove.isEmpty()) {
            return;
        }

        for (Prefix prefix : toRemove) {
            removePrefix(prefix.getPrefixId());
        }

        Highlighter.LOGGER.debug("[Configuration] Removed {} server prefixes", toRemove.size());
    }

    /**
     * Проверяет, содержит ли хранилище префикс с указанным ID
     */
    public boolean containsPrefix(UUID prefix_id) {
        for (Prefix prefix : this.prefixes) {
            if (prefix.getPrefixId().equals(prefix_id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Удаляет префикс и обновляет всех игроков, которые его использовали
     */
    public void removePrefix(UUID prefix_id) {
        for (Prefix prefix : this.prefixes) {
            if (prefix.getPrefixId().equals(prefix_id)) {
                this.prefixes.remove(prefix);
                break;
            }
        }

        // Обновляем всех игроков, которые использовали этот префикс
        for (ServerEntry entry: ServerStorage.getAllServerEntries()) {
            List<UUID> playersToUpdate = new ArrayList<>();

            for (HighlightedPlayer player : entry.getAll()) {
                if (player.getPrefixId() != null && player.getPrefixId().equals(prefix_id)) {
                    playersToUpdate.add(player.getUuid());
                }
            }

            for (UUID playerUuid : playersToUpdate) {
                entry.removePlayer(playerUuid);
                Highlighter.LOGGER.debug("[Configuration] Removed prefix {} from player {}", prefix_id, playerUuid);
            }

            entry.save();
        }

        save();
        repairPrefixIndexes();
    }

    /**
     * Восстанавливает порядок индексов префиксов
     */
    public void repairPrefixIndexes() {
        if (prefixes == null || prefixes.isEmpty()) {
            return;
        }

        // Сортируем префиксы сначала по источнику (SERVER перед LOCAL), затем по индексам
        List<Prefix> sortedPrefixes = new ArrayList<>(prefixes);
        sortedPrefixes.sort(Comparator
                .<Prefix, PrefixSource>comparing(Prefix::getSource)
                .thenComparingInt(Prefix::getIndex));

        int newIndex = 0;
        for (Prefix prefix : sortedPrefixes) {
            prefix.setIndex(newIndex++);
        }

        prefixes.clear();
        prefixes.addAll(sortedPrefixes);
        save();

        Highlighter.LOGGER.debug("[Configuration] Repaired prefix indexes successfully.");
    }

    /**
     * Возвращает префикс по его ID
     */
    public Prefix getPrefix(UUID prefix_id) {
        for (Prefix prefix : this.prefixes) {
            if (prefix.getPrefixId().equals(prefix_id)) {
                return prefix;
            }
        }
        return null;
    }

    /**
     * Получает все локальные префиксы
     */
    public List<Prefix> getLocalPrefixes() {
        return prefixes.stream()
                .filter(p -> p.getSource() == PrefixSource.LOCAL)
                .sorted(Comparator.comparingInt(Prefix::getIndex))
                .collect(Collectors.toList());
    }

    /**
     * Получает все серверные префиксы
     */
    public List<Prefix> getServerPrefixes() {
        return prefixes.stream()
                .filter(p -> p.getSource() == PrefixSource.SERVER)
                .sorted(Comparator.comparingInt(Prefix::getIndex))
                .collect(Collectors.toList());
    }

    /**
     * Перемещает префикс вверх в списке
     */
    public void movePrefixTop(Prefix prefix) {
        if (prefix == null || !prefixes.contains(prefix)) {
            return;
        }
        int currentIndex = prefix.getIndex();
        if (currentIndex <= 0) {
            return;
        }
        prefix.setIndex(currentIndex - 1);
        for (Prefix otherPrefix : prefixes) {
            if (otherPrefix != prefix && otherPrefix.getIndex() == currentIndex - 1) {
                otherPrefix.setIndex(currentIndex);
                break;
            }
        }
        save();
    }

    /**
     * Перемещает префикс вниз в списке
     */
    public void movePrefixDown(Prefix prefix) {
        if (prefix == null || !prefixes.contains(prefix)) {
            return;
        }
        int currentIndex = prefix.getIndex();
        int maxIndex = -1;
        for (Prefix p : prefixes) {
            if (p.getIndex() > maxIndex) {
                maxIndex = p.getIndex();
            }
        }
        if (currentIndex >= maxIndex) {
            return;
        }

        prefix.setIndex(currentIndex + 1);
        for (Prefix otherPrefix : prefixes) {
            if (otherPrefix != prefix && otherPrefix.getIndex() == currentIndex + 1) {
                otherPrefix.setIndex(currentIndex);
                break;
            }
        }
        save();
    }
}
