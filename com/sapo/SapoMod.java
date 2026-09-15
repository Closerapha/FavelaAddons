package com.sapo;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.awt.Color;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SapoMod implements ClientModInitializer {
   public static int alertTimeRemaining = 0;
   public static String aliveOrDeadMessage = "";
   public static int aliveOrDeadColor = 0;

   public void onInitializeClient() {
      Config.load();
      Sapo.registrar();
      if (Build.FULL) {
         SapoESP.registrar();
         SapoAutoWalls.registrar();
         SapoArmorSwap.registrar();
         SapoTraits.registrar();
      }

      SapoTrapCounter.registrar();
      SapoCalls.registrar();
      SapoBossHp.registrar();
      SapoSplits.registrar();
      SapoPortals.registrar();
      ClientReceiveMessageEvents.GAME.register((ClientReceiveMessageEvents.Game)(message, overlay) -> this.handleChatMessage(message.getString()));
      ClientReceiveMessageEvents.CHAT.register((ClientReceiveMessageEvents.Chat)(message, signedMessage, sender, params, receptionTimestamp) -> this.handleChatMessage(message.getString()));
      ClientTickEvents.END_CLIENT_TICK.register((ClientTickEvents.EndTick)(client) -> {
         if (alertTimeRemaining > 0) {
            --alertTimeRemaining;
         }

         SapoDPS.onTick(client);
         if (client.level != null && client.player != null && !SapoPuzzle.activeSolution.isEmpty() && client.player.tickCount % 10 == 0) {
            for(BlockPos pos : SapoPuzzle.activeSolution) {
               client.level.addParticle(ParticleTypes.HAPPY_VILLAGER, (double)pos.getX() + (double)0.5F, (double)pos.getY() + 1.2, (double)pos.getZ() + (double)0.5F, (double)0.0F, (double)0.0F, (double)0.0F);
            }
         }

      });
      HudElementRegistry.addLast(Identifier.parse("sapo:alert"), (graphics, tracker) -> {
         if (alertTimeRemaining > 0) {
            String text = Config.aliveOrDeadMode && aliveOrDeadColor != 0 ? aliveOrDeadMessage : Config.alertText;
            int renderColor = Config.aliveOrDeadMode && aliveOrDeadColor != 0 ? aliveOrDeadColor : Config.alertColor;
            renderColor |= -16777216;
            int posX = Config.aliveOrDeadMode && aliveOrDeadColor != 0 ? Config.aliveOrDeadX : Config.alertX;
            int posY = Config.aliveOrDeadMode && aliveOrDeadColor != 0 ? Config.aliveOrDeadY : Config.alertY;
            float scale = Config.aliveOrDeadMode && aliveOrDeadColor != 0 ? Config.aliveOrDeadScale : Config.alertScale;
            graphics.pose().pushMatrix();
            graphics.pose().translate((float)posX, (float)posY);
            graphics.pose().scale(scale, scale);
            graphics.text(Minecraft.getInstance().font, text, 0, 0, renderColor, true);
            graphics.pose().popMatrix();
         }

         if (Config.dpsHudEnabled && Config.active) {
            double dps = SapoDPS.getCurrentDPS();
            Object[] var10001 = new Object[]{dps};
            String dpsText = "DPS: " + String.format("%.1f", var10001);
            graphics.pose().pushMatrix();
            graphics.pose().translate((float)Config.dpsHudX, (float)Config.dpsHudY);
            graphics.pose().scale(Config.dpsHudScale, Config.dpsHudScale);
            if (dps >= (double)600.0F) {
               int xOffset = 0;
               long time = System.currentTimeMillis();

               for(int i = 0; i < dpsText.length(); ++i) {
                  String letter = String.valueOf(dpsText.charAt(i));
                  float hue = (float)((time + (long)(i * 150)) % 2000L) / 2000.0F;
                  int charColor = Color.HSBtoRGB(hue, 1.0F, 1.0F) | -16777216;
                  float waveY = (float)Math.sin((double)time * 0.015 + (double)i * (double)0.5F) * 2.5F;
                  float shakeX = (float)(Math.random() * (double)1.5F - (double)0.75F);
                  float shakeY = (float)(Math.random() * (double)1.5F - (double)0.75F);
                  graphics.text(Minecraft.getInstance().font, letter, xOffset + (int)shakeX, (int)(waveY + shakeY), charColor, true);
                  xOffset += Minecraft.getInstance().font.width(letter);
               }
            } else {
               int renderColor;
               if (dps >= (double)350.0F) {
                  renderColor = 16733525;
               } else if (dps >= (double)150.0F) {
                  renderColor = 16777045;
               } else {
                  renderColor = 5635925;
               }

               renderColor |= -16777216;
               graphics.text(Minecraft.getInstance().font, dpsText, 0, 0, renderColor, true);
            }

            graphics.pose().popMatrix();
         }

      });
      ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
         LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("fa");
         root.executes((context) -> {
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen((new SapoModMenu()).getModConfigScreenFactory().create(Minecraft.getInstance().screen)));
            return 1;
         });
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("debug").executes((context) -> {
            Config.devMode = !Config.devMode;
            Config.save();
            String state = Config.devMode ? "§2ON" : "§cOFF";
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("Â§a[FavelaClient] Dev Mode: " + state));
            return 1;
         }));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("help").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] Commands:\n§e/fa §7- Opens Mod Menu\n§e/fa help §7- Shows this help\n§e/fa editarHUD §7- Moves the on-screen texts\n§e/fa testar §7- Tests the alert text and the sound\n§e/fa som §7- Tests the alert sound§e/fa trait §7- Dumps the lore of the held weapon§e/fa olhar §7- Diz o que esta na sua mira\n§e/fa splits §7- Limpa e recarrega os splits\n§e/fa splits pb [dungeon] §7- Mostra o personal best\n§e/fa splits iniciar <dungeon> §7- Inicia a run manualmente\n§e/fa split §7- Fecha o segmento atual\n§e/fa split delete §7- Desfaz o ultimo split\n§e/fa split cancel §7- Cancela a run atual\n§e/fa split status §7- Mostra o cue que o mod esta esperando\n§e/fa split reset §7- Apaga todos os personal bests\n§e/fa resolver §7- Solves the Lights Up puzzle\n§e/fa limpar §7- Clears particles\n§e/fa debug §7- Toggles dev logs"));
            return 1;
         }));
         if (Build.FULL) {
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("olhar").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] " + SapoLook.describe()));
            return 1;
         }));
         }

         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("split").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] " + SapoSplits.forceSplit()));
            return 1;
         }).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("delete").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] " + SapoSplits.deleteLastSplit()));
            return 1;
         })).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("status").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] " + SapoSplits.status()));
            return 1;
         })).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cancel").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] " + SapoSplits.cancelRun()));
            return 1;
         })).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("reset").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] " + SapoSplits.resetBests()));
            return 1;
         })));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("splits").executes((context) -> {
            SapoSplits.reset();
            SapoSplits.loadTable();
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] Splits limpos, " + SapoSplits.tableSize() + " dungeon(s) carregadas de config/sapo/splits.json" + SapoSplits.listRoutes()));
            return 1;
         }).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("pb").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] " + SapoSplits.personalBests("")));
            return 1;
         }).then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("dungeon", StringArgumentType.greedyString()).executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] " + SapoSplits.personalBests(StringArgumentType.getString(context, "dungeon"))));
            return 1;
         }))).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("iniciar").then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("dungeon", StringArgumentType.greedyString()).executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] " + SapoSplits.manualStart(StringArgumentType.getString(context, "dungeon"))));
            return 1;
         }))).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("split").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] " + SapoSplits.forceSplit()));
            return 1;
         })));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("trait").executes((context) -> {
            Minecraft client = Minecraft.getInstance();
            ItemStack held = client.player != null ? client.player.getMainHandItem() : ItemStack.EMPTY;
            if (held.isEmpty()) {
               ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§c[FavelaClient] Hold the weapon you want to inspect."));
               return 1;
            } else {
               StringBuilder dump = new StringBuilder();
               dump.append("========== [FA Traits] ==========\n");
               dump.append("Item: ").append(held.getHoverName().getString()).append("\n");
               dump.append("Model: ").append(SapoDisplays.modelId(held)).append("\n");
               dump.append("--- Lore ---\n");
               int index = 0;

               for(String line : SapoTraits.loreLines(held)) {
                  ++index;
                  dump.append(String.format("%2d| %s%n", index, line));
                  dump.append("  | clean: ").append(SapoAutoClicker.sanitize(line)).append("\n");
               }

               List<String> traits = SapoTraits.traits(held);
               List<String> stats = SapoTraits.stats(held);
               dump.append("--- Stats ---\n");

               for(String stat : stats) {
                  dump.append("  ").append(stat).append("\n");
               }

               dump.append("--- Traits detected ---\n");
               dump.append("  ").append(traits.isEmpty() ? "(none)" : String.join(", ", traits)).append("\n");
               dump.append("===================================");
               System.out.println(dump);

               try {
                  client.keyboardHandler.setClipboard(dump.toString());
               } catch (Exception var9) {
               }

               ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] §f" + index + " lore lines · " + stats.size() + " stats · traits: §a" + (traits.isEmpty() ? "§7(none)" : String.join(", ", traits)) + " §7- full dump in console and clipboard"));
               return 1;
            }
         }));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("som").executes((context) -> {
            File folder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "sapo");
            File file = new File(folder, "sapo_alerta.wav");
            String state = file.exists() ? "§afound (" + file.length() / 1024L + " KB)" : "§cmissing";
            StringBuilder triggers = new StringBuilder();

            for(String trigger : Config.parsedSoundTriggers) {
               triggers.append("'").append(trigger).append("' ");
            }

            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] §fSound file: " + state + "§f | Volume: §7" + Config.soundVolume + "§f | Triggers: §7" + (triggers.length() > 0 ? triggers.toString() : "(none)") + "§f\n§7" + file.getAbsolutePath() + "\n§fPlaying now - if you hear nothing, check the game log for [FA Sound]."));
            playExternalSound();
            return 1;
         }));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("testar").executes((context) -> {
            alertTimeRemaining = Config.alertTime;
            aliveOrDeadMessage = Config.alertText;
            aliveOrDeadColor = 0;
            playExternalSound();
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] Alert test activated - on-screen text and sound."));
            return 1;
         }));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("editarHUD").executes((context) -> {
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new AlertHudEditorScreen(Component.literal("HUD Editor"))));
            return 1;
         }));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("resolver").executes((context) -> {
            Minecraft client = Minecraft.getInstance();
            SapoPuzzle.escanearEResolver(client);
            return 1;
         }));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("limpar").executes((context) -> {
            SapoPuzzle.activeSolution.clear();
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaClient] Particles cleared."));
            return 1;
         }));
         dispatcher.register(root);
      });
   }

   private void handleChatMessage(String chatText) {
      SapoSplits.logChat(chatText);
      SapoSplits.onChat(chatText);
      Minecraft client = Minecraft.getInstance();
      if (client.player != null) {
         String name = client.player.getName().getString();
         if (chatText.contains(name + " is attempting to solve the edenic light puzzle!")) {
            SapoPuzzle.escanearEResolver(client);
         }
      }

      if (Config.devMode && Config.parsedSoundTriggers.length > 0) {
         System.out.println("[FA Sound] Chat: \"" + chatText + "\"");
      }

      if (Config.parsedSoundTriggers.length > 0) {
         String lowerChat = chatText.toLowerCase();

         for(String trigger : Config.parsedSoundTriggers) {
            if (!trigger.isEmpty() && lowerChat.contains(trigger)) {
               System.out.println("[FA Sound] Trigger matched: '" + trigger + "'");
               playExternalSound();
               break;
            }
         }
      }

      if (Config.parsedTextTriggers.length > 0) {
         for(String trigger : Config.parsedTextTriggers) {
            if (!trigger.isEmpty() && chatText.contains(trigger)) {
               alertTimeRemaining = Config.alertTime;
               aliveOrDeadMessage = Config.alertText;
               aliveOrDeadColor = 0;
               break;
            }
         }
      }

   }

   public static void playExternalSound() {
      (new Thread(() -> {
         File soundFile = null;

         try {
            File configFolder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "sapo");
            soundFile = new File(configFolder, "sapo_alerta.wav");
            if (!soundFile.exists()) {
               System.out.println("[FA Sound] File not found: " + soundFile.getAbsolutePath());
               Config.extractDefaultSound();
               if (!soundFile.exists()) {
                  return;
               }
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            if (clip.isControlSupported(Type.MASTER_GAIN)) {
               FloatControl gainControl = (FloatControl)clip.getControl(Type.MASTER_GAIN);
               float normalizedVolume = Math.max(Config.soundVolume, 1.0E-4F);
               float db = (float)(Math.log10((double)normalizedVolume) * (double)20.0F);
               gainControl.setValue(Math.max(db, gainControl.getMinimum()));
            }

            clip.addLineListener((event) -> {
               if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                  clip.close();

                  try {
                     audioStream.close();
                  } catch (Exception var4) {
                  }
               }

            });
            clip.start();
            if (Config.devMode) {
               System.out.println("[FA Sound] Playing " + soundFile.getName() + " at volume " + Config.soundVolume);
            }
         } catch (javax.sound.sampled.UnsupportedAudioFileException e) {
            System.out.println("[FA Sound] Not a playable WAV file: " + (soundFile != null ? soundFile.getAbsolutePath() : "?") + " (" + e.getMessage() + ")");
         } catch (javax.sound.sampled.LineUnavailableException e) {
            System.out.println("[FA Sound] No audio line available: " + e.getMessage());
         } catch (Exception e) {
            System.out.println("[FA Sound] Failed to play: " + e);
         }

      }, "SapoAudioThread")).start();
   }
}
