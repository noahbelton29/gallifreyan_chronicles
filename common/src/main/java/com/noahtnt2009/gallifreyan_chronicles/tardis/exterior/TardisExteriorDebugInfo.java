package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class TardisExteriorDebugInfo {

    private TardisExteriorDebugInfo() {}

    public record Info(String tardisId, String exteriorId) {}

    public static Info getLookedAt() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;

        HitResult hit = mc.player.pick(20.0, 0.0f, false);
        if (hit.getType() != HitResult.Type.BLOCK) return null;

        BlockHitResult blockHit = (BlockHitResult) hit;
        Level level = mc.level;
        BlockEntity be = level.getBlockEntity(blockHit.getBlockPos());

        if (!(be instanceof TardisExteriorBlockEntity tardisBE)) return null;

        String tardisId = tardisBE.getTardisId() != null
                ? tardisBE.getTardisId().toString()
                : "unlinked";

        TardisExterior exterior = tardisBE.getExterior();
        String exteriorId = exterior != null ? exterior.id() : "none";

        return new Info(tardisId, exteriorId);
    }
}