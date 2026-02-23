package dev.apexstudios.registree.data;

import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.data.pack.ModPackGeneratorImpl;
import dev.apexstudios.registree.data.pack.SubPackGeneratorImpl;
import dev.apexstudios.registree.data.pack.types.ModPackGenerator;
import dev.apexstudios.registree.data.pack.types.SubPackGenerator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;

public final class ResourceGenerator {
    private final Registree registree;
    private final ModPackGeneratorImpl pack;
    private final Map<String, SubPackGeneratorImpl> packs = Maps.newHashMap();

    @ApiStatus.Internal
    public ResourceGenerator(Registree registree) {
        this.registree = registree;

        pack = new ModPackGeneratorImpl(registree);

        if(isEnabled()) {
            registree.event(GatherDataEvent.Client.class, this::register);
            registree.event(GatherDataEvent.Server.class, this::register);
        }
    }

    @CanIgnoreReturnValue
    public ModPackGenerator pack() {
        return pack;
    }

    @CanIgnoreReturnValue
    public SubPackGenerator pack(String id) {
        return packs.computeIfAbsent(id, $ -> new SubPackGeneratorImpl(registree, id));
    }

    public boolean isEnabled() {
        return DatagenModLoader.isRunningDataGen();
    }

    private void register(GatherDataEvent event) {
        var backend = event.getGenerator();
        var outputDir = backend.getPackOutput().getOutputFolder();

        pack.defaultDescription(() -> Component.literal(event.getModContainer().getModInfo().getDisplayName()));
        var registries = pack.generate(pack.packOutput(outputDir), event::getResourceManager, event.getLookupProvider(), provider -> backend.addProvider(true, provider));

        packs.forEach((id, pack) -> {
            pack.defaultDescription(() -> Component.literal(toEnglishName(id)));

            pack.generate(pack.packOutput(outputDir), event::getResourceManager, registries, provider -> backend.addProvider(true, new DataProvider() {
                @Override
                public CompletableFuture<?> run(CachedOutput cachedOutput) {
                    return provider.run(cachedOutput);
                }

                @Override
                public String getName() {
                    return id + '/' + provider.getName();
                }
            }));
        });
    }

    public static String toEnglishName(String registryName) {
        return Stream.of(registryName.split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }
}
