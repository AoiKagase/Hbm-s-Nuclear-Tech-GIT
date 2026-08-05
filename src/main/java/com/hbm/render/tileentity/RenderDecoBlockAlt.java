package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.render.RenderHelper;
import com.hbm.render.model.ModelGun;
import com.hbm.render.model.ModelStatue;
import com.hbm.tileentity.deco.TileEntityDecoBlockAlt;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

public class RenderDecoBlockAlt extends TileEntitySpecialRenderer<TileEntityDecoBlockAlt> {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":" + "textures/models/misc/ModelStatue.png");
	private static final ResourceLocation gunTexture = new ResourceLocation(RefStrings.MODID + ":" + "textures/models/weapons/ModelGun.png");

	private ModelStatue model;
	private ModelGun gun;
	private ItemStack watchStack;
	private IBakedModel watchModel;
	private RenderItem watchRenderItem;
	private World watchWorld;

	public RenderDecoBlockAlt() {
		this.model = new ModelStatue();
		this.gun = new ModelGun();
	}

	private void updateWatchModel(World world) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		if (watchModel != null && watchRenderItem == renderItem && watchWorld == world)
			return;

		watchStack = new ItemStack(ModItems.watch);
		watchRenderItem = renderItem;
		watchWorld = world;
		watchModel = renderItem.getItemModelWithOverrides(watchStack, world, null);
		watchModel = ForgeHooksClient.handleCameraTransforms(watchModel, TransformType.FIXED, false);
	}

	@Override
	public boolean isGlobalRenderer(TileEntityDecoBlockAlt te) {
		return true;
	}

	@Override
	public void render(TileEntityDecoBlockAlt te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glRotatef(180, 0F, 0F, 1F);
		switch(te.getBlockMetadata()) {
		case 4:
			GL11.glRotatef(90, 0F, 1F, 0F);
			break;
		case 2:
			GL11.glRotatef(180, 0F, 1F, 0F);
			break;
		case 5:
			GL11.glRotatef(270, 0F, 1F, 0F);
			break;
		case 3:
			GL11.glRotatef(0, 0F, 1F, 0F);
			break;
		}

		Block b = te.getBlockType();

		this.bindTexture(texture);
		this.model.renderModel(0.0625F);
		float g = 0.0625F;
		float q = g * 2 + 0.0625F / 3;
		GL11.glTranslatef(0.0F, -2 * g, q);
		GL11.glRotatef(180, 0F, 0F, 1F);
		if(b == ModBlocks.statue_elb_w || b == ModBlocks.statue_elb_f) {
			GL11.glPushMatrix();
			GL11.glTranslated(0, 0.11, 0);
			GL11.glScaled(0.5, 0.5, 0.5);
			updateWatchModel(te.getWorld());
			RenderHelper.bindBlockTexture();

			watchRenderItem.renderItem(watchStack, watchModel);
			GL11.glPopMatrix();
		}
		GL11.glTranslatef(0.0F, 2 * g, -q);
		GL11.glRotatef(180, 0F, 0F, 1F);
		GL11.glRotatef(90, 0F, 1F, 0F);
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		GL11.glTranslatef(-g * 20, g * 4, g * 11);
		GL11.glRotatef(-20, 0F, 0F, 1F);
		this.bindTexture(gunTexture);
		if(b == ModBlocks.statue_elb_g || b == ModBlocks.statue_elb_f)
			this.gun.renderModel(0.0625F);
		GL11.glPopMatrix();
	}
}
