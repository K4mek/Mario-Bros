package com.game.mariobros.sprites.items;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.game.mariobros.MarioBros;
import com.game.mariobros.screens.PlayerScreen;
import com.game.mariobros.sprites.Mario;

public abstract class Item extends Sprite {

    PlayerScreen screen;
    World world;
    Vector2 velocity;
    boolean toDestroy, destroyed;
    Body body;

    Item(PlayerScreen screen, float x, float y) {
        this.screen = screen;
        this.world = screen.getWorld();
        setPosition(x, y);
        setBounds(getX(), getY(), 16 / MarioBros.PPM, 16 / MarioBros.PPM);

        defineItem();

        toDestroy = destroyed = false;
    }

    public void update(float deltaTime) {
        if(toDestroy && !destroyed) {
            world.destroyBody(body);
            destroyed = true;
        }
    }

    abstract void defineItem();

    public abstract void use(Mario mario);

    @Override
    public void draw(Batch batch) {
        if(!destroyed) {
            super.draw(batch);
        }
    }

    void destroy() {
        toDestroy = true;
    }

    public void reverseVelocity(boolean x, boolean y) {
        velocity.x = (x) ? -velocity.x : velocity.x;
        velocity.y = (y) ? -velocity.y : velocity.y;
    }

}