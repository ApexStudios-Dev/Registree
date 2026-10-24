package dev.apexstudios.registree.api;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.function.Consumer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ModEventBus {
    @CanIgnoreReturnValue
    default <TEvent extends Event & IModBusEvent> ModEventBus addListener(Consumer<TEvent> consumer) {
        return addListener(EventPriority.NORMAL, false, consumer);
    }

    @CanIgnoreReturnValue
    default <TEvent extends Event & IModBusEvent> ModEventBus addListener(Class<TEvent> eventType, Consumer<TEvent> consumer) {
        return addListener(EventPriority.NORMAL, false, eventType, consumer);
    }

    @CanIgnoreReturnValue
    default <TEvent extends Event & IModBusEvent> ModEventBus addListener(EventPriority priority, Consumer<TEvent> consumer) {
        return addListener(priority, false, consumer);
    }

    @CanIgnoreReturnValue
    default <TEvent extends Event & IModBusEvent> ModEventBus addListener(EventPriority priority, Class<TEvent> eventType, Consumer<TEvent> consumer) {
        return addListener(priority, false, eventType, consumer);
    }

    @CanIgnoreReturnValue
    <TEvent extends Event & IModBusEvent> ModEventBus addListener(EventPriority priority, boolean receiveCanceled, Consumer<TEvent> consumer);

    @CanIgnoreReturnValue
    <TEvent extends Event & IModBusEvent> ModEventBus addListener(EventPriority priority, boolean receiveCanceled, Class<TEvent> eventType, Consumer<TEvent> consumer);

    @CanIgnoreReturnValue
    default <TEvent extends Event & IModBusEvent> ModEventBus addListener(boolean receiveCanceled, Consumer<TEvent> consumer) {
        return addListener(EventPriority.NORMAL, receiveCanceled, consumer);
    }

    @CanIgnoreReturnValue
    default <TEvent extends Event & IModBusEvent> ModEventBus addListener(boolean receiveCanceled, Class<TEvent> eventType, Consumer<TEvent> consumer) {
        return addListener(EventPriority.NORMAL, receiveCanceled, eventType, consumer);
    }
}
