package net.caffeinemc.mods.sodium.mixin.features.render.particle;

import net.caffeinemc.mods.sodium.api.vertex.format.common.ParticleVertex;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleMixin extends Particle {
    @Shadow
    public abstract float getQuadSize(float tickDelta);

    @Shadow
    protected abstract float getU0();

    @Shadow
    protected abstract float getU1();

    @Shadow
    protected abstract float getV0();

    @Shadow
    protected abstract float getV1();

    @Unique
    private Vector3f transferVector = new Vector3f();

    protected SingleQuadParticleMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    /**
     * @reason Optimize function
     * @author JellySquid
     */
    @Overwrite
protected void renderRotatedQuad(VertexConsumer vertexConsumer, Quaternionf quaternionf, float x, float y, float z, float tickDelta) {
    float size = getQuadSize(tickDelta);
    float minU = getU0();
    float maxU = getU1();
    float minV = getV0();
    float maxV = getV1();
    int light = getLightColor(tickDelta);

    var writer = VertexBufferWriter.of(vertexConsumer);
    int color = ColorABGR.pack(rCol, gCol, bCol, alpha);

    try (MemoryStack stack = MemoryStack.stackPush()) {
        long buffer = stack.nmalloc(4 * ParticleVertex.STRIDE);
        ParticleVertex.put(buffer, transferVector.set(1.0F, -1.0F, 0.0f).rotate(quaternionf).mul(size).add(x, y, z), maxU, maxV, color, light);
        ParticleVertex.put(buffer + ParticleVertex.STRIDE, transferVector.set(1.0F, 1.0F, 0.0f).rotate(quaternionf).mul(size).add(x, y, z), maxU, minV, color, light);
        ParticleVertex.put(buffer + 2 * ParticleVertex.STRIDE, transferVector.set(-1.0F, 1.0F, 0.0f).rotate(quaternionf).mul(size).add(x, y, z), minU, minV, color, light);
        ParticleVertex.put(buffer + 3 * ParticleVertex.STRIDE, transferVector.set(-1.0F, -1.0F, 0.0f).rotate(quaternionf).mul(size).add(x, y, z), minU, maxV, color, light);

        writer.push(stack, buffer, 4, ParticleVertex.FORMAT);
    }
}
}
