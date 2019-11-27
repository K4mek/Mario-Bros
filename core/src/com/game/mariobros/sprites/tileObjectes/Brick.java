package com.game.mariobros.sprites.tileObjectes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.game.mariobros.MarioBros;
import com.game.mariobros.scenes.Hud;
import com.game.mariobros.screens.PlayerScreen;
import com.game.mariobros.sprites.Mario;

public class Brick extends InteractiveTileObject {

    private Hud hud;

    private enum BrickType {
        BROWN_BRICK, BLUE_BRICK, GREEN_BRICK, BLANCK_BRICK;
    }
    private BrickType currentBrick = BrickType.BROWN_BRICK;

    private TiledMapTileSet tileSet;

    public Brick(PlayerScreen screen, MapObject object, Hud hud) {
        super(screen, object);

        tileSet = map.getTileSets().getTileSet("tileset_gutter");

        fixture.setUserData(this);
        setCategoryFilter(MarioBros.BRICK_BIT);

        this.hud = hud;
    }

    @Override
    public void onHeadHit(Mario mario) {
        Gdx.app.log("brick", "collision");

        switch(currentBrick) {
            case BROWN_BRICK:
                setBrick(200);
                currentBrick = BrickType.GREEN_BRICK;
                MarioBros.manager.get("audio/sounds/bump.wav", Sound.class).play();
                break;
            case GREEN_BRICK:
                setBrick(68);
                currentBrick = BrickType.BLUE_BRICK;
                MarioBros.manager.get("audio/sounds/bump.wav", Sound.class).play();
                break;
            case BLUE_BRICK:
                setBrick(134);
                currentBrick = BrickType.BLANCK_BRICK;
                MarioBros.manager.get("audio/sounds/bump.wav", Sound.class).play();
                break;
            case BLANCK_BRICK:
                if(mario.isBig()) {
                    setCategoryFilter(MarioBros.DESTROYED_BIT);
                    getCell().setTile(null);
                    hud.addScore(200);
                    MarioBros.manager.get("audio/sounds/breakblock.wav", Sound.class).play();
                }
                MarioBros.manager.get("audio/sounds/bump.wav", Sound.class).play();
                break;
        }
    }

    public void setBrick(int brick) {
        getCell().setTile(tileSet.getTile(brick));
        hud.addScore(200);
    }
}
