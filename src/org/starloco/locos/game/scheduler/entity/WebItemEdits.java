package org.starloco.locos.game.scheduler.entity;

import org.starloco.locos.database.DatabaseManager;
import org.starloco.locos.database.data.login.ObjectData;
import org.starloco.locos.database.data.login.PlayerData;
import org.starloco.locos.game.scheduler.Updatable;

/** Polls the item stat and characteristic edits queued by the web panel. */
public class WebItemEdits extends Updatable<Void> {
    public final static WebItemEdits instance = new WebItemEdits(3000);

    private WebItemEdits(int wait) {
        super(wait);
    }

    @Override
    public void update() {
        if (this.verify()) {
            DatabaseManager.get(ObjectData.class).applyWebEdits();
            DatabaseManager.get(PlayerData.class).applyWebEdits();
        }
    }
}
