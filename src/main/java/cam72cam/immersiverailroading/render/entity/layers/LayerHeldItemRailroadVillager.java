package cam72cam.immersiverailroading.render.entity.layers;

import cam72cam.immersiverailroading.entity.EntityRailroadVillager;
import cam72cam.immersiverailroading.entity.NpcConveyor;
import cam72cam.immersiverailroading.render.entity.RenderRailroadVillager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHandSide;

public class LayerHeldItemRailroadVillager implements LayerRenderer<EntityRailroadVillager>
{
    protected final RenderRailroadVillager livingEntityRenderer;

    public LayerHeldItemRailroadVillager(RenderRailroadVillager livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    public void doRenderLayer(EntityRailroadVillager entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        ItemStack itemstack = entitylivingbaseIn.getHeldItemMainhand();

        if (!itemstack.isEmpty())
        {
            GlStateManager.pushMatrix();

            if (this.livingEntityRenderer.getMainModel().isChild)
            {
                float f = 0.5F;
                GlStateManager.translate(0.0F, 0.75F, 0.0F);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            }

            this.renderHeldItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
            GlStateManager.popMatrix();
        }
    }

    private void renderHeldItem(EntityLivingBase p_188358_1_, ItemStack p_188358_2_, ItemCameraTransforms.TransformType p_188358_3_, EnumHandSide handSide)
    {
        if (!p_188358_2_.isEmpty())
        {
            GlStateManager.pushMatrix();

            if (p_188358_1_.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            GlStateManager.rotate(-135.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            //Original was GlStateManager.translate(1.0F / 16.0F, 0.125F, -0.625F);
            GlStateManager.translate(-0.7F / 16.0F, 0.0F, -0.50F);
            Minecraft.getMinecraft().getItemRenderer().renderItem(p_188358_1_, p_188358_2_, p_188358_3_);
            GlStateManager.popMatrix();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}