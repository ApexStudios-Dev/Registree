package dev.apexstudios.registree.core;

import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public final class Deferred<TValue> {
    @Nullable private TValue value = null;
    @Nullable private Consumer<TValue> listener = null;

    public void defer(Consumer<TValue> listener) {
        if(value == null) {
            this.listener = this.listener == null ? listener : this.listener.andThen(listener);
        } else {
            listener.accept(value);
        }
    }

    public void invoke(TValue value) {
        if(this.value != null) {
            return;
        }

        this.value = value;

        if(listener != null) {
            listener.accept(value);
            listener = null;
        }
    }
}
