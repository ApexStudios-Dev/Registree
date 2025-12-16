package dev.apexstudios.registree.api.builder;

import dev.apexstudios.registree.api.holder.DeferredHolder;
import java.util.function.Consumer;

public interface IBuilder<TRegistry, TElement extends TRegistry, THolder extends DeferredHolder<TRegistry, TElement>, TSelf extends IBuilder<TRegistry, TElement, THolder, TSelf>> {
    TSelf onRegister(Consumer<TElement> listener);

    THolder register();
}
