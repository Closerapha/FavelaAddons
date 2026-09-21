package com.favelaaddons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class FavelaCrates {
   private static final String CRATE_MODEL = "/furniture/";
   private static final String[] CRATE_SUFFIX = new String[]{"_crate", "_pet", "_mount"};
   private static final String PRIZE_MODEL = "telos:material/";
   private static final String TITLE_MARK = "/gui/";
   private static final String[] CRATE_NAMES = new String[]{"common", "uncommon", "rare", "epic", "legendary", "seasonal", "usual", "strange", "fabled", "exotic"};
   private static final double CRATE_RANGE = 6.0;
   private static final double PRIZE_RANGE = 5.0;
   private static final int MAX_REMEMBERED = 2048;
   private static final long PRIZE_DELAY = 700L;
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final Map<String, List<ItemStack>> POOLS = new HashMap();
   private static Entity pendingPrize = null;
   private static ItemStack pendingStack = ItemStack.EMPTY;
   private static String pendingCrate = "";
   private static long pendingAt = 0L;
   private static final Set<Integer> seenDisplays = new HashSet();
   private static ClientLevel lastLevel = null;
   private static Screen lastScreen = null;
   private static String lastSignature = "";

   private static boolean loaded = false;

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(FavelaCrates::onTick);
   }

   private static File poolFile() {
      File folder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "favelaaddons");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      return new File(folder, "crates.json");
   }

   private static Map<String, List<Saved>> readStored() {
      Map<String, List<Saved>> stored = new HashMap();
      File file = poolFile();
      if (!file.exists()) {
         return stored;
      } else {
         try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);

            try {
               Map<String, List<Saved>> parsed = (Map)GSON.fromJson(reader, (new TypeToken<Map<String, List<Saved>>>() {
               }).getType());
               if (parsed != null) {
                  stored.putAll(parsed);
               }
            } finally {
               reader.close();
            }
         } catch (Exception e) {
            System.out.println("[FA Crate] Could not read crates.json: " + e.getMessage());
         }

         return stored;
      }
   }

   private static void loadPools() {
      File file = poolFile();
      if (file.exists()) {
         try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);

            try {
               Map<String, List<Saved>> stored = (Map)GSON.fromJson(reader, (new TypeToken<Map<String, List<Saved>>>() {
               }).getType());
               if (stored != null) {
                  for(Map.Entry<String, List<Saved>> entry : stored.entrySet()) {
                     List<ItemStack> pool = new ArrayList();

                     for(Saved saved : (List<Saved>)entry.getValue()) {
                        ItemStack stack = saved.toStack();
                        if (!stack.isEmpty()) {
                           pool.add(stack);
                        }
                     }

                     if (pool.isEmpty()) {
                        System.out.println("[FA Crate] Could not rebuild the pool for " + entry.getKey());
                     } else {
                        POOLS.put(entry.getKey(), pool);
                     }
                  }
               }
            } finally {
               reader.close();
            }
         } catch (Exception e) {
            System.out.println("[FA Crate] Could not read crates.json: " + e.getMessage());
         }

      }
   }

   private static void savePools() {
      try {
         Map<String, List<Saved>> stored = readStored();

         for(Map.Entry<String, List<ItemStack>> entry : POOLS.entrySet()) {
            List<Saved> list = new ArrayList();

            for(ItemStack stack : (List<ItemStack>)entry.getValue()) {
               list.add(Saved.of(stack));
            }

            stored.put(entry.getKey(), list);
         }

         OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(poolFile()), StandardCharsets.UTF_8);

         try {
            GSON.toJson(stored, writer);
         } finally {
            writer.close();
         }
      } catch (Exception e) {
         System.out.println("[FA Crate] Could not write crates.json: " + e.getMessage());
      }

   }

   private static void onTick(Minecraft client) {
      if (!loaded && client.level != null) {
         loaded = true;
         loadPools();
      }

      readScreen(client);
      watchWorld(client);
   }

   public static String crateKey(String modelId) {
      if (modelId == null) {
         return "";
      } else {
         String clean = modelId.toLowerCase(Locale.ROOT);
         int cut = clean.lastIndexOf(47);
         String tail = cut < 0 ? clean : clean.substring(cut + 1);

         for(String suffix : CRATE_SUFFIX) {
            if (tail.endsWith(suffix)) {
               return tail;
            }
         }

         return "";
      }
   }

   private static String nearestCrateKey(Minecraft client, String rarity) {
      String best = rarity;
      double bestDistance = Double.MAX_VALUE;

      try {
         AABB box = client.player.getBoundingBox().inflate(CRATE_RANGE * 2.0);

         for(Entity entity : client.level.getEntities(client.player, box)) {
            if (entity instanceof Display.ItemDisplay) {
               String key = crateKey(FavelaDisplays.modelId(entity));
               if (!key.isEmpty() && crateOf(key).equals(rarity)) {
                  double distance = entity.distanceToSqr(client.player);
                  if (distance < bestDistance) {
                     bestDistance = distance;
                     best = key;
                  }
               }
            }
         }
      } catch (Exception var10) {
      }

      return best;
   }

   public static String crateOf(String modelId) {
      if (modelId == null) {
         return "";
      } else {
         String clean = modelId.toLowerCase(Locale.ROOT);
         int cut = clean.lastIndexOf(47);
         String tail = cut < 0 ? clean : clean.substring(cut + 1);

         for(String suffix : CRATE_SUFFIX) {
            if (tail.endsWith(suffix)) {
               return tail.substring(0, tail.length() - suffix.length());
            }
         }

         return "";
      }
   }

   private static String nameFromTexture(String texture) {
      String clean = texture.toLowerCase(Locale.ROOT).replace(".png", "");
      String stripped = crateOf(clean);
      if (!stripped.isEmpty()) {
         return stripped;
      } else {
         int cut = clean.lastIndexOf(47);
         String tail = cut < 0 ? clean : clean.substring(cut + 1);

         for(String name : CRATE_NAMES) {
            if (tail.equals(name) || tail.startsWith(name + "_")) {
               return name;
            }
         }

         return "";
      }
   }

   private static String titleCrate(Component title) {
      if (title == null) {
         return "";
      } else {
         String[] found = new String[]{""};
         StringBuilder seen = new StringBuilder();
         title.visit((style, text) -> {
            String font = FavelaFonts.fontName(style);

            for(int glyph : text.codePoints().toArray()) {
               String texture = FavelaFonts.texture(font, glyph);

               if (texture != null) {

                  if (texture.contains(TITLE_MARK)) {
                     String name = nameFromTexture(texture);
                     if (!name.isEmpty()) {
                        found[0] = name;
                        return Optional.of(Boolean.TRUE);
                     }
                  }
               }
            }

            return Optional.empty();
         }, Style.EMPTY);

         return found[0];
      }
   }

   private static void readScreen(Minecraft client) {
      Screen screen = client.screen;
      if (!(screen instanceof AbstractContainerScreen)) {
         lastScreen = null;
         lastSignature = "";
      } else if (client.player != null) {
         if (screen != lastScreen) {
            lastScreen = screen;
            lastSignature = "";
         }

         List<ItemStack> pool = new ArrayList();
         StringBuilder signature = new StringBuilder();

         try {
            for(Slot slot : ((AbstractContainerScreen)screen).getMenu().slots) {
               ItemStack stack = slot.getItem();
               if (stack != null && !stack.isEmpty() && slot.container != client.player.getInventory()) {
                  pool.add(stack.copy());
                  signature.append(slot.index).append(':').append(stack.getHoverName().getString()).append('|');
               }
            }
         } catch (Exception var8) {
            return;
         }

         String current = signature.toString();
         if (!current.isEmpty() && !current.equals(lastSignature)) {
            lastSignature = current;
            String rarity = titleCrate(screen.getTitle());
            if (!rarity.isEmpty()) {
               String crate = nearestCrateKey(client, rarity);
               List<ItemStack> known = (List)POOLS.get(crate);
               if (known == null || known.size() < pool.size()) {
                  POOLS.put(crate, pool);
                  savePools();
               }
            }
         }
      }

   }

   private static void watchWorld(Minecraft client) {
      if (client.level != null && client.player != null) {
         boolean fresh = client.level != lastLevel;
         if (fresh) {
            lastLevel = client.level;
            seenDisplays.clear();
         }

         if (seenDisplays.size() > MAX_REMEMBERED) {
            seenDisplays.clear();
            fresh = true;
         }

         List<Entity> crates = new ArrayList();
         List<Entity> prizes = new ArrayList();
         AABB box = client.player.getBoundingBox().inflate(CRATE_RANGE * 3.0);

         try {
            for(Entity entity : client.level.getEntities(client.player, box)) {
               if (entity instanceof Display.ItemDisplay) {
                  String model = FavelaDisplays.modelId(entity);
                  if (model == null) {
                     seenDisplays.add(entity.getId());
                  } else if (model.contains(CRATE_MODEL) && !crateOf(model).isEmpty()) {
                     crates.add(entity);
                  } else if (model.startsWith(PRIZE_MODEL) && seenDisplays.add(entity.getId()) && !fresh) {
                     prizes.add(entity);
                  } else {
                     seenDisplays.add(entity.getId());
                  }
               }
            }
         } catch (Exception var11) {
            return;
         }

         if (pendingPrize != null) {
            try {
               ItemStack live = ((Display.ItemDisplay)pendingPrize).getItemStack();
               if (live != null && !live.isEmpty()) {
                  pendingStack = live.copy();
               }
            } catch (Exception var10) {
            }

            if (System.currentTimeMillis() >= pendingAt) {
               Entity settled = pendingPrize;
               pendingPrize = null;
               if (!pendingStack.isEmpty()) {
                  FavelaCrateReel.spin(poolFor(pendingCrate), pendingStack);
               }

               pendingStack = ItemStack.EMPTY;
            }
         } else if (!crates.isEmpty() && !prizes.isEmpty() && !FavelaCrateReel.spinning()) {
            for(Entity prize : prizes) {
               Entity crate = nearest(crates, prize);
               if (crate != null) {
                  pendingPrize = prize;
                  pendingCrate = crateKey(FavelaDisplays.modelId(crate));
                  pendingAt = System.currentTimeMillis() + PRIZE_DELAY;
                  pendingStack = ItemStack.EMPTY;
                  break;
               }
            }

         }
      }
   }

   private static Entity nearest(List<Entity> crates, Entity prize) {
      Entity best = null;
      double bestDistance = PRIZE_RANGE * PRIZE_RANGE;

      for(Entity crate : crates) {
         double distance = crate.distanceToSqr(prize);
         if (distance < bestDistance) {
            bestDistance = distance;
            best = crate;
         }
      }

      return best;
   }

   private static class Saved {
      String item;
      String model;
      String name;
      int count;

      static Saved of(ItemStack stack) {
         Saved saved = new Saved();
         saved.item = String.valueOf(stack.getItem().builtInRegistryHolder().key().identifier());
         Identifier model = (Identifier)stack.get(DataComponents.ITEM_MODEL);
         saved.model = model == null ? null : String.valueOf(model);
         saved.name = stack.getHoverName().getString();
         saved.count = stack.getCount();
         return saved;
      }

      ItemStack toStack() {
         try {
            Item base = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(this.item));
            if (base == null) {
               return ItemStack.EMPTY;
            } else {
               ItemStack stack = new ItemStack(base, Math.max(1, this.count));
               if (this.model != null) {
                  stack.set(DataComponents.ITEM_MODEL, Identifier.parse(this.model));
               }

               if (this.name != null && !this.name.isEmpty()) {
                  stack.set(DataComponents.CUSTOM_NAME, Component.literal(this.name));
               }

               return stack;
            }
         } catch (Exception e) {
            return ItemStack.EMPTY;
         }
      }
   }

   private static List<ItemStack> poolFor(String crate) {
      List<ItemStack> pool = (List)POOLS.get(crate);
      if (pool == null || pool.isEmpty()) {
         pool = (List)POOLS.get(crateOf(crate));
      }

      if (pool != null && !pool.isEmpty()) {
         return pool;
      } else {
         List<ItemStack> everything = new ArrayList();

         for(List<ItemStack> other : POOLS.values()) {
            everything.addAll(other);
         }

         return everything;
      }
   }
}
