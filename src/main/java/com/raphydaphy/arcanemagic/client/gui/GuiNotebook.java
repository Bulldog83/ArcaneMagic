package com.raphydaphy.arcanemagic.client.gui;

import java.io.IOException;

import com.raphydaphy.arcanemagic.ArcaneMagic;
import com.raphydaphy.arcanemagic.api.ArcaneMagicAPI;
import com.raphydaphy.arcanemagic.api.notebook.INotebookEntry;
import com.raphydaphy.arcanemagic.handler.ArcaneMagicSoundHandler;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiNotebook extends GuiScreen
{
	// width and height of the texture, and of just the book area
	public static final int NOTEBOOK_WIDTH = 232;
	public static final int NOTEBOOK_HEIGHT = 180;
	public static final int NOTEBOOK_TEX_HEIGHT = 200;

	// Scale up the notebook so it isn't so tiny
	final double scale = 1.5;

	public static final String tagUsedNotebook = "usedNotebook";

	// Current category selected
	public static final String tagCategory = "notebookCategory";

	// Page within the selected category
	public static final String tagPage = "notebookPage";

	// Page on the list of pages
	public static final String tagIndexPage = "notebookIndexPage";

	// Because for some reason the mouse information provided in onMouseClick is wrong
	public int relMouseX = 0;
	public int relMouseY = 0;

	// The player, to store entity data
	private EntityPlayer player;

	public static final ResourceLocation notebook = new ResourceLocation(ArcaneMagic.MODID,
			"textures/gui/notebook.png");

	public GuiNotebook(EntityPlayer player)
	{
		this.player = player;

		if (player.getEntityData().getBoolean(tagUsedNotebook) == false)
		{
			System.out.println("Player opened Notebook for first time, doing initial setup.");
			// player.getEntityData().setBoolean(tagUsedNotebook, true);
			player.getEntityData().setInteger(tagCategory, 0);
			player.getEntityData().setInteger(tagPage, 0);
			player.getEntityData().setInteger(tagIndexPage, 0);
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.setGuiSize(mc.displayWidth, mc.displayHeight);
	}

	private void drawArrow(boolean isLeft, int x, int y, int mouseX, int mouseY, int screenX, int screenY)
	{
		int offset = isLeft ? 2 : 22;

		if (mouseX >= screenX + x && mouseY >= screenY + y && mouseX <= screenX + x + (18 * scale)
				&& mouseY <= screenY + y + (10 * scale))
		{
			offset += 40;
		}
		drawScaledCustomSizeModalRect(screenX + x, screenY + y, offset, 182, 18, 10, (int) (18 * scale),
				(int) (10 * scale), NOTEBOOK_WIDTH, NOTEBOOK_TEX_HEIGHT);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		// Save the real mouse coordinates for access in onMouseClick
		relMouseX = mouseX;
		relMouseY = mouseY;

		// Because having shading is important
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);

		// Don't break the rest of the world
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		// Because some of the textures are only semi-opaque
		GlStateManager.enableBlend();

		// MC Screen resolution based on GUI scale
		ScaledResolution res = new ScaledResolution(mc);

		// Size of the notebook when taking the larger scale into account
		final int SCALED_NOTEBOOK_WIDTH = (int) (NOTEBOOK_WIDTH * scale);
		final int SCALED_NOTEBOOK_HEIGHT = (int) (NOTEBOOK_HEIGHT * scale);

		// The start x and y coords of the notebook on the screen
		int screenX = (res.getScaledWidth() / 2) - (SCALED_NOTEBOOK_WIDTH / 2);
		int screenY = (res.getScaledHeight() / 2) - (SCALED_NOTEBOOK_HEIGHT / 2);

		// Main notebook texture
		mc.getTextureManager().bindTexture(notebook);
		drawScaledCustomSizeModalRect(screenX, screenY, 0, 0, NOTEBOOK_WIDTH, NOTEBOOK_HEIGHT, SCALED_NOTEBOOK_WIDTH,
				SCALED_NOTEBOOK_HEIGHT, NOTEBOOK_WIDTH, NOTEBOOK_TEX_HEIGHT);

		// Some arrows! These ones let you choose your category page
		drawArrow(true, 13, 240, mouseX, mouseY, screenX, screenY);
		drawArrow(false, 89, 240, mouseX, mouseY, screenX, screenY);

		// Selected Category indicator
		int curCategory = player.getEntityData().getInteger(tagCategory);
		mc.getTextureManager().bindTexture(notebook);
		drawScaledCustomSizeModalRect((int) ((screenX + 13) + (1 * scale)),
				(int) ((screenY + 14 + (curCategory * 20)) + (1 * scale)), 86, 182, 70, 16, (int) (70 * scale),
				(int) (16 * scale), NOTEBOOK_WIDTH, NOTEBOOK_TEX_HEIGHT);

		int curY = 0;
		for (INotebookEntry entry : ArcaneMagicAPI.getNotebookCategories().get(curCategory).getEntries())
		{
			entry.draw(screenX + 145, screenY + 40 + curY, mouseX, mouseY, this);
			curY += entry.getHeight(this) + 5;
		}

		// Custom matrix for drawing scaled strings
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		// Current Category Name
		double largeText = 1.4;
		GlStateManager.scale(largeText, largeText, largeText);
		fontRenderer.drawStringWithShadow(
				I18n.format(ArcaneMagicAPI.getNotebookCategories().get(curCategory).getUnlocalizedName()),
				(int) ((screenX + 145) / largeText), (int) ((screenY + 17) / largeText), 0x666666); // satan is coming

		// Category List
		double categoryNameSize = 0.8;
		GlStateManager.scale((1 / largeText) * categoryNameSize, (1 / largeText) * categoryNameSize,
				(1 / largeText) * categoryNameSize);

		for (int category = 0; category < ArcaneMagicAPI.getCategoryCount(); category++)
		{
			// Draw the category!
			fontRenderer.drawString(
					I18n.format(ArcaneMagicAPI.getNotebookCategories().get(category).getUnlocalizedName()),
					(int) ((screenX + (category == curCategory ? 26 : 18)) * (1 / categoryNameSize)),
					(int) ((screenY + 24 + (category * 20)) * (1 / categoryNameSize)),
					category == curCategory ? 0x515151 : 0x32363d);
		}

		// Go back to default scaling
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();

		// Reset curY for the second round of drawing the entries
		curY = 0;

		// Draw all the entries a second time for tooltip rendering etc
		for (INotebookEntry entry : ArcaneMagicAPI.getNotebookCategories().get(curCategory).getEntries())
		{
			entry.drawPost(screenX + 145, screenY + 40 + curY, mouseX, mouseY, this);
			curY += entry.getHeight(this) + 5;
		}

		// Goodbye matrix!
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (mouseButton == 0)
		{
			// Things from drawScreen to calculate positions based on GUI Scale
			ScaledResolution res = new ScaledResolution(mc);
			final int SCALED_NOTEBOOK_HEIGHT = (int) (NOTEBOOK_HEIGHT * scale);
			int screenY = (res.getScaledHeight() / 2) - (SCALED_NOTEBOOK_HEIGHT / 2);

			for (int tab = 0; tab < ArcaneMagicAPI.getNotebookCategories().size(); tab++)
			{
				if (relMouseY >= screenY + (tab * 23) && relMouseY <= screenY + (tab * 20) + 32)
				{
					if (player.getEntityData().getInteger(tagCategory) != tab)
					{
						player.getEntityWorld().playSound(player.posX, player.posY, player.posZ,
								ArcaneMagicSoundHandler.randomCameraClackSound(), SoundCategory.MASTER, 1f, 1f, false);
						player.getEntityData().setInteger(tagCategory, tab);

					}
					break;
				}
			}
		}
	}
}
