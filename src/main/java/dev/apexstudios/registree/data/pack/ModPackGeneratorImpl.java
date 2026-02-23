package dev.apexstudios.registree.data.pack;

import dev.apexstudios.registree.Registree;
import dev.apexstudios.registree.data.pack.types.ModPackGenerator;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ModPackGeneratorImpl extends BasePackGenerator<ModPackGenerator> implements ModPackGenerator {
    @ApiStatus.Internal
    public ModPackGeneratorImpl(Registree registree) {
        super(registree);
    }
}
