package io.github.moehreag.axolotlclient.modules.sky;

public class SkyLoadingScreen {
    /*private String currentPack = "";
    public static boolean currentlyShown = false;
    private String description="";
    private boolean loadingFinished = false;

    MinecraftClient client = MinecraftClient.getInstance();

    public void render() {
        if(!currentlyShown){
            currentlyShown=true;
        }
        Window window = new Window(MinecraftClient.getInstance());
        int i = window.getScaleFactor();
        Framebuffer framebuffer = new Framebuffer(window.getWidth() * i, window.getHeight() * i, true);
        framebuffer.bind(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, window.getWidth(), window.getHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translatef(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepthTest();

        DrawableHelper.fill(0, 0, MinecraftClient.getInstance().width, MinecraftClient.getInstance().height, AxolotlClient.CONFIG.loadingScreenColor.get().getAsInt());

        GlStateManager.enableTexture();

        MinecraftClient.getInstance().getTextureManager().bindTexture(AxolotlClient.badgeIcon);

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1F);
        DrawableHelper.drawTexture((window.getWidth()/2)-50,
                (window.getHeight()/2)-50,
                0, 0,
                100, 100,
                100, 100);

        if(!loadingFinished) {
            this.client.textRenderer.draw(I18n.translate("sky_loading_text") +
                            " "+Formatting.ITALIC +
                            currentPack+Formatting.RESET+Formatting.ITALIC +
                            (!Objects.equals(description, "")? " "+description:"") + Formatting.RESET+"...",
                    20, window.getHeight() - 20,
                    Color.getChroma().getAsInt());
        } else if(!AxolotlClient.initalized) {
            this.client.textRenderer.draw(Formatting.BOLD+ I18n.translate("resource_loading_finished"),
                    20, window.getHeight()-20, Color.getChroma().getAsInt());
        }
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        framebuffer.endWrite();
        framebuffer.draw(window.getWidth() * i, window.getHeight() * i);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1F);
        MinecraftClient.getInstance().updateDisplay();

    }

    public void update(ResourcePack pack){
        currentPack = pack.getName();
        Color.tickChroma();
        render();
    }

    public void setDesc(String desc){
        this.description="("+desc+")";
        //Color.tickChroma();
        render();
    }

    public void finish(){
        loadingFinished=true;
        Color.tickChroma();
        render();
        AxolotlClient.initalized=true;
    }*/

}
