package dev.apexstudios.registree.api.builder;

import java.util.function.Consumer;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface IBuilder<TRegistry, TElement extends TRegistry, THolder extends DeferredHolder<TRegistry, TElement>, TSelf extends IBuilder<TRegistry, TElement, THolder, TSelf>> {
    TSelf onRegister(Consumer<TElement> listener);

    THolder register();
}
