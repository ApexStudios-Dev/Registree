package dev.apexstudios.registree.data.provider;

import dev.apexstudios.registree.data.provider.types.LanguageProvider;
import net.minecraft.resources.Identifier;

public interface ProviderTypes {
    ProviderType<LanguageProvider> LANGUAGE = LanguageProviderImpl.PROVIDER_TYPE;

    static Identifier internal(String path) {
        return Identifier.fromNamespaceAndPath("registree_internal", path);
    }
}
