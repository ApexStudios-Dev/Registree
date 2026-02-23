package dev.apexstudios.registree.data.pack.types;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface SubPackGenerator extends PackGenerator<SubPackGenerator> {
    @CanIgnoreReturnValue
    SubPackGenerator path(String path);

    @CanIgnoreReturnValue
    default SubPackGenerator path(String... path) {
        return path(String.join("/", path));
    }
}
