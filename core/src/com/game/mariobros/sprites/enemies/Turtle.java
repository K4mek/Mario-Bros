package com.game.mariobros.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.game.mariobros.MarioBros;
import com.game.mariobros.screens.PlayerScreen;
import com.game.mariobros.sprites.Mario;

public class Turtle extends Enemy {

    public static final int KICK_LEFT_SPEED = -2;
    public static final int KICK_RIGHT_SPEED = 2;
    public enum State {
        WALKING, STANDING_SHELL, MOVING_SHELL, WAKEUP, DEAD
    }
    public State currentState;
    private long timeCounter;
    private float stateTime, deadRotationDegrees;
    private Animation<TextureRegion> walkAnimation, shellAnimation;
    private Array<TextureRegion> frames;
    private boolean setToDestroy, destroyed; // false
    private TextureRegion shell;

    public Turtle(PlayerScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<TextureRegion>();

        // Setting the turtle walking animation
        for(int i = 0; i < 4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), i * 16, 0, 16, 24));
        }
        walkAnimation = new Animation<TextureRegion>(.2f, frames);

        // Setting the turtle shell texture
        shell = new TextureRegion(screen.getAtlas().findRegion("turtle"), 64, 0, 16, 24);

        // Setting the turtle shell animation
        frames.clear();
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 80, 0, 16, 24));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 64, 0, 16, 24));
        shellAnimation = new Animation<TextureRegion>(.3f, frames);

        currentState = State.WALKING;
        setTime();

        setBounds(getX(), getY(), 16 / MarioBros.PPM, 24 / MarioBros.PPM);
    }

    private void setTime() {
        timeCounter = TimeUtils.millis();
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
        fdef.restitution = 1.5f;

        b2body.createFixture(fdef).setUserData(this);
    }

    public TextureRegion getFrame(float deltaTime) {
        TextureRegion region;

        switch(currentState) {
            case STANDING_SHELL:
            case MOVING_SHELL:
                region = shell;
                break;
            case WAKEUP:
                region = shellAnimation.getKeyFrame(stateTime, true);
                break;
            case WALKING:
            default:
                region = walkAnimation.getKeyFrame(stateTime, true);
                break;
        }

        if(velocity.x > 0 && !region.isFlipX()) {
            region.flip(true, false);
        }
        if(velocity.x < 0 && region.isFlipX()) {
            region.flip(true, false);
        }

        stateTime += deltaTime;
        return region;

    }

    @Override
    public void draw(Batch batch) {
        if(!destroyed) {
            super.draw(batch);
        }
    }

    @Override
    public void update(float dt) {
        setRegion(getFrame(dt));
        if(currentState == State.STANDING_SHELL && TimeUtils.millis() - timeCounter >= 3000) {
            currentState = State.WAKEUP;
        }
        if(currentState == State.WAKEUP && TimeUtils.millis() - timeCounter >= 5000) {
            currentState = State.WALKING;
            velocity.x = 1;
            setTime();
        }

        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - 8 / MarioBros.PPM);

        if(currentState == State.DEAD) {
            rotate(deadRotationDegrees += 3);
            if(!destroyed) {
                world.destroyBody(b2body);
                destroyed = true;
            }
        }
        else {
            b2body.setLinearVelocity(velocity);
        }
    }

    @Override
    public void hitOnHead(Mario mario) {
        if(currentState != State.STANDING_SHELL) {
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
            setTime();
        }
        else {
            kick((mario.getX() <= this.getX() ? KICK_RIGHT_SPEED : KICK_LEFT_SPEED));
        }
    }

    @Override
    public void onEnemyhit(Enemy enemy) {
        if(enemy instanceof Turtle) {
            if(((Turtle) enemy).getCurrentState() == State.MOVING_SHELL && currentState != State.MOVING_SHELL) {
                killed();
            }
            else if(currentState == State.MOVING_SHELL && ((Turtle) enemy).currentState == State.WALKING) {
                return;
            }
            else {
                reverseVelocity(true, false);
            }
        }
        else if(currentState != State.MOVING_SHELL) {
            reverseVelocity(true, false);
        }
    }

    @Override
    public void reverseVelocity(boolean x, boolean y) {
        super.reverseVelocity(x, y);
    }

    public void kick(int speed) {
        velocity.x = speed;
        currentState = State.MOVING_SHELL;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void killed() {
        currentState = State.DEAD;
        Filter filter = new Filter();
        filter.maskBits = MarioBros.NOTHING_BIT;

        for(Fixture f : b2body.getFixtureList()) {
            f.setFilterData(filter);
        }
        b2body.applyLinearImpulse(new Vector2(0, -2), b2body.getWorldCenter(), true);
    }
}
