package com.favelaaddons;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FavelaGear {
   private static final EquipmentSlot[] SLOTS = new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.BODY, EquipmentSlot.SADDLE};

   public static String model(Entity entity) {
      if (!(entity instanceof LivingEntity)) {
         return "";
      } else {
         LivingEntity living = (LivingEntity)entity;

         for(EquipmentSlot slot : SLOTS) {
            ItemStack stack = living.getItemBySlot(slot);
            if (stack != null && !stack.isEmpty()) {
               Object model = stack.get(DataComponents.ITEM_MODEL);
               if (model != null) {
                  return String.valueOf(model);
               }
            }
         }

         return "";
      }
   }

   public static String describe(Entity entity) {
      if (!(entity instanceof LivingEntity)) {
         return "";
      } else {
         LivingEntity living = (LivingEntity)entity;
         StringBuilder out = new StringBuilder();

         for(EquipmentSlot slot : SLOTS) {
            ItemStack stack = living.getItemBySlot(slot);
            if (stack != null && !stack.isEmpty()) {
               out.append("\n        ").append(slot.getName()).append(": ").append(stack.getCount()).append("x ").append(stack.getItem());
               Object model = stack.get(DataComponents.ITEM_MODEL);
               if (model != null) {
                  out.append("  model: ").append(model);
               }

               Object modelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
               if (modelData != null) {
                  out.append("  cmd: ").append(modelData);
               }

               if (stack.get(DataComponents.CUSTOM_NAME) != null) {
                  out.append("  name: ").append(stack.getHoverName().getString());
               }
            }
         }

         return out.toString();
      }
   }
}
