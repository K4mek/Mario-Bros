package com.game.mariobros.sprites.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.game.mariobros.MarioBros;
import com.game.mariobros.screens.PlayerScreen;
import com.game.mariobros.sprites.Mario;

public class Goomba extends Enemy {

    private float stateTime;
    private Animation<TextureRegion> walkAnimation;
    private Array<TextureRegion> frames;
    private boolean setToDestroy, destroyed; // false

    public Goomba(PlayerScreen screen, float x, float y) {
        super(screen, x, y);
        setSize(16 / MarioBros.PPM, 16 / MarioBros.PPM);

        frames = new Array<TextureRegion>();
        for(int i = 0; i < 2; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("goomba"),i * 16, 0, 16, 16));
        }
        walkAnimation = new Animation<TextureRegion>(.3f, frames);
        stateTime = 0;

        setToDestroy = destroyed = false;
    }

    public void update(float dt) {
        stateTime += dt;
        if(setToDestroy && !destroyed) {
            world.destroyBody(b2body);
            destroyed = true;
            setRegion(new TextureRegion(screen.getAtlas().findRegion("goomba"), 32, 0, 16, 16));
            stateTime = 0f;
        }
        else if(!destroyed) {
            b2body.setLinearVelocity(velocity);
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
            setRegion(walkAnimation.getKeyFrame(stateTime, true));
        }
    }

    @Override
    void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(x, y);
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.ENEMY_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT | MarioBros.COIN_BIT
                | MarioBros.BRICK_BIT
                | MarioBros.ENEMY_BIT
                | MarioBros.OBJECT_BIT
                | MarioBros.MARIO_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        // Create the head here:
        PolygonShape head = new PolygonShape();
        Vector2[] vector2 = new Vector2[4];
        vector2[0] = new Vector2(-5, 8).scl(1 / MarioBros.PPM);
        vector2[1] = new Vector2(5, 8).scl(1 / MarioBros.PPM);
        vector2[2] = new Vector2(-3, 3).scl(1 / MarioBros.PPM);
        vector2[3] = new Vector2(3, 3).scl(1 / MarioBros.PPM);
        head.set(vector2);

        fdef.shape = head;
        fdef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        fdef.restitution = .5f;

        b2body.createFixture(fdef).setUserData(this);
    }

    @Override
    public void draw(Batch batch) {
        if(!destroyed || stateTime < 1) {
            super.draw(batch);
        }
    }

    @Override
    public void onEnemyhit(Enemy enemy) {
        if(enemy instanceof Turtle && ((Turtle) enemy).currentState == Turtle.State.MOVING_SHELL) {
            setToDestroy = true;
        }
        else {
            reverseVelocity(true, false);
        }
    }

    @Override
    public void hitOnHead(Mario mario) {
        setToDestroy = true;
        MarioBros.manager.get("audio/sounds/stomp.wav", Sound.class).play();
    }
}
