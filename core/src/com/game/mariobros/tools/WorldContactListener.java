package com.game.mariobros.tools;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.game.mariobros.MarioBros;
import com.game.mariobros.sprites.Mario;
import com.game.mariobros.sprites.enemies.Enemy;
import com.game.mariobros.sprites.items.Item;
import com.game.mariobros.sprites.tileObjectes.InteractiveTileObject;

public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cdef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch(cdef) {
            case MarioBros.MARIO_HEAD_BIT | MarioBros.BRICK_BIT:
            case MarioBros.MARIO_HEAD_BIT | MarioBros.COIN_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.MARIO_HEAD_BIT) {
                    ((InteractiveTileObject)fixB.getUserData()).onHeadHit((Mario) fixA.getUserData());
                }
                else {
                    ((InteractiveTileObject)fixA.getUserData()).onHeadHit((Mario) fixB.getUserData());
                }
                break;
            case MarioBros.ENEMY_HEAD_BIT | MarioBros.MARIO_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ENEMY_HEAD_BIT) {
                    ((Enemy)fixA.getUserData()).hitOnHead((Mario) fixB.getUserData());
                }
                else {
                    ((Enemy)fixB.getUserData()).hitOnHead((Mario) fixA.getUserData());
                }
                break;

            case MarioBros.ENEMY_BIT | MarioBros.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ENEMY_BIT) {
                    ((Enemy)fixA.getUserData()).reverseVelocity(true, false);
                }
                else {
                    ((Enemy)fixB.getUserData()).reverseVelocity(true, false);
                }
                break;

            case MarioBros.MARIO_BIT | MarioBros.ENEMY_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.MARIO_BIT) {
                    //((Mario) fixA.getUserData()).hit();
                }
                else {
                    //((Mario) fixA.getUserData()).hit();
                }
                break;

            case MarioBros.ENEMY_BIT | MarioBros.ENEMY_BIT:
                ((Enemy)fixA.getUserData()).onEnemyhit((Enemy)fixB.getUserData());
                ((Enemy)fixB.getUserData()).onEnemyhit((Enemy)fixA.getUserData());
                break;

            case MarioBros.ITEM_BIT | MarioBros.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ITEM_BIT) {
                    ((Item)fixA.getUserData()).reverseVelocity(true, false);
                }
                else {
                    ((Item)fixB.getUserData()).reverseVelocity(true, false);
                }
                break;

            case MarioBros.ITEM_BIT | MarioBros.MARIO_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ITEM_BIT) {
                    ((Item)fixA.getUserData()).use((Mario) fixB.getUserData());
                }
                else {
                    ((Item)fixB.getUserData()).use((Mario) fixA.getUserData());
                }
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
