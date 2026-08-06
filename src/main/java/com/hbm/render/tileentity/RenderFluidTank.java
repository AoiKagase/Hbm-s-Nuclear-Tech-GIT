package com.hbm.render.tileentity;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.forgefluid.FFUtils;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineFluidTank;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class RenderFluidTank extends TileEntitySpecialRenderer<TileEntityMachineFluidTank> {

	private static final ResourceLocation GENERIC_TANK_TEXTURE = new ResourceLocation(RefStrings.MODID, "textures/models/tank/tank_generic.png");
	private final Map<Fluid, ResourceLocation> fluidTextures = new IdentityHashMap<>();
	private final Map<Fluid, Integer> fallbackColors = new IdentityHashMap<>();
	
	@Override
	public void render(TileEntityMachineFluidTank te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
		switch(te.getBlockMetadata())
		{
		case 2:
			GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 4:
			GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 3:
			GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 5:
			GL11.glRotatef(180, 0F, 1F, 0F); break;
		}

        bindTexture(ResourceManager.tank_tex);
        ResourceManager.fluidtank.renderPart("Frame");

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GL11.glPopMatrix();
        renderTileEntityAt2(te, x, y, z, partialTicks);
	}
	
	public void renderTileEntityAt2(TileEntity tileEntity, double x, double y, double z, float f)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
		switch(tileEntity.getBlockMetadata())
		{
		case 2:
			GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 4:
			GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 3:
			GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 5:
			GL11.glRotatef(180, 0F, 1F, 0F); break;
		}

		Fluid type = null;
		if(tileEntity instanceof TileEntityMachineFluidTank){
			if(((TileEntityMachineFluidTank)tileEntity).tank.getFluid() != null){
				type = ((TileEntityMachineFluidTank)tileEntity).tank.getFluid().getFluid();
			}
		}

		ResourceLocation rotTexture = getFluidTexture(type);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
		bindTexture(rotTexture);
        ResourceManager.fluidtank.renderPart("Tank");
        GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableCull();
        GL11.glPopMatrix();
    }

	private ResourceLocation getFluidTexture(Fluid type) {
		if(type == null)
			return GENERIC_TANK_TEXTURE;

		ResourceLocation texture = fluidTextures.get(type);
		if(texture == null) {
			String name = FluidRegistry.getFluidName(type).toUpperCase();
			if(name.substring(0, 3).equals("HBM"))
				name = name.substring(3);

			ResourceLocation candidate = new ResourceLocation(RefStrings.MODID, "textures/models/tank/tank_" + name + ".png");
			try {
				Minecraft.getMinecraft().getResourceManager().getResource(candidate);
				texture = candidate;
			} catch (IOException e) {
				// Keep the resource lookup and fluid color calculation off the render hot path.
				texture = GENERIC_TANK_TEXTURE;
				fallbackColors.put(type, ModForgeFluids.getFluidColor(type));
			}
			fluidTextures.put(type, texture);
		}

		Integer fallbackColor = fallbackColors.get(type);
		if(fallbackColor != null)
			FFUtils.setRGBFromHex(fallbackColor);
		return texture;
	}
}
