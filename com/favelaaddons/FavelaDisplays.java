package com.favelaaddons;

import net.minecraft.ChatFormatting;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public class FavelaDisplays {
   public static Vec3 renderedPosition(Entity entity) {
      if (!(entity instanceof Display)) {
         return entity.position();
      } else {
         try {
            Vector3fc translation = (Vector3fc)entity.getEntityData().get(Display.DATA_TRANSLATION_ID);
            if (translation == null) {
               return entity.position();
            } else {
               double yaw = Math.toRadians((double)entity.getYRot());
               double sin = Math.sin(yaw);
               double cos = Math.cos(yaw);
               double localX = (double)translation.x();
               double localY = (double)translation.y();
               double localZ = (double)translation.z();
               return new Vec3(entity.getX() + localX * cos - localZ * sin, entity.getY() + localY, entity.getZ() + localX * sin + localZ * cos);
            }
         } catch (Exception e) {
            return entity.position();
         }
      }
   }

   public static String modelId(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Object model = stack.get(DataComponents.ITEM_MODEL);
         if (model != null) {
            return String.valueOf(model);
         } else {
            try {
               return String.valueOf(stack.getItem().builtInRegistryHolder().key().identifier());
            } catch (Exception e) {
               return stack.getItem().getDescriptionId();
            }
         }
      } else {
         return null;
      }
   }

   public static String modelId(Entity entity) {
      return entity instanceof Display.ItemDisplay ? modelId(((Display.ItemDisplay)entity).getItemStack()) : null;
   }

   public static boolean matchesAny(String modelId, String[] patterns) {
      if (modelId != null && patterns.length != 0) {
         String lower = modelId.toLowerCase();

         for(String pattern : patterns) {
            if (!pattern.isEmpty() && lower.contains(pattern.toLowerCase())) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
   public static String sanitize(String rawName) {
      String strippedName = ChatFormatting.stripFormatting(rawName);
      if (strippedName == null) {
         strippedName = rawName;
      }

      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < strippedName.length(); ++i) {
         char c = strippedName.charAt(i);
         if (c != '[' && c != ']' && !Character.isSurrogate(c) && (c < '\ue000' || c > '\uf8ff')) {
            sb.append(c);
         }
      }

      return sb.toString().trim().toLowerCase();
   }
}
