package dev.apexstudios.registree.core;

import dev.apexstudios.registree.api.ModEventBus;
import java.util.function.Consumer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import org.jspecify.annotations.Nullable;

final class ModEventBusImpl implements ModEventBus {
    private @Nullable IEventBus modBus = null;
    private @Nullable Consumer<IEventBus> action;

    public void register(IEventBus modBus) {
        if(this.modBus != null) {
            throw new IllegalStateException("ModEventBus.register(IEventBus) may only be called once!");
        }

        if(action != null) {
            action.accept(modBus);
            action = null;
        }

        this.modBus = modBus;
    }

    @Override
    public <TEvent extends Event & IModBusEvent> ModEventBus addListener(EventPriority priority, boolean receiveCanceled, Consumer<TEvent> consumer) {
        return with(modBus -> modBus.addListener(priority, receiveCanceled, consumer));
    }

    @Override
    public <TEvent extends Event & IModBusEvent> ModEventBus addListener(EventPriority priority, boolean receiveCanceled, Class<TEvent> eventType, Consumer<TEvent> consumer) {
        return with(modBus -> modBus.addListener(priority, receiveCanceled, eventType, consumer));
    }

    private ModEventBus with(Consumer<IEventBus> action) {
        if(modBus == null) {
            this.action = this.action == null ? action : this.action.andThen(action);
        } else {
            action.accept(modBus);
        }

        return this;
    }
}
