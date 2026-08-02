package net.shadowmage.ancientwarfare.core.container;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/*
 * Created by Olivier on 05/02/2015.
 */
public class ContainerEntityBase<T extends Entity> extends ContainerBase {
    public final T entity;

    public ContainerEntityBase(Player player, int id) {
        super(player);
        //noinspection unchecked
        entity = (T) player.level().getEntity(id);
        if (entity == null) {
            throw new IllegalArgumentException("Id wasn't a valid entity");
        }
    }

    @Override
    public boolean stillValid(Player var1) {
        return entity.isAlive();
    }

    public T getEntity() {
        return entity;
    }
}
