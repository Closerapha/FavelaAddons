package com.favelaaddons;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FavelaMod implements ClientModInitializer {
   public static int alertTimeRemaining = 0;
   public static String aliveOrDeadMessage = "";
   public static int aliveOrDeadColor = 0;

   public void onInitializeClient() {
      Config.load();

      FavelaTrapCounter.registrar();
      FavelaCalls.registrar();
      FavelaBossHp.registrar();
      FavelaPrimed.registrar();
      FavelaVuln.registrar();
      FavelaSplits.registrar();
      FavelaPortals.registrar();
      ClientReceiveMessageEvents.GAME.register((ClientReceiveMessageEvents.Game)(message, overlay) -> this.handleChatMessage(message.getString()));
      ClientReceiveMessageEvents.CHAT.register((ClientReceiveMessageEvents.Chat)(message, signedMessage, sender, params, receptionTimestamp) -> this.handleChatMessage(message.getString()));
      ClientTickEvents.END_CLIENT_TICK.register((ClientTickEvents.EndTick)(client) -> {
         if (alertTimeRemaining > 0) {
            --alertTimeRemaining;
         }

         FavelaDPS.onTick(client);
      });
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:alert"), (graphics, tracker) -> {
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

         if (Config.dpsHudEnabled) {
            double dps = FavelaDPS.getCurrentDPS();
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
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen((new FavelaModMenu()).getModConfigScreenFactory().create(Minecraft.getInstance().screen)));
            return 1;
         });
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("help").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] Commands:\n§e/fa §7- Opens Mod Menu\n§e/fa help §7- Shows this help\n§e/fa hud §7- Moves the on-screen texts\n§e/fa test §7- Tests the alert text and the sound\n§e/fa sound §7- Tests the alert sound\n§e/fa splits §7- Reloads the split routes\n§e/fa splits pb [dungeon] §7- Shows your personal best\n§e/fa splits start <dungeon> §7- Starts a run by hand\n§e/fa split §7- Closes the current segment\n§e/fa split delete §7- Undoes the last split\n§e/fa split cancel §7- Cancels the current run\n§e/fa split status §7- Shows the cue the mod is waiting for\n§e/fa split reset §7- Wipes every personal best\n"));
            return 1;
         }));

         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("split").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] " + FavelaSplits.forceSplit()));
            return 1;
         }).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("delete").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] " + FavelaSplits.deleteLastSplit()));
            return 1;
         })).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("status").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] " + FavelaSplits.status()));
            return 1;
         })).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cancel").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] " + FavelaSplits.cancelRun()));
            return 1;
         })).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("reset").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] " + FavelaSplits.resetBests()));
            return 1;
         })));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("splits").executes((context) -> {
            FavelaSplits.reset();
            FavelaSplits.loadTable();
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] Splits reloaded, " + FavelaSplits.tableSize() + " dungeon(s) loaded from config/favelaaddons/splits.json" + FavelaSplits.listRoutes()));
            return 1;
         }).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("pb").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] " + FavelaSplits.personalBests("")));
            return 1;
         }).then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("dungeon", StringArgumentType.greedyString()).executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] " + FavelaSplits.personalBests(StringArgumentType.getString(context, "dungeon"))));
            return 1;
         }))).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("start").then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("dungeon", StringArgumentType.greedyString()).executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] " + FavelaSplits.manualStart(StringArgumentType.getString(context, "dungeon"))));
            return 1;
         }))).then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("split").executes((context) -> {
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] " + FavelaSplits.forceSplit()));
            return 1;
         })));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("sound").executes((context) -> {
            File folder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "favelaaddons");
            File file = new File(folder, "favela_alerta.wav");
            String state = file.exists() ? "§afound (" + file.length() / 1024L + " KB)" : "§cmissing";
            StringBuilder triggers = new StringBuilder();

            for(String trigger : Config.parsedSoundTriggers) {
               triggers.append("'").append(trigger).append("' ");
            }

            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] §fSound file: " + state + "§f | Volume: §7" + Config.soundVolume + "§f | Triggers: §7" + (triggers.length() > 0 ? triggers.toString() : "(none)") + "§f\n§7" + file.getAbsolutePath() + "\n§fPlaying now - if you hear nothing, check the game log for [FA Sound]."));
            playExternalSound();
            return 1;
         }));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("test").executes((context) -> {
            alertTimeRemaining = Config.alertTime;
            aliveOrDeadMessage = Config.alertText;
            aliveOrDeadColor = 0;
            playExternalSound();
            ((FabricClientCommandSource)context.getSource()).getPlayer().sendSystemMessage(Component.literal("§a[FavelaAddons] Alert test activated - on-screen text and sound."));
            return 1;
         }));
         root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("hud").executes((context) -> {
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new AlertHudEditorScreen(Component.literal("HUD Editor"))));
            return 1;
         }));
         dispatcher.register(root);
      });
   }

   private void handleChatMessage(String chatText) {
      FavelaSplits.onChat(chatText);
      Minecraft client = Minecraft.getInstance();
      if (client.player != null) {
         String name = client.player.getName().getString();
      }


      if (Config.parsedSoundTriggers.length > 0) {
         String lowerChat = chatText.toLowerCase();

         for(String trigger : Config.parsedSoundTriggers) {
            if (!trigger.isEmpty() && lowerChat.contains(trigger)) {

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
            File configFolder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "favelaaddons");
            soundFile = new File(configFolder, "favela_alerta.wav");
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
         } catch (javax.sound.sampled.UnsupportedAudioFileException e) {
            System.out.println("[FA Sound] Not a playable WAV file: " + (soundFile != null ? soundFile.getAbsolutePath() : "?") + " (" + e.getMessage() + ")");
         } catch (javax.sound.sampled.LineUnavailableException e) {
            System.out.println("[FA Sound] No audio line available: " + e.getMessage());
         } catch (Exception e) {
            System.out.println("[FA Sound] Failed to play: " + e);
         }

      }, "FavelaAudioThread")).start();
   }
}
