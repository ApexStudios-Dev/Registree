package dev.apexstudios.registree.data.provider;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import dev.apexstudios.registree.data.context.ProviderOutputContext;
import dev.apexstudios.registree.data.provider.types.LanguageProvider;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class LanguageProviderImpl implements BaseProvider, LanguageProvider {
    public static final ProviderType<LanguageProvider> PROVIDER_TYPE = ProviderType.create(ProviderTypes.internal("language"), LanguageProviderImpl::new);

    private final Map<String, String> translations = Maps.newTreeMap();

    private LanguageProviderImpl() { }

    @Override
    public CompletableFuture<?> generate(CachedOutput cache, ProviderOutputContext context) {
        var json = new JsonObject();
        translations.forEach(json::addProperty);

        if(json.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        var path = context.outputPath(PackOutput.Target.RESOURCE_PACK, "lang", "en_us.json");
        return DataProvider.saveStable(cache, json, path);
    }

    @Override
    public LanguageProvider add(String key, String value) {
        if(translations.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Duplicate translation: '" + key + "' -> '" + value + "'");
        }

        return this;
    }
}
