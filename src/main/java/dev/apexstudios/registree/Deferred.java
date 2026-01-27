package dev.apexstudios.registree;

import com.google.common.util.concurrent.Runnables;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public sealed interface Deferred<TValue> extends Supplier<TValue>, Predicate<TValue>, Callable<TValue> {
    @CanIgnoreReturnValue
    Deferred<TValue> whenAvailable(Consumer<TValue> action);

    @CanIgnoreReturnValue
    @SuppressWarnings("DataFlowIssue")
    default Deferred<TValue> ifAvailable(Consumer<TValue> action, Runnable emptyAction) {
        if(isAvailable()) {
            action.accept(get());
        } else {
            emptyAction.run();
        }

        return this;
    }

    @CanIgnoreReturnValue
    default Deferred<TValue> ifAvailable(Consumer<TValue> action) {
        return ifAvailable(action, Runnables.doNothing());
    }

    boolean isAvailable();

    @Override
    @Nullable
    TValue get();

    default TValue getOrThrow() {
        return Objects.requireNonNull(get(), "Deferred value was accessed before notify() was called.");
    }

    @SuppressWarnings({"NullableProblems", "DataFlowIssue"})
    default Optional<TValue> asOptional() {
        return isAvailable() ? Optional.of(get()) : Optional.empty();
    }

    @SuppressWarnings("NullableProblems")
    default Stream<TValue> stream() {
        return isAvailable() ? Stream.of(get()) : Stream.empty();
    }

    default View<TValue> readOnly() {
        return this instanceof Deferred.View<TValue> view ? view : new View<>(this);
    }

    @Override
    default boolean test(TValue value) {
        return isAvailable() && Objects.equals(get(), value);
    }

    @Override
    default TValue call() throws Exception {
        return getOrThrow();
    }

    static <TValue> Notifiable<TValue> create() {
        return new Instance<>();
    }

    static <TValue> Deferred<TValue> lazy(Supplier<TValue> factory) {
        return (factory instanceof Deferred<TValue> deferred ? deferred : new Lazy<>(factory)).readOnly();
    }

    sealed interface Notifiable<TValue> extends Deferred<TValue>, Consumer<TValue> {
        @CanIgnoreReturnValue
        Notifiable<TValue> notify(TValue value);

        @ApiStatus.Obsolete
        @Override
        default void accept(TValue value) {
            notify(value);
        }

        @Override
        Notifiable<TValue> whenAvailable(Consumer<TValue> action);

        @Override
        default Notifiable<TValue> ifAvailable(Consumer<TValue> action, Runnable emptyAction) {
            return (Notifiable<TValue>) Deferred.super.ifAvailable(action, emptyAction);
        }

        @Override
        default Notifiable<TValue> ifAvailable(Consumer<TValue> action) {
            return (Notifiable<TValue>) Deferred.super.ifAvailable(action);
        }
    }

    final class Instance<TValue> implements Notifiable<TValue> {
        private final Object lock = new Object();
        private @Nullable TValue value = null;
        private @Nullable Consumer<TValue> action = null;

        private Instance() {

        }

        @Override
        public Instance<TValue> notify(TValue value) {
            Objects.requireNonNull(value);

            Consumer<TValue> toRun;

            synchronized (lock) {
                if(this.value != null) {
                    throw new IllegalStateException("Deferred values can only be notified once!");
                }

                this.value = value;
                toRun = action;
                action = null;
            }

            if(toRun != null) {
                toRun.accept(value);
            }

            return this;
        }

        @Override
        public Instance<TValue> whenAvailable(Consumer<TValue> action) {
            Objects.requireNonNull(action);

            TValue valueSnapshot;

            synchronized (lock) {
                valueSnapshot = value;

                if(valueSnapshot == null) {
                    this.action = this.action == null ? action : this.action.andThen(action);
                }
            }

            if(valueSnapshot != null) {
                action.accept(valueSnapshot);
            }

            return this;
        }

        @Override
        public boolean isAvailable() {
            synchronized (lock) {
                return value != null;
            }
        }

        @Override
        public @Nullable TValue get() {
            synchronized (lock) {
                return value;
            }
        }

        @Override
        public Instance<TValue> ifAvailable(Consumer<TValue> action, Runnable emptyAction) {
            return (Instance<TValue>) Notifiable.super.ifAvailable(action, emptyAction);
        }

        @Override
        public Instance<TValue> ifAvailable(Consumer<TValue> action) {
            return (Instance<TValue>) Notifiable.super.ifAvailable(action);
        }
    }

    final class View<TValue> implements Deferred<TValue> {
        private final Deferred<TValue> delegate;

        private View(Deferred<TValue> delegate) {
            this.delegate = delegate;
        }

        @Override
        public View<TValue> whenAvailable(Consumer<TValue> action) {
            delegate.whenAvailable(action);
            return this;
        }

        @Override
        public boolean isAvailable() {
            return delegate.isAvailable();
        }

        @Override
        public @Nullable TValue get() {
            return delegate.get();
        }

        @Override
        public View<TValue> ifAvailable(Consumer<TValue> action, Runnable emptyAction) {
            return (View<TValue>) Deferred.super.ifAvailable(action, emptyAction);
        }

        @Override
        public View<TValue> ifAvailable(Consumer<TValue> action) {
            return (View<TValue>) Deferred.super.ifAvailable(action);
        }
    }

    final class Lazy<TValue> implements Deferred<TValue> {
        private final Object lock = new Object();
        private @Nullable TValue value = null;
        private @Nullable Consumer<TValue> action = null;
        private @Nullable Supplier<TValue> factory;

        private Lazy(Supplier<TValue> factory) {
            this.factory = factory;
        }

        @Override
        public Lazy<TValue> whenAvailable(Consumer<TValue> action) {
            Objects.requireNonNull(action);

            TValue valueSnapshot;

            synchronized (lock) {
                valueSnapshot = value;

                if(valueSnapshot == null) {
                    this.action = this.action == null ? action : this.action.andThen(action);
                }
            }

            if(valueSnapshot != null) {
                action.accept(valueSnapshot);
            }

            return this;
        }

        @Override
        public @Nullable TValue get() {
            Consumer<TValue> toRun = null;
            TValue result;

            synchronized (lock) {
                if(factory != null) {
                    this.value = Objects.requireNonNull(factory.get());
                    factory = null;
                    toRun = action;
                    action = null;
                }

                result = value;
            }

            if(toRun != null) {
                toRun.accept(result);
            }

            return result;
        }

        @Override
        public boolean isAvailable() {
            synchronized (lock) {
                return value != null;
            }
        }

        @Override
        public Lazy<TValue> ifAvailable(Consumer<TValue> action, Runnable emptyAction) {
            return (Lazy<TValue>) Deferred.super.ifAvailable(action, emptyAction);
        }

        @Override
        public Lazy<TValue> ifAvailable(Consumer<TValue> action) {
            return (Lazy<TValue>) Deferred.super.ifAvailable(action);
        }
    }
}
