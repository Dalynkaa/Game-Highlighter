package me.dalynkaa.highlighter.client.gui.widgets.colorPicker;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dalynkaa.highlighter.Highlighter;
import me.dalynkaa.highlighter.client.adapters.ColorAdapter;
import me.dalynkaa.highlighter.client.adapters.GuiAdapter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class OptimizedColorPickerWidget extends ClickableWidget {
    private static final int PICKER_SIZE = 200;
    private static final int HUE_BAR_WIDTH = 20;
    private static final int SPACING = 12;
    private static final int BORDER_SIZE = 2;

    private float hue = 0.0f;
    private float saturation = 1.0f;
    private float brightness = 1.0f;
    private int currentColor;

    private boolean draggingPicker = false;
    private boolean draggingHue = false;

    private Consumer<Integer> colorChangeCallback;

    // Texture optimization
    private NativeImageBackedTexture pickerTexture;
    private NativeImageBackedTexture hueTexture;
    private Identifier pickerTextureId;
    private Identifier hueTextureId;
    private boolean texturesNeedUpdate = true;

    public OptimizedColorPickerWidget(int x, int y, Consumer<Integer> colorChangeCallback) {
        super(x, y, PICKER_SIZE + HUE_BAR_WIDTH + SPACING, PICKER_SIZE, Text.empty());
        this.colorChangeCallback = colorChangeCallback;
        initializeTextures();
        updateColor();
    }

    private void initializeTextures() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Create picker texture
        NativeImage pickerImage = new NativeImage(PICKER_SIZE, PICKER_SIZE, false);
        pickerTexture = new NativeImageBackedTexture(pickerImage);
        //? if <=1.21.2 {
        pickerTextureId = client.getTextureManager().registerDynamicTexture("color_picker", pickerTexture);
        //?} else {
        /*pickerTextureId = Identifier.of(Highlighter.MOD_ID, "color_picker");
        client.getTextureManager().registerTexture(pickerTextureId, pickerTexture);
        *///?}

        // Create hue bar texture
        NativeImage hueImage = new NativeImage(HUE_BAR_WIDTH, PICKER_SIZE, false);
        hueTexture = new NativeImageBackedTexture(hueImage);
        //? if <=1.21.2 {
        hueTextureId = client.getTextureManager().registerDynamicTexture("hue_bar", hueTexture);
        //?} else {
        /*hueTextureId = Identifier.of(Highlighter.MOD_ID, "hue_bar");
        client.getTextureManager().registerTexture(hueTextureId, hueTexture);
        *///?}

        updateTextures();
    }

    private void updateTextures() {
        updatePickerTexture();
        updateHueTexture();
        texturesNeedUpdate = false;
    }

    private void updatePickerTexture() {
        NativeImage image = pickerTexture.getImage();

        for (int x = 0; x < PICKER_SIZE; x++) {
            for (int y = 0; y < PICKER_SIZE; y++) {
                float s = (float) x / (PICKER_SIZE - 1);
                float b = 1.0f - (float) y / (PICKER_SIZE - 1);

                int[] rgb = hsbToRgb(hue, s, b);
                int color = 0xFF000000 | (rgb[2] << 16) | (rgb[1] << 8) | rgb[0];

                //? if =1.21.1 {
                image.setColor(x, y, color);
                //?} else {
                /*image.setColorArgb(x, y, color);
                *///?}
            }
        }

        pickerTexture.upload();
    }

    private void updateHueTexture() {
        NativeImage image = hueTexture.getImage();

        for (int y = 0; y < PICKER_SIZE; y++) {
            float h = (float) y / (PICKER_SIZE - 1);
            int[] rgb = hsbToRgb(h, 1.0f, 1.0f);
            int color = 0xFF000000 | (rgb[2] << 16) | (rgb[1] << 8) | rgb[0];

            for (int x = 0; x < HUE_BAR_WIDTH; x++) {
                //? if =1.21.1 {
                image.setColor(x, y, color);
                 //?} else {
                /*image.setColorArgb(x, y, color);
                *///?}
            }
        }

        hueTexture.upload();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (texturesNeedUpdate) {
            updateTextures();
        }

        int x = getX();
        int y = getY();

        // Draw picker background with border
        context.fill(x - BORDER_SIZE, y - BORDER_SIZE,
                x + PICKER_SIZE + BORDER_SIZE, y + PICKER_SIZE + BORDER_SIZE,
                0xFF2A2A2A);

        // Draw main color picker using optimized texture
        RenderSystem.setShaderTexture(0, pickerTextureId);
        GuiAdapter.drawTexture(context,pickerTextureId, x, y, 0, 0, PICKER_SIZE, PICKER_SIZE, PICKER_SIZE, PICKER_SIZE);

        // Draw hue bar background with border
        int hueX = x + PICKER_SIZE + SPACING;
        context.fill(hueX - BORDER_SIZE, y - BORDER_SIZE,
                hueX + HUE_BAR_WIDTH + BORDER_SIZE, y + PICKER_SIZE + BORDER_SIZE,
                0xFF2A2A2A);

        // Draw hue bar using optimized texture
        RenderSystem.setShaderTexture(0, hueTextureId);
        GuiAdapter.drawTexture(context,hueTextureId, hueX, y, 0, 0, HUE_BAR_WIDTH, PICKER_SIZE, HUE_BAR_WIDTH, PICKER_SIZE);

        // Draw selection indicators
        drawSelectionIndicators(context);
    }

    private void drawSelectionIndicators(DrawContext context) {
        int x = getX();
        int y = getY();

        // Main picker crosshair
        int pickerX = x + Math.round(saturation * (PICKER_SIZE - 1));
        int pickerY = y + Math.round((1.0f - brightness) * (PICKER_SIZE - 1));

        // Outer white crosshair
        context.fill(pickerX - 5, pickerY - 1, pickerX + 6, pickerY + 2, 0xFFFFFFFF);
        context.fill(pickerX - 1, pickerY - 5, pickerX + 2, pickerY + 6, 0xFFFFFFFF);

        // Inner black crosshair
        context.fill(pickerX - 4, pickerY, pickerX + 5, pickerY + 1, 0xFF000000);
        context.fill(pickerX, pickerY - 4, pickerX + 1, pickerY + 5, 0xFF000000);

        // Hue bar selector
        int hueX = x + PICKER_SIZE + SPACING;
        int hueY = y + Math.round(hue * (PICKER_SIZE - 1));

        // Draw triangular selectors on both sides
        drawTriangleSelector(context, hueX - 4, hueY, true);
        drawTriangleSelector(context, hueX + HUE_BAR_WIDTH + 1, hueY, false);
    }

    private void drawTriangleSelector(DrawContext context, int x, int y, boolean pointingRight) {
        if (pointingRight) {
            // Right-pointing triangle
            for (int i = 0; i < 4; i++) {
                context.fill(x + i, y - i, x + i + 1, y + i + 1, 0xFFFFFFFF);
                if (i > 0) {
                    context.fill(x + i, y - i + 1, x + i, y + i, 0xFF000000);
                }
            }
        } else {
            // Left-pointing triangle
            for (int i = 0; i < 4; i++) {
                context.fill(x - i, y - i, x - i + 1, y + i + 1, 0xFFFFFFFF);
                if (i > 0) {
                    context.fill(x - i, y - i + 1, x - i, y + i, 0xFF000000);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY) || button != 0) {
            return false;
        }

        int relativeX = (int) (mouseX - getX());
        int relativeY = (int) (mouseY - getY());

        if (relativeX >= 0 && relativeX < PICKER_SIZE && relativeY >= 0 && relativeY < PICKER_SIZE) {
            // Clicked on main picker
            draggingPicker = true;
            updatePickerValues(relativeX, relativeY);
            return true;
        } else if (relativeX >= PICKER_SIZE + SPACING &&
                relativeX < PICKER_SIZE + SPACING + HUE_BAR_WIDTH &&
                relativeY >= 0 && relativeY < PICKER_SIZE) {
            // Clicked on hue bar
            draggingHue = true;
            updateHueValue(relativeY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != 0) {
            return false;
        }

        int relativeX = (int) (mouseX - getX());
        int relativeY = (int) (mouseY - getY());

        if (draggingPicker) {
            updatePickerValues(
                    MathHelper.clamp(relativeX, 0, PICKER_SIZE - 1),
                    MathHelper.clamp(relativeY, 0, PICKER_SIZE - 1)
            );
            return true;
        } else if (draggingHue) {
            updateHueValue(MathHelper.clamp(relativeY, 0, PICKER_SIZE - 1));
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingPicker = false;
            draggingHue = false;
            return true;
        }
        return false;
    }

    private void updatePickerValues(int x, int y) {
        saturation = (float) x / (PICKER_SIZE - 1);
        brightness = 1.0f - (float) y / (PICKER_SIZE - 1);
        updateColor();
    }

    private void updateHueValue(int y) {
        float oldHue = hue;
        hue = (float) y / (PICKER_SIZE - 1);

        // Only update picker texture if hue changed significantly
        if (Math.abs(oldHue - hue) > 0.01f) {
            texturesNeedUpdate = true;
        }

        updateColor();
    }

    private void updateColor() {
        int[] rgb = hsbToRgb(hue, saturation, brightness);
        currentColor = ColorAdapter.getArgb(255, rgb[0], rgb[1], rgb[2]);

        if (colorChangeCallback != null) {
            colorChangeCallback.accept(currentColor);
        }
    }

    public void setColor(int color) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;

        float[] hsb = rgbToHsb(r, g, b);
        float oldHue = this.hue;

        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.currentColor = color;

        // Update textures if hue changed
        if (Math.abs(oldHue - hue) > 0.01f) {
            texturesNeedUpdate = true;
        }
    }

    public int getColor() {
        return currentColor;
    }

    public void cleanup() {
        if (pickerTexture != null) {
            pickerTexture.close();
        }
        if (hueTexture != null) {
            hueTexture.close();
        }
    }

    // HSB/RGB conversion utilities
    private static int[] hsbToRgb(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;

        if (saturation == 0) {
            r = g = b = Math.round(brightness * 255.0f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));

            switch ((int) h) {
                case 0:
                    r = Math.round(brightness * 255.0f);
                    g = Math.round(t * 255.0f);
                    b = Math.round(p * 255.0f);
                    break;
                case 1:
                    r = Math.round(q * 255.0f);
                    g = Math.round(brightness * 255.0f);
                    b = Math.round(p * 255.0f);
                    break;
                case 2:
                    r = Math.round(p * 255.0f);
                    g = Math.round(brightness * 255.0f);
                    b = Math.round(t * 255.0f);
                    break;
                case 3:
                    r = Math.round(p * 255.0f);
                    g = Math.round(q * 255.0f);
                    b = Math.round(brightness * 255.0f);
                    break;
                case 4:
                    r = Math.round(t * 255.0f);
                    g = Math.round(p * 255.0f);
                    b = Math.round(brightness * 255.0f);
                    break;
                case 5:
                    r = Math.round(brightness * 255.0f);
                    g = Math.round(p * 255.0f);
                    b = Math.round(q * 255.0f);
                    break;
            }
        }

        return new int[]{
                MathHelper.clamp(r, 0, 255),
                MathHelper.clamp(g, 0, 255),
                MathHelper.clamp(b, 0, 255)
        };
    }

    private static float[] rgbToHsb(int r, int g, int b) {
        float[] hsbvals = new float[3];
        int cmax = Math.max(r, Math.max(g, b));
        int cmin = Math.min(r, Math.min(g, b));

        float brightness = ((float) cmax) / 255.0f;
        float saturation = (cmax != 0) ? ((float) (cmax - cmin)) / ((float) cmax) : 0;
        float hue = 0;

        if (saturation != 0) {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));

            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;

            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }

        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}