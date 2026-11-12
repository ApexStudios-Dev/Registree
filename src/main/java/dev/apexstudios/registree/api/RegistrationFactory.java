package dev.apexstudios.registree.api;

@FunctionalInterface
public interface RegistrationFactory<TRegistry, TValue extends TRegistry> {
    TValue register(Registration<TRegistry, TValue> registration);
}
