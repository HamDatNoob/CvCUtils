package me.hamga.cvcutils.hud;

import me.hamga.cvcutils.util.TextColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayDeque;

/**
 * A deferred text rendering utility for Minecraft HUD rendering.
 *
 * <p>This class builds a queue of rendering operations (text, transforms, color, etc.)
 * which are executed in order when {@link #render()} is called. Internally, it uses
 * OpenGL matrix transformations via {@link GlStateManager}.
 *
 * <p>All methods are chainable and enqueue operations rather than executing immediately.
 * The final rendered position and appearance depend entirely on the order in which
 * methods are called.
 *
 * <h3>Important Behavior</h3>
 * <ul>
 *     <li>Rendering is <b>order-dependent</b>. Operations are applied in the exact order they are chained.</li>
 *     <li>Transforms (translate, scale, rotate) behave like OpenGL: earlier calls affect later ones.</li>
 *     <li>State such as position and text size is resolved at render time, not when methods are called.</li>
 *     <li>Color state can be modified incrementally (e.g., {@link #color(int)} then {@link #alpha(float)}).</li>
 * </ul>
 *
 * <h3>Basic Example</h3>
 * <pre>
 * new TextRenderer()
 *     .text("Hello")
 *     .translate(100, 50)
 *     .center()
 *     .scale(2)
 *     .render();
 * </pre>
 *
 * <h3>Ordering Rules</h3>
 * <ul>
 *     <li>{@link #text(String)} must be called before methods that depend on text size
 *         (e.g., {@link #center()}, {@link #scale(float, float)}, {@link #scaleCentered(float, float)}).</li>
 *
 *     <li>{@link #translate(float, float)} must be called before {@link #rotate(float)}
 *         if using implicit rotation pivot.</li>
 *
 *     <li>Transform order matters:
 *         <pre>
 *         translate → rotate ≠ rotate → translate
 *         </pre>
 *     </li>
 *
 *     <li>Multiple {@code translate()} calls accumulate.</li>
 *
 *     <li>{@link #rotate(float)} uses the current translated position as its pivot.
 *         Use {@link #rotate(float, float, float)} for an explicit pivot.</li>
 *
 *     <li>Calling methods in the wrong order may not throw errors but can produce
 *         unexpected visual results.</li>
 * </ul>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>After {@link #render()} is called, all internal state and queued operations are reset.</li>
 *     <li>Each instance is intended for single-use per render call.</li>
 * </ul>
 */

public class TextRenderer {
    private final FontRenderer renderer =  Minecraft.getMinecraft().fontRendererObj;
    private final ArrayDeque<Runnable> renderQueue = new ArrayDeque<>();

    private String text;
    private float x = Float.NaN;
    private float y = Float.NaN;
    private int color = TextColor.white;
    private boolean shadow = true;

    private boolean textIsSet = false;
    private boolean positionIsSet = false;

    private float textHeight = renderer.FONT_HEIGHT;
    private float textWidth = 0;

    /**
     * Creates a new TextRenderer instance.
     *
     * <p>Rendering behavior is defined by chaining method calls (text, translate,
     * scale, etc.) before calling {@link #render()}.
     *
     * <p>No text or position is set by default.
     */
    public TextRenderer(){

    }

    /**
     * Executes all queued rendering operations and draws the text.
     *
     * <p>This method:
     * <ul>
     *     <li>Applies all queued transformations in order</li>
     *     <li>Renders the text (with or without shadow depending on configuration) at the transformed origin</li>
     *     <li>Resets all internal state after execution</li>
     * </ul>
     *
     * <p>This instance should not be reused without reconfiguration after calling this method.
     */
    public void render() {
        GlStateManager.pushMatrix();

        while(!this.renderQueue.isEmpty()) {
            this.renderQueue.poll().run();
        }

        if(shadow){
            renderer.drawStringWithShadow(this.text, 0, 0, this.color);
        }else{
            renderer.drawString(this.text, 0, 0, this.color);
        }

        GlStateManager.popMatrix();

        this.text = null;
        this.x = Float.NaN;
        this.y = Float.NaN;
        this.color = TextColor.white;
        this.shadow = true;
        this.textIsSet = false;
        this.positionIsSet = false;
        this.textHeight = renderer.FONT_HEIGHT;
        this.textWidth = 0;
    }

