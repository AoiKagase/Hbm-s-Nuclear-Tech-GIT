package com.hbm.render.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineSolderingStation;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RenderSolderingStation extends TileEntitySpecialRenderer<TileEntityMachineSolderingStation> {

	private ItemStack cachedDisplay = ItemStack.EMPTY;
	private boolean cachedDisplayPresent;
	private IBakedModel cachedDisplayModel;
	private World cachedWorld;
	private RenderItem cachedRenderItem;

	private void updateCachedDisplay(ItemStack display, World world) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		boolean present = display != null && !display.isEmpty();
		if (cachedWorld == world && cachedRenderItem == renderItem && cachedDisplayPresent == present
				&& (!present || (cachedDisplay.getCount() == display.getCount()
						&& ItemStack.areItemStacksEqual(cachedDisplay, display)))) {
			return;
		}

		cachedWorld = world;
		cachedRenderItem = renderItem;
		cachedDisplayPresent = present;
		cachedDisplay = present ? display.copy() : ItemStack.EMPTY;
		cachedDisplayModel = null;
		if (present) {
			cachedDisplayModel = renderItem.getItemModelWithOverrides(cachedDisplay, world, null);
			cachedDisplayModel = ForgeHooksClient.handleCameraTransforms(cachedDisplayModel,
					ItemCameraTransforms.TransformType.FIXED, false);
		}
	}
	
	@Override
	public void render(TileEntityMachineSolderingStation solderer, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		switch(solderer.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
		}
		
		GL11.glTranslated(-0.5, 0, 0.5);
		
		bindTexture(ResourceManager.soldering_station_tex);
		ResourceManager.soldering_station.renderAll();

		updateCachedDisplay(solderer.display, solderer.getWorld());
		if(cachedDisplayModel != null) {
			GL11.glPushMatrix();
			GL11.glTranslated(0, 1.125D, 0D);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glRotatef(90, 0F, 1F, 0F);
			GL11.glRotatef(-90, 1F, 0F, 0F);
			GL11.glRotatef(180, 0F, 1F, 0F);

			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			cachedRenderItem.renderItem(cachedDisplay, cachedDisplayModel);
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();
	}
}
