package net.dawson.adorablehamsterpets.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Small Fabric-only deferred registry used to preserve deterministic registration order.
 */
public final class DeferredRegister<T> {
    private static final ThreadLocal<ResourceKey<?>> ACTIVE_KEY = new ThreadLocal<>();
    private final String namespace;
    private final Registry<T> registry;
    private final List<Runnable> registrations = new ArrayList<>();
    private boolean registered;

    private DeferredRegister(String namespace, Registry<T> registry) {
        this.namespace = namespace;
        this.registry = registry;
    }

    public static <T> DeferredRegister<T> create(String namespace, Registry<T> registry) {
        return new DeferredRegister<>(namespace, registry);
    }

    public <S extends T> RegistrySupplier<S> register(String path, Supplier<S> factory) {
        if (registered) {
            throw new IllegalStateException("Cannot add registry entries after registration");
        }

        RegistrySupplier<S> handle = new RegistrySupplier<>();
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        ResourceKey<T> key = ResourceKey.create(registry.key(), id);
        registrations.add(() -> {
            ACTIVE_KEY.set(key);
            try {
                handle.bind(Registry.register(registry, key, factory.get()));
            } finally {
                ACTIVE_KEY.remove();
            }
        });
        return handle;
    }

    @SuppressWarnings("unchecked")
    public static <T> ResourceKey<T> activeKey(Registry<T> registry) {
        ResourceKey<?> key = ACTIVE_KEY.get();
        if (key == null || !key.isFor(registry.key())) {
            throw new IllegalStateException("Registry properties requested outside their registration factory");
        }
        return (ResourceKey<T>) key;
    }

    public void register() {
        if (registered) {
            return;
        }

        registered = true;
        registrations.forEach(Runnable::run);
        registrations.clear();
    }
}