    /**
     * Sets the text to render.
     *
     * <p>This must be called before any operations that depend on text dimensions
     * such as centering or scaling.
     *
     * @param text the string to render
     * @return this renderer for chaining
     */
    public TextRenderer text(String text){
        this.textIsSet = true;

        this.renderQueue.add(() -> {
            this.text = text;

            this.textWidth = renderer.getStringWidth(this.text);
        });

        return this;
    }

    /**
     * Applies a translation (position offset).
     *
     * <p>Translations are cumulative and applied in order.
     *
     * <p>This also defines the current position used as the pivot for
     * {@link #rotate(float)} if no explicit pivot is provided.
     *
     * @param x horizontal offset
     * @param y vertical offset
     * @return this renderer for chaining
     */
    public TextRenderer translate(float x, float y){
        this.positionIsSet = true;

        this.renderQueue.add(() -> {
            this.x = (Float.isNaN(this.x)) ? x : this.x + x;
            this.y = (Float.isNaN(this.y)) ? y : this.y + y;

            GlStateManager.translate(x, y, 0f);
        });

        return this;
    }

    /**
     * Centers the text horizontally around the current position.
     *
     * <p>Requires that {@link #text(String)} has been called beforehand.
     *
     * @return this renderer for chaining
     * @throws IllegalStateException if text is not set
     */
    public TextRenderer center() {
        verifyText("center");

        this.renderQueue.add(() -> GlStateManager.translate(-this.textWidth / 2, 0, 0f));

        return this;
    }

    /**
     * Sets the text color.
     *
     * <p>If not specified, defaults to {@link TextColor#white}.
     *
     * <p>The alpha channel can be modified separately using {@link #alpha(float)}.
     *
     * <p>If called multiple times, the last value applied in the chain will be used.
     *
     * @param color ARGB color value
     * @return this renderer for chaining
     */
    public TextRenderer color(int color) {
        this.renderQueue.add(() -> this.color = color);

        return this;
    }

    /**
     * Sets the alpha (transparency) of the text color.
     *
     * <p>This modifies only the alpha channel of the current color, preserving RGB values.
     *
     * <p>The alpha value is expected to be in the range {@code [0.0, 1.0]}:
     * <ul>
     *     <li>{@code 0.0} = fully transparent</li>
     *     <li>{@code 1.0} = fully opaque</li>
     * </ul>
     *
     * <p>If called multiple times, the last value applied in the chain will be used.
     *
     * @param alpha transparency value between 0.0 and 1.0
     * @return this renderer for chaining
     */
    public TextRenderer alpha(float alpha) {
        this.renderQueue.add(() -> {
            int a = (int)(alpha * 255) << 24;
            this.color = (this.color & 0x00FFFFFF) | a;
        });

        return this;
    }

    /**
     * Enables or disables text shadow rendering.
     *
     * <p>If enabled, text will be rendered using
     * {@link net.minecraft.client.gui.FontRenderer#drawStringWithShadow(String, float, float, int)}.
     * If disabled, {@code drawString} will be used instead.
     *
     * <p>Shadow is enabled by default.
     *
     * <p>If called multiple times, the last value applied in the chain will be used.
     *
     * @param shadow true to enable shadow, false to disable
     * @return this renderer for chaining
     */
    public TextRenderer shadow(boolean shadow) {
        this.renderQueue.add(() -> this.shadow = shadow);

        return this;
    }

    /**
     * Scales the text.
     *
     * <p>Scaling is applied relative to the current origin.
     * Use {@link #scaleCentered(float, float)} to scale around the text center.
     *
     * <p>Requires that {@link #text(String)} has been called beforehand.
     *
     * @param scaleX horizontal scale
     * @param scaleY vertical scale
     * @return this renderer for chaining
     * @throws IllegalStateException if text is not set
     */
    public TextRenderer scale(float scaleX, float scaleY){
        verifyText("scale");

        this.renderQueue.add(() -> {
            GlStateManager.scale(scaleX, scaleY, 1f);

            this.textHeight *= scaleY;
            this.textWidth *= scaleX;
        });

        return this;
    }

