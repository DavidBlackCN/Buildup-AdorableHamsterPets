package net.dawson.adorablehamsterpets.registry;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A value handle populated when its owning {@link DeferredRegister} is registered.
 */
public final class RegistrySupplier<T> implements Supplier<T> {
    private T value;

    void bind(T value) {
        if (this.value != null) {
            throw new IllegalStateException("Registry supplier was already bound");
        }

        this.value = Objects.requireNonNull(value, "Registered value");
    }

    @Override
    public T get() {
        if (value == null) {
            throw new IllegalStateException("Registry value was accessed before registration");
        }

        return value;
    }
}
