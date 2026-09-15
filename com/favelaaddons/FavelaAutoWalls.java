package com.favelaaddons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public class FavelaAutoWalls {
   private static final int MIN_RING_POINTS = 12;
   private static final double RING_TOLERANCE = 2.0;
   private static final double MIN_GAP_DEGREES = 12.0;
   private static final int TRACER_COLOR = -11141291;
   private static final float TRACER_WIDTH = 4.0F;
   private static final double TRACER_START = 1.5;
   private static Vec3 gapTarget = null;
   private static boolean turnArmed = false;

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(FavelaAutoWalls::onTick);
      LevelRenderEvents.BEFORE_GIZMOS.register(FavelaAutoWalls::render);
   }

   private static void onTick(Minecraft client) {
      Vec3 previous = gapTarget;
      if (Config.autoWalls && client.level != null && client.player != null ) {
         try {
            gapTarget = findGap(client);
         } catch (Exception e) {
            gapTarget = null;
            if (Config.devMode) {
               System.out.println("[FA Auto Walls] Error scanning ring: " + e.getMessage());
            }
         }

      } else {
         gapTarget = null;
      }

      if (gapTarget == null) {
         turnArmed = false;
      } else if (previous == null) {
         turnArmed = true;
         if (Config.devMode) {
            System.out.println("[FA Auto Walls] New gap detected, aiming once.");
         }
      }

      turnToGap(client);
   }

   private static void turnToGap(Minecraft client) {
      Vec3 target = gapTarget;
      boolean active = Config.autoWalls && Config.autoWallsTurn && turnArmed && client.screen == null;
      if (active && target != null && client.player != null) {
         double dx = target.x - client.player.getX();
         double dz = target.z - client.player.getZ();
         float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - (double)90.0F);
         float step = (float)Math.max(1, Config.autoWallsTurnSpeed);
         float deltaYaw = Mth.wrapDegrees(targetYaw - client.player.getYRot());
         if (Math.abs(deltaYaw) <= step) {
            client.player.setYRot(targetYaw);
            client.player.setYHeadRot(targetYaw);
            turnArmed = false;
            if (Config.devMode) {
               System.out.println("[FA Auto Walls] Aimed at the gap.");
            }
         } else {
            float newYaw = client.player.getYRot() + Math.signum(deltaYaw) * step;
            client.player.setYRot(newYaw);
            client.player.setYHeadRot(newYaw);
         }
      }

   }

   public static String diagnose(Minecraft client) {
      StringBuilder report = new StringBuilder();
      report.append("enabled=").append(Config.autoWalls).append(" | tracer=").append(Config.autoWallsTracer).append(" | turn=").append(Config.autoWallsTurn);
      if (client.level == null || client.player == null) {
         return report.append(" | no level").toString();
      } else {
         report.append(" | world=").append(String.valueOf(client.level.dimension().identifier()));
         report.append(" | patterns=");

         for(String pattern : Config.parsedAutoWallsModels) {
            report.append("[").append(pattern).append("]");
         }

         if (Config.parsedAutoWallsModels.length == 0) {
            report.append("(none)");
         }

         double range = Math.max((double)16.0F, (double)Config.espRange);
         AABB box = client.player.getBoundingBox().inflate(range);
         int displays = 0;
         int matched = 0;

         for(Entity entity : client.level.getEntities(client.player, box)) {
            String modelId = FavelaDisplays.modelId(entity);
            if (modelId != null) {
               ++displays;
               if (FavelaDisplays.matchesAny(modelId, Config.parsedAutoWallsModels)) {
                  ++matched;
               }
            }
         }

         report.append(" | range=").append((int)range).append(" | itemDisplays=").append(displays).append(" | matched=").append(matched);
         StringBuilder fit = new StringBuilder();
         Vec3 target = findGap(client, fit);
         report.append(" | ").append(fit);
         if (target != null) {
            report.append(String.format(Locale.ROOT, " | TARGET (%.2f, %.2f, %.2f)", target.x, target.y, target.z));
         }

         return report.toString();
      }
   }

   private static Vec3 findGap(Minecraft client) {
      return findGap(client, null);
   }

   private static void note(StringBuilder diag, String message) {
      if (diag != null) {
         diag.append(message);
      }

   }

   private static Vec3 findGap(Minecraft client, StringBuilder diag) {
      double wallRange = Math.max((double)16.0F, (double)Config.espRange);
      double bossRange = (double)Math.max(1, Config.autoWallsBossRange);
      AABB box = client.player.getBoundingBox().inflate(Math.max(wallRange, bossRange));
      List<double[]> points = new ArrayList();
      boolean guarded = Config.parsedAutoWallsBossModels.length > 0;
      boolean bossFound = false;
      String bossName = null;
      double bossSumX = (double)0.0F;
      double bossSumZ = (double)0.0F;
      int bossParts = 0;

      for(Entity entity : client.level.getEntities(client.player, box)) {
         String modelId = FavelaDisplays.modelId(entity);
         if (modelId != null) {
            Vec3 rendered = FavelaDisplays.renderedPosition(entity);
            double distance = client.player.position().distanceTo(rendered);
            if (guarded && distance <= bossRange && FavelaDisplays.matchesAny(modelId, Config.parsedAutoWallsBossModels)) {
               bossFound = true;
               if (bossName == null) {
                  bossName = modelId.toLowerCase().contains("black") ? "True Ophanim" : "Ophanim";
               }

               bossSumX += rendered.x;
               bossSumZ += rendered.z;
               ++bossParts;
            }

            if (distance <= wallRange && FavelaDisplays.matchesAny(modelId, Config.parsedAutoWallsModels)) {
               points.add(new double[]{rendered.x, rendered.z, rendered.y});
            }
         }
      }
      if (guarded) {
         note(diag, "boss=" + (bossFound ? bossName + " within " + (int)bossRange + " blocks" : "no Ophanim nearby"));
         if (!bossFound) {
            note(diag, " | BAIL: boss is not nearby");
            return null;
         }

         note(diag, " | ");
      }

      note(diag, "points=" + points.size());
      if (points.size() < MIN_RING_POINTS) {
         note(diag, " | BAIL: fewer than " + MIN_RING_POINTS + " wall parts");
         return null;
      } else {
         double sumX = (double)0.0F;
         double sumZ = (double)0.0F;
         double sumXX = (double)0.0F;
         double sumZZ = (double)0.0F;
         double sumXZ = (double)0.0F;
         double sumW = (double)0.0F;
         double sumXW = (double)0.0F;
         double sumZW = (double)0.0F;

         for(double[] p : points) {
            double w = p[0] * p[0] + p[1] * p[1];
            sumX += p[0];
            sumZ += p[1];
            sumXX += p[0] * p[0];
            sumZZ += p[1] * p[1];
            sumXZ += p[0] * p[1];
            sumW += w;
            sumXW += p[0] * w;
            sumZW += p[1] * w;
         }

         double n = (double)points.size();
         double det = sumXX * (sumZZ * n - sumZ * sumZ) - sumXZ * (sumXZ * n - sumZ * sumX) + sumX * (sumXZ * sumZ - sumZZ * sumX);
         if (Math.abs(det) < 1.0E-6) {
            note(diag, " | BAIL: points are collinear");
            return null;
         } else {
            double a = (sumXW * (sumZZ * n - sumZ * sumZ) - sumXZ * (sumZW * n - sumZ * sumW) + sumX * (sumZW * sumZ - sumZZ * sumW)) / det;
            double b = (sumXX * (sumZW * n - sumZ * sumW) - sumXW * (sumXZ * n - sumZ * sumX) + sumX * (sumXZ * sumW - sumZW * sumX)) / det;
            double c = (sumXX * (sumZZ * sumW - sumZW * sumZ) - sumXZ * (sumXZ * sumW - sumZW * sumX) + sumXW * (sumXZ * sumZ - sumZZ * sumX)) / det;
            double centerX = a / (double)2.0F;
            double centerZ = b / (double)2.0F;
            double radiusSq = c + centerX * centerX + centerZ * centerZ;
            if (radiusSq <= (double)0.0F) {
               note(diag, " | BAIL: circle fit failed");
               return null;
            } else {
               double radius = Math.sqrt(radiusSq);
               note(diag, String.format(Locale.ROOT, " | fit center=(%.2f, %.2f) radius=%.2f", centerX, centerZ, radius));
               double playerFromCentre = Math.sqrt((client.player.getX() - centerX) * (client.player.getX() - centerX) + (client.player.getZ() - centerZ) * (client.player.getZ() - centerZ));
               if (bossParts > 0) {
                  double bossX = bossSumX / (double)bossParts;
                  double bossZ = bossSumZ / (double)bossParts;
                  double centreToBoss = Math.sqrt((bossX - centerX) * (bossX - centerX) + (bossZ - centerZ) * (bossZ - centerZ));
                  note(diag, String.format(Locale.ROOT, " | centreToBoss=%.2f playerFromCentre=%.2f", centreToBoss, playerFromCentre));
                  if (Config.autoWallsIgnoreCentre && centreToBoss <= (double)Math.max(1, Config.autoWallsBossClearance) && playerFromCentre > radius) {
                     note(diag, " | BAIL: ring expands from the boss");
                     return null;
                  }
               }

               if (!(radius < (double)1.5F) && !(radius > (double)80.0F)) {
                  List<Double> angles = new ArrayList();

                  for(double[] p : points) {
                     double dx = p[0] - centerX;
                     double dz = p[1] - centerZ;
                     if (Math.abs(Math.sqrt(dx * dx + dz * dz) - radius) <= RING_TOLERANCE) {
                        angles.add(Math.atan2(dz, dx));
                     }
                  }

                  note(diag, " | onCircle=" + angles.size());
                  if (angles.size() < MIN_RING_POINTS) {
                     note(diag, " | BAIL: fewer than " + MIN_RING_POINTS + " parts within " + RING_TOLERANCE + " blocks of the circle");
                     return null;
                  } else {
                     Collections.sort(angles);
                     double widest = (double)0.0F;
                     double gapStart = (double)0.0F;

                     for(int i = 0; i < angles.size(); ++i) {
                        double current = (Double)angles.get(i);
                        double next = (Double)angles.get((i + 1) % angles.size());
                        double span = next - current;
                        if (span <= (double)0.0F) {
                           span += (Math.PI * 2D);
                        }

                        if (span > widest) {
                           widest = span;
                           gapStart = current;
                        }
                     }

                     note(diag, String.format(Locale.ROOT, " | widestGap=%.1f deg", Math.toDegrees(widest)));
                     if (widest < Math.toRadians(MIN_GAP_DEGREES)) {
                        note(diag, " | BAIL: gap below " + MIN_GAP_DEGREES + " deg (ring looks closed)");
                        return null;
                     } else {
                        double middle = gapStart + widest / (double)2.0F;
                        double sumY = (double)0.0F;

                        for(double[] p : points) {
                           sumY += p[2];
                        }

                        Vec3 gap = new Vec3(centerX + Math.cos(middle) * radius, sumY / (double)points.size(), centerZ + Math.sin(middle) * radius);
                        if (bossParts > 0) {
                           double bossX = bossSumX / (double)bossParts;
                           double bossZ = bossSumZ / (double)bossParts;
                           double clearance = Math.sqrt((gap.x - bossX) * (gap.x - bossX) + (gap.z - bossZ) * (gap.z - bossZ));
                           note(diag, String.format(Locale.ROOT, " | gapToBoss=%.1f", clearance));
                           if (clearance < (double)Config.autoWallsBossClearance) {
                              note(diag, " | BAIL: gap is too close to the boss");
                              return null;
                           }
                        }

                        return gap;
                     }
                  }
               } else {
                  note(diag, " | BAIL: radius outside 1.5-80 blocks");
                  return null;
               }
            }
         }
      }
   }

   private static void render(LevelRenderContext context) {
      Vec3 target = gapTarget;
      Minecraft client = Minecraft.getInstance();
      if (Config.autoWalls && Config.autoWallsTracer && target != null && client.player != null) {
         try {
            Camera camera = client.gameRenderer.getMainCamera();
            if (!camera.isInitialized()) {
               return;
            }

            Vector3fc forward = camera.forwardVector();
            Vec3 from = camera.position().add((double)forward.x() * TRACER_START, (double)forward.y() * TRACER_START, (double)forward.z() * TRACER_START);
            Gizmos.line(from, target, TRACER_COLOR, TRACER_WIDTH).setAlwaysOnTop();
            Gizmos.cuboid(new AABB(target.x - 0.4, target.y - 0.4, target.z - 0.4, target.x + 0.4, target.y + 0.4, target.z + 0.4), GizmoStyle.stroke(TRACER_COLOR, TRACER_WIDTH)).setAlwaysOnTop();
         } catch (Exception e) {
            if (Config.devMode) {
               System.out.println("[FA Auto Walls] Error drawing tracer: " + e.getMessage());
            }
         }

      }
   }
}
