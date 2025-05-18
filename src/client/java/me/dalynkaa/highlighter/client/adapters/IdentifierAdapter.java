package me.dalynkaa.highlighter.client.adapters;

import net.minecraft.util.Identifier;

/**
 * Адаптер для работы с идентификаторами Minecraft
 * Содержит методы, которые могут изменяться при обновлении Minecraft
 */
public class IdentifierAdapter {
    
    /**
     * Создает идентификатор из строки с указанным пространством имен
     * @param namespace Пространство имен
     * @param path Путь
     * @return Идентификатор
     */
    public static Identifier create(String namespace, String path) {
        return Identifier.tryParse(namespace, path);
    }
    
    /**
     * Создает идентификатор из строки
     * @param path Путь (с опциональным пространством имен через ":")
     * @return Идентификатор
     */
    public static Identifier of(String path) {
        return Identifier.of(path);
    }
    
    /**
     * Создает ванильный идентификатор
     * @param path Путь внутри ванильного пространства имен
     * @return Ванильный идентификатор
     */
    public static Identifier ofVanilla(String path) {
        return Identifier.ofVanilla(path);
    }
}
