package com.game.mariobros.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.game.mariobros.screens.PlayerScreen;
import com.game.mariobros.sprites.Mario;

public abstract class Enemy extends Sprite {

    World world;
    PlayerScreen screen;
    public Body b2body;
    Vector2 velocity;
    float x, y;

    Enemy(PlayerScreen screen, float x, float y) {
        this.world = screen.getWorld();
        this.screen = screen;
        this.x = x;
        this.y = y;

        velocity = new Vector2(-1, 0);

        defineEnemy();

        b2body.setActive(false);
    }

    abstract void defineEnemy();

    public abstract void update(float dt);

    public abstract void hitOnHead(Mario mario);

    public abstract void onEnemyhit(Enemy enemy);

    public void reverseVelocity(boolean x, boolean y) {
        velocity.x = (x) ? -velocity.x : velocity.x;
        velocity.y = (y) ? -velocity.y : velocity.y;
    }
}
