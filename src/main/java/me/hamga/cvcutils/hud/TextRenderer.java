package me.hamga.cvcutils.hud;

import me.hamga.cvcutils.util.TextColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayDeque;

public class TextRenderer {
    private final FontRenderer renderer =  Minecraft.getMinecraft().fontRendererObj;
    private final ArrayDeque<Runnable> renderQueue = new ArrayDeque<>();

    private String text;
    private float x = Float.MIN_VALUE;
    private float y = Float.MIN_VALUE;
    private int color = TextColor.white;

    private boolean textIsSet = false;
    private boolean positionIsSet = false;

    private float textHeight = renderer.FONT_HEIGHT;
    private float textWidth;

    public TextRenderer(String text, float x, float y, int color) {
        textAddFirst(text);
        positionAddFirst(x, y);
        color(color);
    }

    public TextRenderer(String text, float x, float y) {
        textAddFirst(text);
        positionAddFirst(x, y);
    }

    public TextRenderer(float x, float y) {
        positionAddFirst(x, y);
    }

    public TextRenderer(){

    }

    public void render(String text, float x, float y, int color){
        textAddFirst(text);
        positionAddFirst(x, y);
        color(color);

        GlStateManager.pushMatrix();

        while(!this.renderQueue.isEmpty()) {
            this.renderQueue.poll().run();
        }

        System.out.println(this);

        renderer.drawStringWithShadow(this.text, this.x, this.y, this.color);

        GlStateManager.popMatrix();

        this.text = null;
        this.x = Float.MIN_VALUE;
        this.y = Float.MIN_VALUE;
        this.color = TextColor.white;
        this.textIsSet = false;
        this.positionIsSet = false;
    }

    public void render(String text, float x, float y){
        render(text, x, y, this.color);
    }

    public void render(String text) {
        render(text, this.x, this.y, this.color);
    }

    public void render() {
        render(this.text, this.x, this.y, this.color);
    }

    public TextRenderer text(String text){
        this.textIsSet = true;

        this.renderQueue.add(() -> {
            this.text = text;

            this.textWidth = renderer.getStringWidth(this.text);
        });

        return this;
    }

    private void textAddFirst(String text){
        this.textIsSet = true;

        this.renderQueue.addFirst(() -> {
            this.text = text;

            this.textWidth = renderer.getStringWidth(this.text);
        });

    }

    public TextRenderer position(float x, float y){
        this.positionIsSet = true;

        this.renderQueue.add(() -> {
            this.x = x;
            this.y = y;

            this.renderQueue.add(() -> GlStateManager.translate(x, y, 0f));
        });

        return this;
    }

    private void positionAddFirst(float x, float y){
        this.positionIsSet = true;

        this.renderQueue.addFirst(() -> {
            this.x = x;
            this.y = y;

            this.renderQueue.add(() -> GlStateManager.translate(x, y, 0f));
        });

    }

    public TextRenderer center() {
        verifyText("center");

        this.renderQueue.add(() -> {
            float oldX = this.x;
            this.x += this.textWidth / 2;

            this.renderQueue.add(() -> GlStateManager.translate(oldX - this.x, 0, 0f));
        });

        return this;
    }

    public TextRenderer color(int color) {
        this.renderQueue.add(() -> this.color = color);

        return this;
    }

    public TextRenderer scale(float scaleX, float scaleY){
        verifyText("scale");

        this.textHeight *= scaleY;
        this.textWidth *= scaleX;

        this.renderQueue.add(() -> GlStateManager.scale(scaleX, scaleY, 1f));

        return this;
    }

    public TextRenderer scaleCentered(float scaleX, float scaleY){
        verifyText("scale");

        this.textHeight *= scaleY;
        this.textWidth *= scaleX;

        this.renderQueue.add(() -> {
            this.renderQueue.add(() -> GlStateManager.translate(textWidth / 2f, textHeight / 2f, 0f));
            this.renderQueue.add(() -> GlStateManager.scale(scaleX, scaleY, 1f));
            this.renderQueue.add(() -> GlStateManager.translate(-textWidth / 2f, -textHeight / 2f, 0f));
        });

        return this;
    }

    public TextRenderer scale(float scale){
        return scale(scale, scale);
    }

    public TextRenderer rotate(float degrees, float x, float y, boolean xAxis, boolean yAxis, boolean zAxis){
        float xMult = (xAxis) ? 1f : 0f;
        float yMult = (yAxis) ? 1f : 0f;
        float zMult = (zAxis) ? 1f : 0f;

        GlStateManager.translate(x, y, 0f);   // move origin to text position
        GlStateManager.rotate(degrees, xMult, yMult, zMult);

        return this;
    }

    public TextRenderer rotate(float degrees){
        return rotate(degrees, this.x, this.y, false, false, true);
    }

    public TextRenderer rotate(float degrees, float x, float y){
        return rotate(degrees, x, y, false, false, true);
    }

    private void verifyText(String context){
        if (!this.textIsSet) {
            throw new IllegalStateException("Text must be set before calling " + context + "()");
        }
    }

    private void verifyPosition(String context){
        if (!this.positionIsSet) {
            throw new IllegalStateException("Position must be set before calling " + context + "()");
        }
    }

    @Override
    public String toString(){
        return "TextRenderer{" +
                "renderQueueLength=" + renderQueue.size() +
                ", text='" + text + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", color=" + color +
                ", textIsSet=" + textIsSet +
                ", positionIsSet=" + positionIsSet +
                ", textHeight=" + textHeight +
                ", textWidth=" + textWidth +
                '}';
    }
}
