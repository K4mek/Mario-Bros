package com.game.mariobros.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.game.mariobros.MarioBros;
import com.game.mariobros.screens.PlayerScreen;
import com.game.mariobros.sprites.enemies.Enemy;
import com.game.mariobros.sprites.enemies.Turtle;

import java.util.HashMap;

public class Mario extends Sprite {

    private interface Frame {
        void act();
    }
    public enum State {
        RUNNING, JUMPING, STANDING, FALLING, GROWING, DEAD
    }
    public State currentState;
    public State previousState;
    private boolean runningRight, marioIsBig, runGrowAnimation, timeToDefineBigMario, timeToRedefineMario, marioIsDead;
    private boolean marioOutWorld, toJump;
    private float stateTimer;
    private HashMap<State, Frame> action;

    public World world;
    public Body b2body;

    private TextureRegion marioJump, marioStand, bigMarioStand, bigMarioJump, marioDead, region;
    private Animation<TextureRegion> marioRun, bigMarioRun, growMario;

    public Mario(PlayerScreen screen) {
        this.world = screen.getWorld();

        currentState = previousState = State.STANDING;
        stateTimer = 0;
        runningRight = toJump = true;

        // Creating animation frames
        Array<TextureRegion> frames = new Array<TextureRegion>();

        // Set the mario run animation
        for(int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("little_mario"), i * 16, 0, 16, 16));
        }
        marioRun = new Animation(0.1f, frames);
        frames.clear();

        for(int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), i * 16, 0, 16, 32));
        }
        bigMarioRun = new Animation(0.1f, frames);

        // Get set animation frames from growing mario
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        growMario = new Animation<TextureRegion>(0.2f, frames);

        // Set the mario jump animation
        marioJump = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 80, 0, 16, 16);
        bigMarioJump = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 80, 0, 16, 32);

        // Creating the mario stand animation
        marioStand = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 0, 0, 16, 16);
        bigMarioStand = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32);

        // Creating mario dead sprite
        marioDead = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 96, 0, 16, 16);

        // Define all properties of the Mario
        definedMario();

        setSize(16 / MarioBros.PPM, 16 / MarioBros.PPM);
        setRegion(marioStand);

        defineActions();
    }

    private void defineActions() {
        //region = new TextureRegion();
        action = new HashMap<State, Frame>();
        action.put(State.DEAD, () -> {this.region= marioDead;});
        action.put(State.JUMPING, () -> {this.region= marioIsBig ? bigMarioJump : marioJump;});
        action.put(State.GROWING, () -> {
            this.region= growMario.getKeyFrame(stateTimer);
            if(growMario.isAnimationFinished(stateTimer)) {
                runGrowAnimation = false;
            }
        });
        action.put(State.RUNNING, () -> {
            this.region= marioIsBig ?
                bigMarioRun.getKeyFrame(stateTimer, true) :
                marioRun.getKeyFrame(stateTimer, true);
        });
        action.put(State.STANDING, () -> {this.region = marioIsBig ? bigMarioStand : marioStand;} );
        action.put(State.FALLING, () -> {this.region = marioIsBig ? bigMarioStand : marioStand;} );

    }

    public void update(float dt) {
        if(marioIsBig) {
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2 - 6 / MarioBros.PPM);
        }
        else {
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
        }

        setRegion(getFrame(dt));

        if(timeToDefineBigMario) { defineBigMario(); }

        if(timeToRedefineMario) { redefineMario(); }

        // Check out whether Mario fall
        if(this.getY() < -.5f && !marioIsDead) {
            marioOutWorld = true;
            hit(null);
        }
    }

    public void hit(Enemy enemy) {
        if(enemy instanceof Turtle && ((Turtle)enemy).getCurrentState() == Turtle.State.STANDING_SHELL) {
            ((Turtle) enemy).kick(this.getX() <= enemy.getX() ? Turtle.KICK_LEFT_SPEED : Turtle.KICK_RIGHT_SPEED);
        }
        else {
            if (!marioOutWorld && marioIsBig) {
                marioIsBig = false;
                timeToRedefineMario = true;
                setBounds(getX(), getY(), getWidth(), getHeight() / 2);
                MarioBros.manager.get("audio/sounds/powerdown.wav", Sound.class).play();
            } else {
                //MarioBros.manager.get("audio/music/mario_music.ogg", Sound.class).stop();
                MarioBros.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
                marioIsDead = true;
                Filter localFiter = new Filter();
                localFiter.categoryBits = MarioBros.NOTHING_BIT;
                for (Fixture fixture : b2body.getFixtureList()) {
                    fixture.setFilterData(localFiter);
                }
                b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
            }
        }
    }

    public boolean isBig() {
        return marioIsBig;
    }

    public TextureRegion getFrame(float dt) {
        currentState = getState();

        /*
        TextureRegion region;
        switch(currentState) {
            case DEAD:
                region = marioDead;
                break;
            case GROWING:
                region = growMario.getKeyFrame(stateTimer);
                if(growMario.isAnimationFinished(stateTimer)) {
                    runGrowAnimation = false;
                }
                break;
            case JUMPING:
                region = marioIsBig ? bigMarioJump : marioJump;
                break;
            case RUNNING:
                region = marioIsBig ?
                        bigMarioRun.getKeyFrame(stateTimer, true) :
                        marioRun.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = marioIsBig ? bigMarioStand : marioStand;
                break;
        }
        */
        action.get(currentState).act();

        if((b2body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, runningRight = false);
        }
        else if((b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(runningRight = true,false);
        }

        stateTimer = (currentState == previousState) ? stateTimer + dt : 0;
        previousState = currentState;
        return region;

    }

    public void grow() {
        if(!isBig()) {
            runGrowAnimation = marioIsBig = timeToDefineBigMario = true;
            setBounds(getX(), getY(), getWidth(), getHeight() * 2);
            MarioBros.manager.get("audio/sounds/powerup.wav", Sound.class).play();
        }
    }

    public State getState() {
        if(marioIsDead) {
            return State.DEAD;
        }
        else if(runGrowAnimation) {
            return State.GROWING;
        }
        else if(b2body.getLinearVelocity().y > 0 || (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING)) {
            return State.JUMPING;
        }
        else if(b2body.getLinearVelocity().y < 0) {
            return State.FALLING;
        }
        else if(b2body.getLinearVelocity().x != 0) {
            return State.RUNNING;
        }
        else { return State.STANDING; }
    }

    public boolean isDead() {
        return marioIsDead;
    }

    public float getStateTimer() {
        return stateTimer;
    }

    public void setToJump(boolean value) {
        toJump = value;
    }

    public boolean getToJump() {
        return toJump;
    }

    public void definedMario() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(32 / MarioBros.PPM, 32 / MarioBros.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;

        b2body.createFixture(fdef).setUserData(this);
    }

    public void redefineMario() {
        Vector2 position = b2body.getPosition();
        world.destroyBody(b2body);
        BodyDef bdef = new BodyDef();
        bdef.position.set(position);
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;

        b2body.createFixture(fdef).setUserData(this);

        timeToRedefineMario = false;
    }

    public void defineBigMario() {
        Vector2 currentPosition = b2body.getPosition();
        world.destroyBody(b2body);

        BodyDef bdef = new BodyDef();
        bdef.position.set(currentPosition.add(0f, 10 / MarioBros.PPM));
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);
        shape.setPosition(new Vector2(0, -14 / MarioBros.PPM));
        b2body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;

        b2body.createFixture(fdef).setUserData(this);

        timeToDefineBigMario = false;
    }
}
