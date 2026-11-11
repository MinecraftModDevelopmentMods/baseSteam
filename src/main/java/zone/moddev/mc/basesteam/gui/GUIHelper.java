package zone.moddev.mc.basesteam.gui;

import cyano.poweradvantage.api.simple.SimpleMachineGUI.GUIContainer;
import zone.moddev.mc.basepower.gui.FluidTankGUI;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import cyano.poweradvantage.api.fluid.FluidStack;

@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
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
		PoseStack pose = container.getMatrices();
		pose.pushPose();
		try {
			positionNeedle(pose, pivotX, pivotY, zLevel, value);
			container.blit(0, 0, 176, 0, 7, 35);
		} finally {
			pose.popPose();
		}
	}

	static void positionNeedle(PoseStack pose, float pivotX, float pivotY, float zLevel, float value) {
		pose.translate(pivotX, pivotY, zLevel);
		pose.mulPose(Axis.ZP.rotationDegrees(needleRotation(value)));
		pose.translate(-3.5F, -17.5F, 0.0F);
	}

	private static float needleRotation(float value) {
		return (displayLevel(value) - 0.5F) * 180.0F;
	}

	/** @deprecated Use the screen-aware overload to draw with the machine texture. */
	@Deprecated
	public static void drawNeedle(float pivotX, float pivotY, float zLevel,
			float value) {
		value = displayLevel(value);
		

		final float pixelSize = 1.0f / 256; // conversion factor
		
		final float PI = 3.141592654f;
		final float needleX = 176 ;
		final float needleW = 7 ;
		final float needleY = 0 ;
		final float needleH = 35;
		final float needleL = needleH * 0.5f;
		final float needleR = needleW * 0.5f;
		
		float angle = (1f - value) * PI;
		float offsetX = pivotX;
		float offsetY = pivotY;

		float sin = Mth.sin(angle);
		float cos = Mth.cos(angle);

		float tipX =   cos * needleL + offsetX;
		float tipY =  -sin * needleL + offsetY;
		float baseX = -cos * needleL + offsetX;
		float baseY =  sin * needleL + offsetY;
		float dx = -sin * needleR;
		float dy = -cos * needleR;
		
		float[] x = new float[4];
		float[] y = new float[4];
		float[] u = new float[4];
		float[] v = new float[4];
		// top-left, bottom-left, bottom-right, top-right
		x[0] = tipX - dx;
		x[1] = baseX - dx;
		x[2] = baseX + dx;
		x[3] = tipX + dx;
		y[0] = tipY - dy;
		y[1] = baseY - dy;
		y[2] = baseY + dy;
		y[3] = tipY + dy;
		u[0] = pixelSize * needleX;
		u[1] = u[0];
		u[2] = u[0] + pixelSize * needleW;
		u[3] = u[2];
		v[0] = pixelSize * needleY;
		v[1] = v[0] + pixelSize * needleH;
		v[2] = v[1];		
		v[3] = v[0];
		
		com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
		BufferBuilder renderer = Tesselator.getInstance().begin(
				com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		// (x, y, z, u, v)
		for(int i = 3; i >= 0; i--){
			renderer.addVertex(x[i], y[i], zLevel).setUv(u[i],v[i]);
		}
		com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(renderer.buildOrThrow());
		
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
			ResourceLocation displayImage, int texOverlayX, int texOverlayY, int texOverlayW, int texOverlayH){
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
