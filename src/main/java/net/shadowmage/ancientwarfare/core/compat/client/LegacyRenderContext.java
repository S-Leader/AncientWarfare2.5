package net.shadowmage.ancientwarfare.core.compat.client;

import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.Nullable;

/**
 * Supplies the active modern pose stack to legacy renderer helpers.
 */
public final class LegacyRenderContext {
    private static final ThreadLocal<PoseStack> ACTIVE_POSE = new ThreadLocal<>();

    private LegacyRenderContext() {
    }

    @Nullable
    public static PoseStack pose() {
        return ACTIVE_POSE.get();
    }

    static void run(PoseStack poseStack, Runnable renderCall) {
        PoseStack previous = ACTIVE_POSE.get();
        ACTIVE_POSE.set(poseStack);
        try {
            renderCall.run();
        } finally {
            if (previous == null) {
                ACTIVE_POSE.remove();
            } else {
                ACTIVE_POSE.set(previous);
            }
        }
    }
}
