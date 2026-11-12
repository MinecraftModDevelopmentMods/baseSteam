package zone.moddev.mc.basesteam.gui;

import cyano.poweradvantage.api.simple.SimpleMachineGUI.GUIContainer;
import zone.moddev.mc.basepower.gui.FluidTankGUI;
import org.joml.Matrix3x2fStack;
import net.minecraft.resources.Identifier;
import cyano.poweradvantage.api.fluid.FluidStack;

public abstract class GUIHelper {


	public static float maxDelta(float newValue,float oldValue,float maxChange){
		if((newValue - oldValue) > maxChange){
			return oldValue + maxChange;
		}
		if((oldValue - newValue) > maxChange){
			return oldValue - maxChange;
		}
		return newValue;
	}
	
	public static void drawNeedle(GUIContainer container, float pivotX, float pivotY, float zLevel, float value) {
		Matrix3x2fStack pose = container.getMatrices();
		pose.pushMatrix();
		try {
			positionNeedle(pose, pivotX, pivotY, value);
			container.blit(0, 0, 176, 0, 7, 35);
		} finally {
			pose.popMatrix();
		}
	}

	static void positionNeedle(Matrix3x2fStack pose, float pivotX, float pivotY, float value) {
		pose.translate(pivotX, pivotY);
		pose.rotate((float) Math.toRadians(needleRotation(value)));
		pose.translate(-3.5F, -17.5F);
	}

	private static float needleRotation(float value) {
		return (displayLevel(value) - 0.5F) * 180.0F;
	}

	/** @deprecated Use the screen-aware overload to draw with the machine texture. */
	@Deprecated
	public static void drawNeedle(float pivotX, float pivotY, float zLevel,
			float value) {
		drawNeedle(drawingContainer(net.minecraft.client.Minecraft.getInstance().gui.screen()), pivotX, pivotY, zLevel, value);
	}

	static GUIContainer drawingContainer(net.minecraft.client.gui.screens.Screen screen) {
		if (screen instanceof GUIContainer container && container.getGraphics() != null) return container;
		throw new IllegalStateException("A needle requires an active machine decoration callback");
	}
	
	public static void drawFlameProgress(int x, int y, float flameHeight, GUIContainer gc){
		flameHeight = displayLevel(flameHeight);
		if(flameHeight <= 0) return;
		final int flameTexX = 200;
		final int flameTexY = 0;
		final int width = 14;
		final int height = 15;
		int h = (int)(flameHeight*height);
		if(h == 0 ) h = 1;
		if(h > 0){
			gc.blit(x, y+h, flameTexX, flameTexY+h, width, height - h);
		}
	}
	
	public static void drawFluidBar(FluidStack fs, float barHeight, int xPos, int yPos, 
			GUIContainer guiContainer, int x, int y, float z,
			Identifier displayImage, int texOverlayX, int texOverlayY, int texOverlayW, int texOverlayH){
		barHeight = displayLevel(barHeight);
		final int texMarginW = (texOverlayW - 16) / 2;
		final int texMarginH = (texOverlayH - 60) / 2;
		final int w = 16;
		final int barSlotHeight = 60;
		final int h = (int)(barSlotHeight * barHeight);
		if(barHeight > 0){
			FluidTankGUI.drawFluidFilledRectangle(guiContainer,fs,x+xPos, y+yPos+barSlotHeight-h,w, h,z);
		}
		guiContainer.blit(displayImage, x+xPos-texMarginW, y+yPos-texMarginH, texOverlayX, texOverlayY, texOverlayW, texOverlayH); // x, y, textureOffsetX, textureOffsetY, width, height)
	}
	


	public static void drawDownArrowProgress(int x, int y, float progress,
			GUIContainer gc) {
		progress = displayLevel(progress);
		if(progress <= 0) return;
		final int arrowTexX = 184;
		final int arrowTexY = 0;
		final int width = 16;
		final int height = 32;
		int h = (int)(progress*height);
		if(h == 0 ) h = 1;
		if(h > 0){
			gc.blit(x, y, arrowTexX, arrowTexY, width, h);
		}
	}

	static float displayLevel(float value) {
		return Float.isFinite(value) ? Math.max(0F, Math.min(1F, value)) : 0F;
	}
}
