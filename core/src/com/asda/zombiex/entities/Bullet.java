package com.asda.zombiex.entities;

import com.asda.zombiex.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * @author Skala
 */
public class Bullet extends B2DSprite {
    private boolean remove = false;

    public Bullet(Body body) {
        super(body);

        Texture tex = Game.res.getTexture("bullet");
        TextureRegion sprite = new TextureRegion(tex, 0, 0, 8, 8);

        setAnimation(sprite, 1 / 12f);
    }

    public boolean shouldRemove() {
        return remove;
    }
}
