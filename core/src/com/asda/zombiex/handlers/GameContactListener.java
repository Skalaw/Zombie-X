package com.asda.zombiex.handlers;

import com.asda.zombiex.entities.Bullet;
import com.asda.zombiex.entities.Player;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;

/**
 * @author Skala
 */
public class GameContactListener implements ContactListener {
    private Array<Body> bodiesToRemove;
    private Array<Player> hitPlayer;

    public GameContactListener() {
        super();
        bodiesToRemove = new Array<Body>();
        hitPlayer = new Array<Player>();
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa == null || fb == null || fa.getUserData() == null || fb.getUserData() == null) {
            return;
        }

        if (fa.getUserData().equals("bullet") && fb.getUserData().equals("block")) {
            bodiesToRemove.add(fa.getBody());
        }

        if (fb.getUserData().equals("bullet") && fa.getUserData().equals("block")) {
            bodiesToRemove.add(fb.getBody());
        }

        if (fa.getUserData().equals("bullet") && fb.getUserData().equals("border")) {
            bodiesToRemove.add(fa.getBody());
        }

        if (fb.getUserData().equals("bullet") && fa.getUserData().equals("border")) {
            bodiesToRemove.add(fb.getBody());
        }

        if (fa.getUserData().equals("bullet") && fb.getUserData().equals("player")) {
            bodiesToRemove.add(fa.getBody());

            Player player = (Player) fb.getBody().getUserData();
            player.loseHealth(Bullet.POWER_BULLET);
            hitPlayer.add(player);
        }

        if (fb.getUserData().equals("bullet") && fa.getUserData().equals("player")) {
            bodiesToRemove.add(fb.getBody());

            Player player = (Player) fa.getBody().getUserData();
            player.loseHealth(Bullet.POWER_BULLET);
            hitPlayer.add(player);
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if (fa == null || fb == null) {
            return;
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public Array<Body> getBodiesToRemove() {
        return bodiesToRemove;
    }

    public Array<Player> getHitPlayer() {
        return hitPlayer;
    }
}
