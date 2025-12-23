package dev.apexstudios.registree.api.holder;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;

public final class DeferredAttachmentType<TValue> extends ApexDeferredHolder<AttachmentType<?>, AttachmentType<TValue>> {
    public DeferredAttachmentType(ResourceKey<AttachmentType<?>> registryKey) {
        super(registryKey);
    }
}
