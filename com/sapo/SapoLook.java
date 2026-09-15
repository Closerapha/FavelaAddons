package com.sapo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SapoLook {
   private static final double RANGE = 120.0;
   private static final double RADIUS = 2.5;

   public static String describe() {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         Vec3 eye = client.player.getEyePosition(1.0F);
         Vec3 look = client.player.getViewVector(1.0F);
         Vec3 end = eye.add(look.scale(RANGE));
         StringBuilder out = new StringBuilder();
         BlockHitResult blockHit = client.level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, client.player));
         double blockDistance = RANGE;
         String blockLine = "§7Bloco na mira: §8nenhum";
         if (blockHit != null && blockHit.getType() != HitResult.Type.MISS) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = client.level.getBlockState(pos);
            blockDistance = eye.distanceTo(blockHit.getLocation());
            blockLine = String.format(Locale.ROOT, "§7Bloco na mira: §f%s §7em §f%d %d %d §7(%.1fm)", String.valueOf(BuiltInRegistries.BLOCK.getKey(state.getBlock())), pos.getX(), pos.getY(), pos.getZ(), blockDistance);
         }

         List<Entity> hits = new ArrayList();

         for(Entity entity : client.level.getEntities(client.player, new AABB(eye, end).inflate(RADIUS))) {
            Vec3 target = SapoDisplays.renderedPosition(entity);
            Vec3 toTarget = target.subtract(eye);
            double along = toTarget.dot(look);
            if (!(along < 0.0) && !(along > RANGE)) {
               double off = toTarget.subtract(look.scale(along)).length();
               if (!(off > RADIUS)) {
                  hits.add(entity);
               }
            }
         }

         hits.sort(Comparator.comparingDouble((entity) -> eye.distanceTo(SapoDisplays.renderedPosition(entity))));
         out.append("\n§7Entidades na mira: §f").append(hits.size());
         int shown = 0;

         for(Entity entity : hits) {
            if (shown >= 12) {
               break;
            }

            Vec3 target = SapoDisplays.renderedPosition(entity);
            double distance = eye.distanceTo(target);
            String flag = distance > blockDistance ? "§8[atras do bloco] " : "";
            out.append(String.format(Locale.ROOT, "%n  §f%.1fm §7%s%s", distance, flag, SapoDebugger.identityOf(entity, String.valueOf(EntityType.getKey(entity.getType())))));
            String gear = SapoGear.describe(entity);
            if (!gear.isEmpty()) {
               out.append("§8").append(gear.replace("\n        ", " | "));
            }

            if (entity.isInvisible()) {
               out.append(" §8[invisivel]");
            }

            ++shown;
         }

         if (hits.size() > shown) {
            out.append("\n  §8... mais ").append(hits.size() - shown);
         }

         out.append("\n").append(blockLine);
         System.out.println("[FA Look]\n" + out.toString().replaceAll("\u00a7.", ""));
         return out.toString();
      } else {
         return "§cVocê precisa estar num mundo.";
      }
   }
}
