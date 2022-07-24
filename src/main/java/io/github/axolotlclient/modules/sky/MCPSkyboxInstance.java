package io.github.axolotlclient.modules.sky;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class MCPSkyboxInstance extends SkyboxInstance {

    public MCPSkyboxInstance(JsonObject json){
        super(json);
        this.textures[0] = new Identifier(json.get("source").getAsString());
        this.fade[0] = json.get("startFadeIn").getAsInt();
        this.fade[1] = json.get("endFadeIn").getAsInt();
        this.fade[2] = json.get("startFadeOut").getAsInt();
        this.fade[3] = json.get("endFadeOut").getAsInt();
	    try {
			this.blendMode=parseBlend(json.get("blend").getAsString());
        } catch (Exception ignored){}
    }

    @Override
    public void renderSkybox(MatrixStack matrices) {
        this.alpha=getAlpha();

        RenderSystem.color4f(1,1,1,1);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        //RenderSystem.se(GameRenderer::getPositionTexShader);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        for (int i = 0; i < 6; ++i) {

            if(textures[0]!=null) {

                matrices.push();

                float u;
                float v;
				
                if(i==0){
                    u=0;
                    v=0;

                } else if (i == 1) {
					matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
                    u=1/3F;
                    v=0.5F;

                } else if (i == 2) {
	                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90));
	                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180));
                    u=2/3F;
                    v=0F;

                } else if (i == 3) {
	                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180));
                    u=1/3F;
                    v=0F;

                } else if (i == 4) {
	                matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90));
	                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90));
                    u=2/3F;
                    v=0.5F;

                } else {
	                matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(-90));
	                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
                    v=0.5F;
                    u=0;
                }

	            Matrix4f matrix4f = matrices.peek().getModel();
                bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
                bufferBuilder.vertex(matrix4f, -100, -100, -100).texture(u, v).color(1F, 1F, 1F, alpha).next();
                bufferBuilder.vertex(matrix4f, -100, -100, 100).texture(u, v+0.5F).color(1F, 1F, 1F, alpha).next();
                bufferBuilder.vertex(matrix4f, 100, -100, 100).texture(u+1/3F, v+0.5F).color(1F, 1F, 1F, alpha).next();
                bufferBuilder.vertex(matrix4f, 100, -100, -100).texture(u+1/3F, v).color(1F, 1F, 1F, alpha).next();

                bufferBuilder.end();
                BufferRenderer.draw(bufferBuilder);
                matrices.pop();
            }
        }
        RenderSystem.disableBlend();
    }
}
