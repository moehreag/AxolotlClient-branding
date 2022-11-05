package io.github.axolotlclient.modules.hud.util;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 * @license GPL-3.0
 */

@Data
@Accessors(fluent = true)
public class DrawPosition {

    public int x, y;

    public DrawPosition(int x, int y) {
        this.x=x;
        this.y=y;
    }

    public DrawPosition subtract(int x, int y) {
        return new DrawPosition(this.x - x, this.y - y);
    }

    public DrawPosition subtract(DrawPosition position) {
        return new DrawPosition(position.x, position.y);
    }

    public DrawPosition divide(float scale) {
        return new DrawPosition((int) (x / scale), (int) (y / scale));
    }

}
