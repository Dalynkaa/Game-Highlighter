package me.dalynkaa.highlighter.client.translations;

import me.dalynkaa.highlighter.Highlighter;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class DynamicLanguageResourcePack implements ResourcePack {
    private final Path translationsDir;
    
    public DynamicLanguageResourcePack() {
        this.translationsDir = Path.of(".minecraft/highlighter_translations");
    }
    
    @Override
    public InputSupplier<InputStream> openRoot(String... path) {
        return null;
    }
    
    @Override
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        if (type != ResourceType.CLIENT_RESOURCES) {
            return null;
        }
        
        // Проверяем что это запрос языкового файла для нашего мода
        if (!"highlighter".equals(id.getNamespace()) || !id.getPath().startsWith("lang/")) {
            return null;
        }
        
        String langFileName = id.getPath().substring("lang/".length());
        Path langFile = translationsDir.resolve(langFileName);
        
        if (Files.exists(langFile)) {
            return () -> {
                try {
                    return Files.newInputStream(langFile);
                } catch (IOException e) {
                    Highlighter.LOGGER.warn("[DynamicLanguageResourcePack] Failed to read {}", langFile, e);
                    return new ByteArrayInputStream("{}".getBytes());
                }
            };
        }
        
        return null;
    }
    
    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResourcePack.ResultConsumer consumer) {
        if (type != ResourceType.CLIENT_RESOURCES || !"highlighter".equals(namespace) || !prefix.startsWith("lang/")) {
            return;
        }
        
        try {
            if (Files.exists(translationsDir)) {
                Files.list(translationsDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        Identifier resourceId = Identifier.of("highlighter", "lang/" + fileName);
                        consumer.accept(resourceId, open(type, resourceId));
                    });
            }
        } catch (IOException e) {
            Highlighter.LOGGER.warn("[DynamicLanguageResourcePack] Failed to find translation resources", e);
        }
    }
    
    @Override
    public Set<String> getNamespaces(ResourceType type) {
        if (type == ResourceType.CLIENT_RESOURCES) {
            return Set.of("highlighter");
        }
        return Set.of();
    }
    
    @Override
    public <T> T parseMetadata(ResourceMetadataSerializer<T> metadataSerializer) throws IOException {
        return null;
    }
    
    @Override
    public ResourcePackInfo getInfo() {
        //? if >=1.21.6 {
        /*return new ResourcePackInfo("highlighter_dynamic_translations", null, null, null);
        *///?} else {
        return new ResourcePackInfo("highlighter_dynamic_translations", null, null);
        //?}
    }
    
    @Override
    public void close() {
        // Ничего не нужно закрывать
    }
}