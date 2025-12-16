package dev.apexstudios.registree.api.builder;

import dev.apexstudios.registree.api.holder.DeferredHolder;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;

public interface IBuilder<TRegistry, TRegistered extends TRegistry, TResult, TSelf extends IBuilder<TRegistry, TRegistered, TResult, TSelf>> {
    TSelf onRegister(Consumer<TRegistered> listener);

    TResult register();

    interface Basic<TRegistry, TSelf extends Basic<TRegistry, TSelf>> extends IBuilder<TRegistry, TRegistry, ResourceKey<TRegistry>, TSelf> {

    }

    interface WithHolder<TRegistry, TElement extends TRegistry, THolder extends DeferredHolder<TRegistry, TElement>, TSelf extends WithHolder<TRegistry, TElement, THolder, TSelf>> extends IBuilder<TRegistry, TElement, THolder, TSelf> {

    }
}
