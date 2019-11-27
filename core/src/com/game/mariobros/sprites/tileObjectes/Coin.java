package com.game.mariobros.sprites.tileObjectes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;
import com.game.mariobros.MarioBros;
import com.game.mariobros.scenes.Hud;
import com.game.mariobros.screens.PlayerScreen;
import com.game.mariobros.sprites.Mario;
import com.game.mariobros.sprites.items.ItemDefinition;
import com.game.mariobros.sprites.items.Mushroom;

public class Coin extends InteractiveTileObject {

    private Hud hud;
    private static TiledMapTileSet tileSet;
    private final int BLANCK_COIN = 28;

    public Coin(PlayerScreen screen, MapObject object, Hud hud) {
        super(screen, object);

        tileSet = map.getTileSets().getTileSet("tileset_gutter");

        fixture.setUserData(this);
        setCategoryFilter(MarioBros.COIN_BIT);

        this.hud = hud;
    }

    @Override
    public void onHeadHit(Mario mario) {
        Gdx.app.log("coin", "collision");

        if(getCell().getTile().getId() == BLANCK_COIN) {
            MarioBros.manager.get("audio/sounds/bump.wav", Sound.class).play();
        }
        else {
            if(object.getProperties().containsKey("mushroom")) {
                screen.spawnItem(new ItemDefinition(new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioBros.PPM),
                        Mushroom.class));
                MarioBros.manager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
            }
            else {
                MarioBros.manager.get("audio/sounds/coin.wav", Sound.class).play();
            }
        }

        getCell().setTile(tileSet.getTile(BLANCK_COIN));

        hud.addScore(100);
    }
}