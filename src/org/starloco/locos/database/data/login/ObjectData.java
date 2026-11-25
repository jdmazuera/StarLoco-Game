package org.starloco.locos.database.data.login;

import com.mysql.jdbc.Statement;
import com.zaxxer.hikari.HikariDataSource;
import org.starloco.locos.client.Player;
import org.starloco.locos.common.SocketManager;
import org.starloco.locos.database.data.FunctionDAO;
import org.starloco.locos.game.world.World;
import org.starloco.locos.kernel.Constant;
import org.starloco.locos.object.GameObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectData extends FunctionDAO<GameObject> {

    public ObjectData(HikariDataSource dataSource) {
        super(dataSource, "world_objects");
    }

    @Override
    public void loadFully() {
        try {
            getData("SELECT * FROM " + getTableName() + ";", result -> {
                while (result != null && result.next()) {
                    int id = result.getInt("id");
                    int template = result.getInt("template");
                    int quantity = result.getInt("quantity");
                    int position = result.getInt("position");
                    String stats = result.getString("stats");
                    int puit = result.getInt("puit");

                    if (quantity == 0) continue;
                    GameObject object = World.world.newObjet(id, template, quantity, position, stats, puit);
                    if (object.getTemplate() == null)
                        this.delete(object);
                    else
                        World.world.addGameObject(object);
                }
            });
        } catch (SQLException e) {
            super.sendError(e);
        }
    }

    @Override
    public GameObject load(int id) {
        try {
            return getData("SELECT * FROM " + getTableName() + " WHERE `id` IN (" + id + ");", result -> {
                if(!result.next()) return null;
                int template = result.getInt("template");
                int quantity = result.getInt("quantity");
                int position = result.getInt("position");
                String stats = result.getString("stats");
                int puit = result.getInt("puit");

                if (quantity > 0) {
                    GameObject object = World.world.newObjet(result.getInt("id"), template, quantity, position, stats, puit);
                    World.world.addGameObject(object);
                    return object;
                }

                return null;
            });
        } catch (SQLException e) {
            super.sendError(e);
        }
        return null;
    }

    @Override
    public boolean insert(GameObject entity) {
        PreparedStatement statement = null;
        boolean ok = true;
        try {
            statement = this.getConnection().prepareStatement("INSERT INTO " + getTableName() + " (`template`, `quantity`, `position`, `stats`, `puit`) VALUES (?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, entity.getTemplate().getId());
            statement.setInt(2, entity.getQuantity());
            statement.setInt(3, entity.getPosition());
            statement.setString(4, entity.parseToSave());
            statement.setInt(5, entity.getPuit());
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                ok = false;
            } else {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getInt(1));
                    } else {
                        ok = false;
                    }
                }
            }
        } catch (SQLException e) {
            super.sendError(e);
            ok = false;
        } finally {
            close(statement);
        }
        return ok;
    }

    @Override
    public void delete(GameObject entity) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE FROM " + getTableName() + " WHERE id = ?;");
            p.setInt(1, entity.getGuid());
            execute(p);
        } catch (SQLException e) {
            super.sendError(e);
        } finally {
            close(p);
        }
    }

    @Override
    public void update(GameObject entity) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE " + getTableName() + " SET `template` = ?, `quantity` = ?, `position` = ?, `puit` = ?, `stats` = ? WHERE `id` = ?;");
            p.setInt(1, entity.getTemplate().getId());
            p.setInt(2, entity.getQuantity());
            p.setInt(3, entity.getPosition());
            p.setInt(4, entity.getPuit());
            p.setString(5, entity.parseToSave());
            p.setInt(6, entity.getGuid());
            execute(p);
        } catch (SQLException e) {
            super.sendError(e);
        } finally {
            close(p);
        }
    }

    @Override
    public Class<?> getReferencedClass() {
        return ObjectData.class;
    }

    public void loads(String ids) {
        try {
            getData("SELECT * FROM " + getTableName() + " WHERE `id` IN (" + ids + ");", result -> {
                while (result != null && result.next()) {
                    int quantity = result.getInt("quantity");

                    if (quantity > 0) {
                        GameObject object = World.world.newObjet(result.getInt("id"), result.getInt("template"), quantity,
                                result.getInt("position"), result.getString("stats"), result.getInt("puit"));
                        World.world.addGameObject(object);
                    }
                }
            });
        } catch (SQLException e) {
            super.sendError(e);
        }
    }

    private boolean webEditsTableReady = false;

    /** Applies stat edits queued by the web panel (rows of "statId:value;..." where value 0 removes the stat). */
    public void applyWebEdits() {
        try {
            if (!webEditsTableReady) {
                execute("CREATE TABLE IF NOT EXISTS web_item_edits (id INT AUTO_INCREMENT PRIMARY KEY, object_id INT NOT NULL, stats VARCHAR(2000) NOT NULL)");
                webEditsTableReady = true;
            }
            Map<Integer, String[]> edits = new LinkedHashMap<>();
            getData("SELECT id, object_id, stats FROM web_item_edits ORDER BY id;", result -> {
                while (result.next())
                    edits.put(result.getInt("id"), new String[]{result.getString("object_id"), result.getString("stats")});
            });

            for (Map.Entry<Integer, String[]> edit : edits.entrySet()) {
                int objectId = Integer.parseInt(edit.getValue()[0]);
                Player owner = World.world.getOnlinePlayers().stream()
                        .filter(p -> p.getItems().containsKey(objectId)).findFirst().orElse(null);
                if (owner != null && owner.getFight() != null) continue; // retried after the fight

                GameObject object = World.world.getGameObject(objectId);
                if (object != null) {
                    for (String pair : edit.getValue()[1].split(";")) {
                        if (pair.isEmpty()) continue;
                        String[] kv = pair.split(":");
                        int statId = Integer.parseInt(kv[0]), value = Integer.parseInt(kv[1]);
                        if (value == 0) object.getStats().getEffects().remove(statId);
                        else object.getStats().getEffects().put(statId, value);
                    }
                    update(object);
                    if (owner != null) {
                        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(owner, object);
                        if (object.getPosition() != Constant.ITEM_POS_NO_EQUIPED) {
                            owner.refreshStats();
                            SocketManager.GAME_SEND_STATS_PACKET(owner);
                        }
                    }
                }
                execute("DELETE FROM web_item_edits WHERE id = " + edit.getKey() + ";");
            }
        } catch (Exception e) {
            super.sendError(e);
        }
    }
}
