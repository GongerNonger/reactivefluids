package com.reactivefluids;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Model for the Tropical Slime — mirrors vanilla SlimeModel structure.
 * Two layer definitions:
 *   - Outer body: translucent shell (8x8x8 cube), texture 64x32
 *   - Inner body: opaque core (6x6x6 cube + eyes + mouth), texture 64x32
 *     (inner texture is 32x1536 animated via mcmeta, but each frame is 32x32;
 *      the model treats the texture as 64x32 to match vanilla UV mapping)
 */
@OnlyIn(Dist.CLIENT)
public class TropicalSlimeModel<T extends TropicalSlimeEntity> extends HierarchicalModel<T> {

    private final ModelPart root;

    public TropicalSlimeModel(ModelPart root) {
        this.root = root;
    }

    /**
     * Outer shell — single 8x8x8 cube, same as vanilla slime outer.
     * Texture size 64x32, UV starts at (0,0).
     */
    public static LayerDefinition createOuterBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        // Eyes and mouth on the outer shell (Manus texture has them at UV 32,0)
        part.addOrReplaceChild("right_eye",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("left_eye",
                CubeListBuilder.create().texOffs(32, 4)
                        .addBox(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(32, 8)
                        .addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    /**
     * Inner body — 6x6x6 core + eyes + mouth, same as vanilla slime inner.
     * Texture size 64x32 (animated frames handled by mcmeta, each frame 32x32,
     * but we use 64x32 to match vanilla UV coords exactly).
     */
    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        // Inner body cube — UV at (0,16) on a 32x32 frame texture
        part.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // No animation needed — squish is handled by the renderer's scale()
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
