package cam72cam.immersiverailroading.render.entity;

import cam72cam.immersiverailroading.entity.EntityRailroadVillager;
import cam72cam.immersiverailroading.render.entity.layers.LayerHeldItemRailroadVillager;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRailroadVillager extends RenderLiving<EntityRailroadVillager>
{
    private static final ResourceLocation VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/villager.png");
    private static final ResourceLocation FARMER_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/farmer.png");
    private static final ResourceLocation LIBRARIAN_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/librarian.png");
    private static final ResourceLocation PRIEST_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/priest.png");
    private static final ResourceLocation SMITH_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/smith.png");
    private static final ResourceLocation BUTCHER_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/butcher.png");

    public RenderRailroadVillager(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelVillager(0.0F), 0.5F);
        //this.addLayer(new LayerHeldItemRailroadVillager(this));
        this.addLayer(new LayerCustomHead(this.getMainModel().villagerHead));
    }

    public ModelVillager getMainModel()
    {
        return (ModelVillager)super.getMainModel();
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityRailroadVillager entity)
    {
        return FARMER_VILLAGER_TEXTURES;
    }

    public void transformHeldFull3DItemLayer()
    {
        GlStateManager.translate(0.0F, 0.1875F, 0.0F);
    }

    
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(RenderRailroadVillager entitylivingbaseIn, float partialTickTime)
    {
        float f = 0.9375F;
        this.shadowSize = 0.5F;

        GlStateManager.scale(f, f, f);
    }

}