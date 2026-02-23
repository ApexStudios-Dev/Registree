package dev.apexstudios.registree.data.pack;

import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.data.pack.types.SubPackGenerator;
import java.nio.file.Path;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public final class SubPackGeneratorImpl extends BasePackGenerator<SubPackGenerator> implements SubPackGenerator {
    private final String packId;
    private @Nullable String path = null;

    @ApiStatus.Internal
    public SubPackGeneratorImpl(Registree registree, String packId) {
        super(registree);

        this.packId = packId;
    }

    @Override
    public SubPackGenerator path(String path) {
        this.path = path;
        return self;
    }

    @Override
    public PackOutput packOutput(Path outputDir) {
        if(path != null && !path.isBlank()) {
            return new PackOutput(outputDir.resolve(path));
        }

        return new PackOutput(outputDir.resolve("packs").resolve(packId));
    }
}
