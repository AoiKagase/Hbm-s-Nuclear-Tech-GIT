package com.hbm.render.tileentity;

import com.hbm.tileentity.machine.TileEntityMachinePress;
import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineEPress;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

public class RenderEPress extends TileEntitySpecialRenderer<TileEntityMachineEPress> {

	private static final int ITEM_MODEL_CACHE_SIZE = 16;
	private final CachedItemModel[] itemModelCache = new CachedItemModel[ITEM_MODEL_CACHE_SIZE];
	private int nextItemModelCacheIndex;

	private CachedItemModel getCachedItemModel(int itemId, int meta, World world) {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		for (CachedItemModel cached : itemModelCache) {
			if (cached != null && cached.itemId == itemId && cached.meta == meta
					&& cached.world == world && cached.renderItem == renderItem) {
				return cached;
			}
		}

		CachedItemModel cached = itemModelCache[nextItemModelCacheIndex];
		itemModelCache[nextItemModelCacheIndex] = cached != null ? cached : new CachedItemModel();
		cached = itemModelCache[nextItemModelCacheIndex];
		nextItemModelCacheIndex = (nextItemModelCacheIndex + 1) % ITEM_MODEL_CACHE_SIZE;

		cached.itemId = itemId;
		cached.meta = meta;
		cached.world = world;
		cached.renderItem = renderItem;
		Item item = Item.getItemById(itemId);
		cached.stack = item == null ? ItemStack.EMPTY : new ItemStack(item, 1, meta);
		cached.model = null;
		if (!cached.stack.isEmpty() && !(item instanceof ItemBlock)) {
			cached.model = renderItem.getItemModelWithOverrides(cached.stack, world, null);
			cached.model = ForgeHooksClient.handleCameraTransforms(cached.model, TransformType.FIXED, false);
		}
		return cached;
	}

	private static final class CachedItemModel {
		private int itemId;
		private int meta;
		private World world;
		private RenderItem renderItem;
		private ItemStack stack;
		private IBakedModel model;
	}

	@Override
	public boolean isGlobalRenderer(TileEntityMachineEPress te) {
		return true;
	}
	
	@Override
	public void render(TileEntityMachineEPress te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GlStateManager.enableLighting();
		GL11.glRotatef(180, 0F, 1F, 0F);
		
		switch(te.getBlockMetadata()) {
		case 2:
			GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 4:
			GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 3:
			GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 5:
			GL11.glRotatef(180, 0F, 1F, 0F); break;
		}
		
		this.bindTexture(ResourceManager.epress_body_tex);
		
		ResourceManager.epress_body.renderAll();
			
	GL11.glPopMatrix();
	
    renderTileEntityAt2(te, x, y, z, partialTicks);
	}
	
	public void renderTileEntityAt2(TileEntityMachineEPress press, double x, double y, double z, float f) {
		GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5D, y + 1 + 1 - 0.125, z + 0.5D);
			GlStateManager.enableLighting();
			GL11.glRotatef(180, 0F, 1F, 0F);
			
			switch(press.getBlockMetadata()) {
			case 2:
				GL11.glRotatef(270, 0F, 1F, 0F); break;
			case 4:
				GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 3:
				GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 5:
				GL11.glRotatef(180, 0F, 1F, 0F); break;
			}

            float f1 = 0.875F * (press.prevProgress + (press.progress-press.prevProgress) * f)  / TileEntityMachinePress.maxProgress;
            GL11.glTranslated(0, -f1, 0);
		
			this.bindTexture(ResourceManager.epress_head_tex);
		
			ResourceManager.epress_head.renderAll();

            GlStateManager.enableLighting();
            GL11.glRotatef(180, 0F, 1F, 0F);
            GL11.glRotatef(-90, 1F, 0F, 0F);
            CachedItemModel cached = getCachedItemModel(press.stampItem, press.stampMeta, press.getWorld());

            if (cached.model != null) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                GL11.glTranslatef(0.125F, 0F, 0F);
                GL11.glRotatef(180, 0F, 1F, 0F);
                GL11.glScalef(0.5F, 0.5F, 0.5F);


                cached.renderItem.renderItem(cached.stack, cached.model);
            }
			
		GL11.glPopMatrix();
		
        renderTileEntityAt3(press, x, y, z, f);
    }
    
	public void renderTileEntityAt3(TileEntityMachineEPress press, double x, double y, double z, float f) {
		GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5D, y + 1, z + 0.5);
			GlStateManager.enableLighting();
			GL11.glRotatef(180, 0F, 1F, 0F);
			
			switch(press.getBlockMetadata()) {
			case 2:
				GL11.glRotatef(270, 0F, 1F, 0F); break;
			case 4:
				GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 3:
				GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 5:
				GL11.glRotatef(180, 0F, 1F, 0F); break;
			}

			GL11.glRotatef(90, 0F, 1F, 0F);
			GL11.glRotatef(-90, 1F, 0F, 0F);
			GL11.glTranslatef(1.0F, 1.0F - 0.0625F * 165/100, 0.0F);
			GL11.glTranslatef(-1, -1.15F, 0);

			CachedItemModel cached = getCachedItemModel(press.item, press.meta, press.getWorld());
			
			if (cached.model != null) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				GL11.glTranslatef(0.0F, 0.125F, 0.0F);
				GL11.glRotatef(180, 0F, 1F, 0F);
				GL11.glScalef(0.5F, 0.5F, 0.5F);
				cached.renderItem.renderItem(cached.stack, cached.model);
			}
			
		GL11.glPopMatrix();
    }
}