    /**
     * Uniformly scales the text.
     *
     * <p>Equivalent to:
     * <pre>
     * scale(scale, scale)
     * </pre>
     *
     * <p>This scales both width and height equally relative to the current origin.
     *
     * <p><b>Requires:</b> {@link #text(String)} must be called beforehand.
     *
     * @param scale uniform scale factor
     * @return this renderer for chaining
     * @throws IllegalStateException if text is not set
     */
    public TextRenderer scale(float scale){
        return scale(scale, scale);
    }

    /**
     * Scales the text around its center.
     *
     * <p>This adjusts the transform so scaling occurs relative to the text midpoint
     * rather than the origin.
     *
     * <p>Requires that {@link #text(String)} has been called beforehand.
     *
     * @param scaleX horizontal scale
     * @param scaleY vertical scale
     * @return this renderer for chaining
     * @throws IllegalStateException if text is not set
     */
    public TextRenderer scaleCentered(float scaleX, float scaleY){
        verifyText("scale");

        this.renderQueue.add(() -> {
            GlStateManager.translate(textWidth / 2f, textHeight / 2f, 0f);
            GlStateManager.scale(scaleX, scaleY, 1f);
            GlStateManager.translate(-textWidth / 2f, -textHeight / 2f, 0f);

            this.textHeight *= scaleY;
            this.textWidth *= scaleX;
        });

        return this;
    }

    /**
     * Rotates the text.
     *
     * <p>If pivot coordinates are not provided (NaN), the current translated position
     * will be used as the pivot.
     *
     * <p>Requires that {@link #translate(float, float)} has been called beforehand
     * when using implicit pivot.
     *
     * @param degrees rotation angle in degrees
     * @param px pivot x (or NaN to use current position)
     * @param py pivot y (or NaN to use current position)
     * @param xAxis rotate around X axis
     * @param yAxis rotate around Y axis
     * @param zAxis rotate around Z axis
     * @return this renderer for chaining
     * @throws IllegalStateException if position is not set
     */
    public TextRenderer rotate(float degrees, float px, float py, boolean xAxis, boolean yAxis, boolean zAxis){
        verifyPosition("rotate");

        float xMult = (xAxis) ? 1f : 0f;
        float yMult = (yAxis) ? 1f : 0f;
        float zMult = (zAxis) ? 1f : 0f;

        this.renderQueue.add(() -> {
            float _px = (Float.isNaN(px)) ? this.x : px;
            float _py = (Float.isNaN(py)) ? this.y : py;

            GlStateManager.translate(_px, _py, 0f);   // move origin to text position
            GlStateManager.rotate(degrees, xMult, yMult, zMult);
            GlStateManager.translate(-_px, -_py, 0f);   // move origin to text position
        });

        return this;
    }

    /**
     * Rotates the text around the current position on the Z axis.
     *
     * <p>Equivalent to:
     * <pre>
     * rotate(degrees, Float.NaN, Float.NaN, false, false, true)
     * </pre>
     *
     * <p>The pivot is resolved at render time:
     * <ul>
     *     <li>If a position has been established via {@link #translate(float, float)},
     *         that position is used as the pivot.</li>
     *     <li>If no position is available, this will result in undefined behavior.</li>
     * </ul>
     *
     * <p><b>Requires:</b> {@link #translate(float, float)} must be called beforehand.
     *
     * @param degrees rotation angle in degrees
     * @return this renderer for chaining
     * @throws IllegalStateException if position is not set
     */
    public TextRenderer rotate(float degrees){
        return rotate(degrees, Float.NaN, Float.NaN, false, false, true);
    }

    /**
     * Rotates the text around a specified pivot point on the Z axis.
     *
     * <p>Equivalent to:
     * <pre>
     * rotate(degrees, px, py, false, false, true)
     * </pre>
     *
     * <p>This uses the provided pivot directly and does not depend on the current
     * translated position.
     *
     * @param degrees rotation angle in degrees
     * @param px pivot x-coordinate
     * @param py pivot y-coordinate
     * @return this renderer for chaining
     */
    public TextRenderer rotate(float degrees, float px, float py){
        return rotate(degrees, px, py, false, false, true);
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
