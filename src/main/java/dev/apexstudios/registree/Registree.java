package dev.apexstudios.registree;

public final class Registree extends BaseRegistree<Registree> {
    private Registree(String namespace) {
        super(namespace);
    }

    public static Registree create(String namespace) {
        return new Registree(namespace);
    }
}
