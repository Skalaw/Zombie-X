package com.asda.zombiex.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;

/**
 * @author Skala
 */
public class Content {
    private HashMap<String, Texture> textures;
    private HashMap<String, Music> music;
    private HashMap<String, Sound> sounds;

    public Content() {
        textures = new HashMap<String, Texture>();
        music = new HashMap<String, Music>();
        sounds = new HashMap<String, Sound>();
    }

    public void loadTexture(String path) {
        int slashIndex = path.lastIndexOf('/');
        String key;
        if (slashIndex == -1) {
            key = path.substring(0, path.lastIndexOf('.'));
        } else {
            key = path.substring(slashIndex + 1, path.lastIndexOf('.'));
        }
        loadTexture(path, key);
    }

    public void loadTexture(String path, String key) {
        Texture tex = new Texture(Gdx.files.internal(path));
        textures.put(key, tex);
    }

    public Texture getTexture(String key) {
        return textures.get(key);
    }

    public void removeAll() {
        for (Object o : textures.values()) {
            Texture tex = (Texture) o;
            tex.dispose();
        }
        textures.clear();

        for (Object o : music.values()) {
            Music music = (Music) o;
            music.dispose();
        }
        music.clear();

        for (Object o : sounds.values()) {
            Sound sound = (Sound) o;
            sound.dispose();
        }
        sounds.clear();
    }
}
