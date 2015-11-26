package com.asda.zombiex.net;

import com.badlogic.gdx.utils.Array;

/**
 * @author Skala
 */
public interface ClientCallback {
    void onResponse(final Array<String> responses);
}
