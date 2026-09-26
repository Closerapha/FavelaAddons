package com.favelaaddons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.resources.Identifier;

public class FavelaSplits {
   private static final int DONE_COLOR = -171;
   private static final int LIVE_COLOR = -11141291;
   private static final int PAST_COLOR = -5592406;
   private static final int PENDING_COLOR = -8355712;
   private static final int GOLD_COLOR = -22016;
   private static final int TITLE_COLOR = -1;
   private static final int TITLE_BG = -1610612736;
   private static final int CURRENT_BG = -1593819136;
   private static final int AHEAD_GAINING = 0xFF13A10E;
   private static final int AHEAD_LOSING = 0xFF7FE08A;
   private static final int BEHIND_LOSING = 0xFF9E1B1B;
   private static final int BEHIND_GAINING = 0xFFE87878;
   private static final int GOLD_SPLIT = 0xFFD4AF37;
   private static final int SPLIT_TIMER = 0xFFFFFFFF;
   private static final int RUN_TIMER = 0xFF13A10E;
   private static final int LIVE_DELTA = 0xFFFFFFFF;
   private static final long FINISHED_TTL = 15000L;
   private static final long FREEZE_TTL = 15000L;
   private static final String[] HUB_WORLDS = new String[]{"telos:realm", "telos:hub"};
   private static final int ROW_HEIGHT = 10;
   private static final float DEAD_EDGE = 0.02F;
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final List<Route> ROUTES = new ArrayList();
   private static final int ROUTE_VERSION = 2;
   private static final List<Split> finished = new ArrayList();
   private static final List<Split> done = new ArrayList();
   private static final List<Step> steps = new ArrayList();
   private static final Map<String, Best> bests = new java.util.LinkedHashMap();
   private static Route active = null;
   private static int segmentIndex = 0;
   private static long runStart = 0L;
   private static long segmentStart = 0L;
   private static Route pendingRoute = null;
   private static long pendingStart = 0L;
   private static String barName = "";
   private static float barProgress = -1.0F;
   private static String lastWorld = "";
   private static long endMillis = 0L;

   public static void registrar() {
      loadTable();
      ClientTickEvents.END_CLIENT_TICK.register(FavelaSplits::onTick);
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:splits"), FavelaSplits::render);
   }

   public static void reset() {
      pendingRoute = null;
      pendingStart = 0L;
      finished.clear();
      done.clear();
      active = null;
      segmentIndex = 0;
      runStart = 0L;
      segmentStart = 0L;
      barName = "";
      barProgress = -1.0F;
   }

   public static int tableSize() {
      return ROUTES.size();
   }

   public static void loadTable() {
      ROUTES.clear();
      loadBests();

      try {
         File folder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "favelaaddons");
         File file = new File(folder, "splits.json");
         if (!file.exists()) {
            if (!folder.exists()) {
               folder.mkdirs();
            }

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

            try {
               GSON.toJson(defaultRoutes(), writer);
            } catch (Throwable var7) {
               try {
                  writer.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            writer.close();
         }

         InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);

         try {
            Route[] routes = (Route[])GSON.fromJson(reader, Route[].class);
            if (routes != null) {
               for(Route route : routes) {
                  if (route != null && route.dungeon != null && route.segments != null && route.segments.length > 0) {
                     ROUTES.add(route);
                  }
               }
            }
         } catch (Throwable var9) {
            try {
               reader.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         reader.close();
         if (ROUTES.isEmpty()) {
            for(Route route : defaultRoutes()) {
               ROUTES.add(route);
            }

            saveTable();
         }

         if (upgrade()) {
            saveTable();
         }

         refreshDelays();

      } catch (Exception e) {
         System.out.println("[FA Splits] Could not read splits.json: " + e.getMessage());
      }

   }

   private static Segment optSeg(String name, String cue) {
      Segment segment = chatSeg(name, cue);
      segment.optional = true;
      return segment;
   }

   private static Segment chatSeg(String name, String cue) {
      Segment segment = new Segment();
      segment.name = name;
      segment.chat = cue;
      return segment;
   }

   private static Segment group(String name, Segment[] children) {
      Segment segment = new Segment();
      segment.name = name;
      segment.children = children;
      return segment;
   }

   private static Segment armedSeg(String name, String arm, String cue) {
      Segment segment = new Segment();
      segment.name = name;
      segment.chatArm = arm;
      segment.chat = cue;
      return segment;
   }

   private static Segment hidden(Segment segment) {
      segment.hidden = Boolean.TRUE;
      return segment;
   }

   private static Segment portalSeg(String name, String cue) {
      Segment segment = new Segment();
      segment.name = name;
      segment.portal = cue;
      return segment;
   }

   private static Route rustbornKingdom() {
      Route route = new Route();
      route.dungeon = "Rustborn Kingdom";
      route.portal = "shatters";
      route.portalStart = 28;
      route.world = "";
      route.segments = new Segment[]{chatSeg("Valerion Clear", "If you insist on entering, prepare to face the wrath of the Rustborn Kingdom's last knight"), group("Valerion", new Segment[]{chatSeg("Phase 1", "My sword may rust, my armor may wear, but my loyalty to this kingdom will never fade"), chatSeg("Desperation", "Valerion has been defeated")}), chatSeg("Mythrion", "After eons... The first infiltration through the kingdom has occurred"), chatSeg("Nebula Clear", "Your fate is entwined with the cosmic threads of oblivion"), chatSeg("Nebula", "Nebula has been defeated"), chatSeg("Opha Clear", "And so they persist"), group("Ophanim", new Segment[]{chatSeg("Phase 1", "You are truly powerless against the very fabric of time"), chatSeg("Phase 2", "And as the clock ticks forwards, your very resolve is tested against the hands of fate"), optSeg("Phase 3", "Nothing you can do, shall change the outcome|Judgment approaches|Very well. Let the true test of your strength commence"), chatSeg("Phase 4", "Wait...*yawn*. that...sound"), chatSeg("Desperation", "Ophanim has been defeated")})};
      return route;
   }

   private static Route raphsCastle() {
      Route route = new Route();
      route.dungeon = "Raph's Castle";
      route.portal = "onyxs_castle";
      route.portalStart = 58;
      route.world = "";
      route.segments = new Segment[]{chatSeg("Raph Clear", "The Champions of Raphael have been defeated"), hidden(portalSeg("Interlude", "onyxs_castle2")), group("Raphael", new Segment[]{chatSeg("Phase 1", "Your abilities are disabled for the rest of this phase"), chatSeg("Phase 2", "But perhaps, I could use a new host. This old fool has been problematic indeed"), chatSeg("Phase 3", "YOU STILL RESIST, RAPHAEL? I COMMAND YOU TO OBLITERATE THEM"), chatSeg("Phase 4", "To think you'd protect the ones who felled Loa. You really are pathetic, Sanguine Lord"), chatSeg("Phase 5", "This is the way my life ends"), armedSeg("Desperation", "I had chains... But now", "IM FREE")})};
      return route;
   }

   private static Route seraphsDomain() {
      Route route = new Route();
      route.dungeon = "Seraph's Domain";
      route.portal = "hardmode_celestials_province";
      route.portalStart = 28;
      route.world = "";
      route.segments = new Segment[]{group("True Seraphim", new Segment[]{chatSeg("Teleport", "I sense a Seraphim's fading spirit. Poor child. Your death deals me more pain than any torture"), chatSeg("Chicken", "Is this YOUR doing?! Inconceivable"), chatSeg("Slow beams / Dance", "And yet, you are unlike anything I have encountered in my ventures to the unknown"), chatSeg("Fast beams / Rain", "YOU CANNOT FATHOM THE TRUE MEANING OF PAIN"), chatSeg("Desperation", "I promised them...Ezekiel...Samael")})};
      return route;
   }

   private static Route celestialsProvince() {
      Route route = new Route();
      route.dungeon = "Celestial's Province";
      route.portal = "celestials_province";
      route.portalStart = 58;
      route.world = "";
      route.segments = new Segment[]{chatSeg("Deimos", "Your strings of fate remain in the hands of Asmodeus"), hidden(chatSeg("Interlude", "YOU DARE SHOW YOUR FACES? PATHETIC INSECTS")), group("Asmodeus", new Segment[]{chatSeg("Chalices / Fireballs", "Your world is DONE. Raphael can't save you from destiny"), chatSeg("Chase", "glory will bathe these lands. But you will not live to see it"), chatSeg("Sawblades", "ENOUGH. COLLAPSE BEFORE ME"), chatSeg("Desperation", "I JUST NEED MORE POWER. GIVE ME ONE MORE CHANCE")}), hidden(chatSeg("Interlude 2", "I've blessed you with more power than most... AND YOU STILL FAIL")), group("Seraphim", new Segment[]{chatSeg("Teleports", "Suffer whilst you can. You'll beg to feel the sting once I reduce you to nothing"), chatSeg("Slow Beams / Dance", "We find your arrogance amusing. but God knows his conquerors shan't fail him"), chatSeg("Fast Beams / Rain", "NO ONE CAN SAVE YOU HERE"), chatSeg("Desperation", "Your home lies in ruin. AND YOUR FATE RESTS IN THE HANDS OF THE OPHANIM AND CHERUBIM")})};
      return route;
   }

   private static Route neoEden() {
      Route route = new Route();
      route.dungeon = "Neo Eden";
      route.portal = "";
      route.startChat = "A tear in reality reveals the entrance to a hidden sanctuary";
      route.startDelay = 4000L;
      route.portalStart = 57;
      route.world = "";
      route.segments = new Segment[]{chatSeg("Neo Eden Clear", "That is sufficient to proceed"), hidden(chatSeg("Interlude", "The garden has guided you")), chatSeg("Twins", "It would have been disappointing if they ended this too early"), hidden(chatSeg("Interlude 2", "Enough performance|You have been observed")), group("Cherubim", new Segment[]{chatSeg("Phase 1", "You arrogant mortal"), chatSeg("Phase 2", "The garden does not exist in a single shape"), chatSeg("Phase 3", "I have already decided where you die"), chatSeg("Phase 4", "I will not be witnessed from within a failing construct"), chatSeg("Desperation", "Cherubim has been defeated|Cherubin has been defeated")})};
      return route;
   }

   private static boolean startFromChat(String message) {
      for(Route route : ROUTES) {
         String cue = route.startChat;
         if (cue != null && !cue.trim().isEmpty() && matchesCue(cue, message)) {
            if (active != route || endMillis > 0L) {
               if (route.startDelay > 0L) {
                  pendingRoute = route;
                  pendingStart = System.currentTimeMillis() + route.startDelay;
               } else {
                  start(route, 0L);
               }
            }

            return true;
         }
      }

      return false;
   }

   private static Route dawnOfCreation() {
      Route route = new Route();
      route.dungeon = "Dawn of Creation";
      route.portal = "hardmode_shatters";
      route.portalStart = 28;
      route.world = "";
      route.segments = new Segment[]{group("True Ophanim", new Segment[]{chatSeg("Phase 1", "Your existence is unfamiliar. A glitch to even Celestials. But I know what you are"), chatSeg("Phase 2", "And I've sensed your spirit fading over and over"), chatSeg("Phase 3", "may not see you. But I see your sanctuary out there. Hehe, struck gold"), chatSeg("Phase 4", "YOU CANNOT FATHOM WHAT I'VE SEEN"), chatSeg("Desperation", "True Ophan has been defeated")})};
      return route;
   }

   private static void carry(Segment[] fresh, Segment[] kept) {
      if (fresh != null && kept != null) {
         for(int i = 0; i < fresh.length && i < kept.length; ++i) {
            Segment a = fresh[i];
            Segment b = kept[i];
            if (a != null && b != null) {
               if (b.name != null && !b.name.trim().isEmpty()) {
                  a.name = b.name;
               }

               carry(a.children, b.children);
            }
         }

      }
   }

   private static boolean upgrade() {
      boolean touched = false;
      Route[] fresh = defaultRoutes();

      for(int i = 0; i < ROUTES.size(); ++i) {
         Route mine = (Route)ROUTES.get(i);
         if (mine.version < ROUTE_VERSION) {
            for(Route stock : fresh) {
               if (stock.dungeon != null && stock.dungeon.equals(mine.dungeon)) {
                  carry(stock.segments, mine.segments);
                  stock.version = ROUTE_VERSION;
                  ROUTES.set(i, stock);
                  touched = true;
                  break;
               }
            }
         }
      }

      for(Route stock : fresh) {
         boolean known = false;

         for(Object seen : ROUTES) {
            if (stock.dungeon != null && stock.dungeon.equals(((Route)seen).dungeon)) {
               known = true;
               break;
            }
         }

         if (!known) {
            stock.version = ROUTE_VERSION;
            ROUTES.add(stock);
            touched = true;
         }
      }

      return touched;
   }

   private static Route[] defaultRoutes() {
      Route[] made = new Route[]{raphsCastle(), rustbornKingdom(), dawnOfCreation(), celestialsProvince(), seraphsDomain(), neoEden()};
      for(Route route : made) {
         route.version = ROUTE_VERSION;
      }

      return made;
   }

   private static Route route(String dungeon, String portal, int portalStart, String[] specs) {
      Route route = new Route();
      route.dungeon = dungeon;
      route.portal = portal;
      route.portalStart = portalStart;
      route.world = "";
      route.segments = new Segment[specs.length];

      for(int i = 0; i < specs.length; ++i) {
         String[] parts = specs[i].split("[|]");
         Segment segment = new Segment();
         segment.name = parts[0];
         if (parts.length > 1) {
            String[] cue = parts[1].split(":");
            if (cue[0].equals("start")) {
               segment.bossStart = cue[1];
            } else if (cue[0].equals("kill")) {
               segment.bossKill = cue[1];
            } else if (cue[0].equals("hp")) {
               segment.hp = Float.valueOf(cue[1]);
            } else if (cue[0].equals("chat")) {
               segment.chat = cue[1];
            }
         }

         route.segments[i] = segment;
      }

      return route;
   }


   private static void refreshDelays() {
      for(Route stored : ROUTES) {
         for(Route fresh : defaultRoutes()) {
            if (stored.dungeon != null && stored.dungeon.equals(fresh.dungeon)) {
               stored.startDelay = fresh.startDelay;
               break;
            }
         }
      }

   }

   public static void saveTable() {
      try {
         File folder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "favelaaddons");
         if (!folder.exists()) {
            folder.mkdirs();
         }

         OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(folder, "splits.json")), StandardCharsets.UTF_8);

         try {
            GSON.toJson(ROUTES.toArray(new Route[0]), writer);
         } catch (Throwable var4) {
            try {
               writer.close();
            } catch (Throwable var3) {
               var4.addSuppressed(var3);
            }

            throw var4;
         }

         writer.close();
      } catch (Exception e) {
         System.out.println("[FA Splits] Could not write splits.json: " + e.getMessage());
      }

   }

   private static Route find(String query) {
      String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
      if (needle.isEmpty()) {
         return null;
      } else {
         for(Route route : ROUTES) {
            if (route.dungeon != null && route.dungeon.toLowerCase(Locale.ROOT).contains(needle)) {
               return route;
            }
         }

         return null;
      }
   }

   public static int routeCount() {
      return ROUTES.size();
   }

   public static String routeName(int route) {
      return route >= 0 && route < ROUTES.size() ? ((Route)ROUTES.get(route)).dungeon : "";
   }

   private static Segment[] segmentsOf(int route) {
      if (route >= 0 && route < ROUTES.size()) {
         Segment[] segments = ((Route)ROUTES.get(route)).segments;
         if (segments != null) {
            return segments;
         }
      }

      return new Segment[0];
   }

   private static Segment[] childrenOf(int route, int segment) {
      Segment[] segments = segmentsOf(route);
      if (segment >= 0 && segment < segments.length) {
         Segment[] children = segments[segment].children;
         if (children != null) {
            return children;
         }
      }

      return new Segment[0];
   }

   public static int segmentCount(int route) {
      return segmentsOf(route).length;
   }

   public static String segmentName(int route, int segment) {
      Segment[] segments = segmentsOf(route);
      return segment >= 0 && segment < segments.length ? String.valueOf(segments[segment].name) : "";
   }

   public static void setSegmentName(int route, int segment, String name) {
      Segment[] segments = segmentsOf(route);
      if (segment >= 0 && segment < segments.length && name != null && !name.trim().isEmpty()) {
         segments[segment].name = name.trim();
      }

   }

   public static int childCount(int route, int segment) {
      return childrenOf(route, segment).length;
   }

   public static String childName(int route, int segment, int child) {
      Segment[] children = childrenOf(route, segment);
      return child >= 0 && child < children.length ? String.valueOf(children[child].name) : "";
   }

   public static void setChildName(int route, int segment, int child, String name) {
      Segment[] children = childrenOf(route, segment);
      if (child >= 0 && child < children.length && name != null && !name.trim().isEmpty()) {
         children[child].name = name.trim();
      }

   }

   public static void applyNames() {
      saveTable();
      if (active != null) {
         for(Step step : steps) {
            if (step.segment != null) {
               step.name = step.segment.name;
               if (step.row != null) {
                  step.row.rename(step.segment.name);
               }
            }
         }
      }

   }

   public static String listRoutes() {
      StringBuilder text = new StringBuilder();

      for(Route route : ROUTES) {
         String portal = route.portal == null || route.portal.trim().isEmpty() ? "§c(auto: " + portalKey(route) + ")" : "§7portal=§f" + route.portal.trim();
         portal = portal + (route.portalStart != null && route.portalStart > 0 ? " §8start=" + route.portalStart + "s" : " §8no offset");
         text.append("\n§e").append(route.dungeon).append(" §8[").append(route.segments.length).append("] ").append(portal);
      }

      return text.toString();
   }

   public static String manualStart(String query) {
      Route route = find(query);
      if (route == null) {
         return "§cNo dungeon by that name." + listRoutes();
      } else {
         start(route);
         return "§aRun started: §e" + route.dungeon;
      }
   }

   public static String forceSplit() {
      Segment segment = current();
      if (active == null || segment == null) {
         return "§cNo run in progress.";
      } else {
         closeSegment(segment);
         return "§aSplit: §e" + segment.name;
      }
   }
   private static void onTick(Minecraft client) {
      if (FavelaPower.off()) {
         return;
      }

      if (!Config.splits || client.player == null || client.level == null) {
         return;
      }

      long stamp = System.currentTimeMillis();
      if (pendingRoute != null && stamp >= pendingStart) {
         Route armed = pendingRoute;
         pendingRoute = null;
         pendingStart = 0L;
         start(armed, 0L);
      }

      finished.removeIf((run) -> stamp - run.start > FINISHED_TTL);
      if (endMillis > 0L && stamp - endMillis > FREEZE_TTL) {
         stop();
      }


      String world = String.valueOf(client.level.dimension().identifier());
      if (!world.equals(lastWorld)) {
         lastWorld = world;
         enterWorld(world);
      }

      LerpingBossEvent bar = FavelaBossHp.activeBar();
      String name = bar == null ? "" : FavelaDisplays.sanitize(bar.getName().getString());
      float progress = bar == null ? -1.0F : bar.getProgress();
      if (!name.equals(barName)) {
         if (!barName.isEmpty()) {
            check("bossKill", barName, barProgress >= 0.0F && barProgress <= DEAD_EDGE);
         }

         if (!name.isEmpty()) {
            check("bossStart", name, true);
         }
      }

      if (!name.isEmpty()) {
         checkHp(name, progress);
      }

      barName = name;
      barProgress = progress;
   }

   private static void enterWorld(String world) {
      for(String hub : HUB_WORLDS) {
         if (world.equalsIgnoreCase(hub)) {
            stop();
            finished.clear();
            return;
         }
      }

      for(Route route : ROUTES) {
         if (route.world != null && !route.world.trim().isEmpty() && world.equalsIgnoreCase(route.world.trim())) {
            start(route);
            return;
         }
      }

      if (active != null && active.world != null && !active.world.trim().isEmpty()) {
         stop();
      }

   }

   public static void onPortal(String model, int seconds) {
      onPortal(model, seconds, null);
   }

   public static void onPortal(String model, int seconds, String where) {
      if (Config.splits) {
         String id = model.toLowerCase(Locale.ROOT);
         String tail = id.substring(id.lastIndexOf(47) + 1);
         if (active != null) {
            Segment segment = current();
            if (segment != null && segment.portal != null && !segment.portal.trim().isEmpty() && tail.equals(segment.portal.trim().toLowerCase(Locale.ROOT))) {
               closeSegment(segment);
               return;
            }
         }

         for(Route route : ROUTES) {
            String key = portalKey(route);
            if (!key.isEmpty() && tail.equals(key)) {
               if (active != route || endMillis > 0L) {
                  start(route, portalOffset(route, seconds));
               }

               return;
            }
         }


      }
   }

   private static long portalOffset(Route route, int seconds) {
      if (route.portalStart != null && route.portalStart > 0 && seconds >= 0) {
         long elapsed = (long)(route.portalStart - seconds) * 1000L;
         if (elapsed < 0L) {
            elapsed = 0L;
         }


         return elapsed;
      } else {
         return 0L;
      }
   }
   private static String portalKey(Route route) {
      if (route.portal != null && !route.portal.trim().isEmpty()) {
         return route.portal.trim().toLowerCase(Locale.ROOT);
      } else if (route.dungeon == null) {
         return "";
      } else {
         StringBuilder out = new StringBuilder();

         for(int i = 0; i < route.dungeon.length(); ++i) {
            char c = Character.toLowerCase(route.dungeon.charAt(i));
            if (c >= 'a' && c <= 'z') {
               out.append(c);
            } else if (c == ' ') {
               out.append('_');
            }
         }

         return out.toString();
      }
   }

   private static void buildSteps(Route route) {
      steps.clear();

      for(Segment segment : route.segments) {
         if (segment.children != null && segment.children.length > 0) {
            Step parent = new Step();
            parent.name = segment.name;
            parent.key = segment.name;
            parent.hidden = Boolean.TRUE.equals(segment.hidden);
            parent.depth = 0;
            parent.segment = null;
            parent.parent = -1;
            steps.add(parent);
            int parentIndex = steps.size() - 1;

            for(Segment child : segment.children) {
               Step leaf = new Step();
               leaf.name = child.name;
               leaf.key = segment.name + "/" + child.name;
               leaf.hidden = Boolean.TRUE.equals(child.hidden);
               leaf.depth = 1;
               leaf.segment = child;
               leaf.parent = parentIndex;
               steps.add(leaf);
            }
         } else {
            Step leaf = new Step();
            leaf.name = segment.name;
            leaf.key = segment.name;
            leaf.hidden = Boolean.TRUE.equals(segment.hidden);
            leaf.depth = 0;
            leaf.segment = segment;
            leaf.parent = -1;
            steps.add(leaf);
         }
      }

   }

   private static void openStep(long stamp) {
      while(segmentIndex < steps.size() && ((Step)steps.get(segmentIndex)).segment == null) {
         Step parent = (Step)steps.get(segmentIndex);
         parent.startMillis = stamp;
         parent.row = new Split(parent.name, parent.depth, stamp);
         parent.row.key = parent.key;
         parent.row.hidden = parent.hidden;
         done.add(parent.row);
         ++segmentIndex;
      }

      if (segmentIndex < steps.size()) {
         Step leaf = (Step)steps.get(segmentIndex);
         leaf.startMillis = stamp;
         leaf.armed = false;
         leaf.row = new Split(leaf.name, leaf.depth, stamp);
         leaf.row.key = leaf.key;
         leaf.row.hidden = leaf.hidden;
         done.add(leaf.row);
      }

   }

   private static void start(Route route) {
      start(route, 0L);
   }

   private static void start(Route route, long offset) {
      long now = System.currentTimeMillis() - offset;
      endMillis = 0L;
      finished.clear();
      active = route;
      runStart = now;
      segmentStart = now;
      done.clear();
      buildSteps(route);
      segmentIndex = 0;
      openStep(now);

   }
   private static void stop() {
      endMillis = 0L;
      active = null;
      segmentIndex = 0;
      steps.clear();
      done.clear();
   }

   private static Segment current() {
      return active != null && segmentIndex < steps.size() ? ((Step)steps.get(segmentIndex)).segment : null;
   }
   private static void check(String kind, String bossName, boolean allowed) {
      if (allowed) {
         Segment segment = current();
         if (segment != null) {
            String cue = kind.equals("bossKill") ? segment.bossKill : segment.bossStart;
            if (cue != null && !cue.trim().isEmpty() && bossName.contains(cue.trim().toLowerCase())) {
               closeSegment(segment);
            }

         }
      }
   }

   private static void checkHp(String bossName, float progress) {
      Segment segment = current();
      if (segment != null && segment.hp != null && progress >= 0.0F) {
         String target = segment.boss == null ? "" : segment.boss.trim().toLowerCase();
         if ((target.isEmpty() || bossName.contains(target)) && progress <= segment.hp / 100.0F) {
            closeSegment(segment);
         }

      }
   }

   public static void onChat(String text) {
      if (Config.splits) {
         String message = normalize(text);
         if (startFromChat(message)) {
            return;
         }

         if (active == null) {
            return;
         }

         Segment segment = current();
         if (segment != null) {

            Step step = (Step)steps.get(segmentIndex);
            if (segment.chatArm != null && !segment.chatArm.trim().isEmpty() && !step.armed) {
               if (matchesCue(segment.chatArm, message)) {
                  step.armed = true;
               }

            } else if (matchesCue(segment.chat, message)) {
               closeSegment(segment);
            } else if (skippable(segment) && segmentIndex + 1 < steps.size()) {
               Segment ahead = ((Step)steps.get(segmentIndex + 1)).segment;
               if (ahead != null && matchesCue(ahead.chat, message)) {
                  skipSegment();
                  Segment now = current();
                  if (now != null) {
                     closeSegment(now);
                  }
               }
            }

         }
      }
   }

   private static boolean skippable(Segment segment) {
      return segment != null && segment.optional != null && segment.optional;
   }

   private static boolean matchesCue(String cue, String message) {
      if (cue != null && !cue.trim().isEmpty()) {
         for(String option : cue.split("[|]")) {
            String needle = normalize(option);
            if (!needle.isEmpty() && message.contains(needle)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
   private static String normalize(String text) {
      StringBuilder out = new StringBuilder();
      boolean gap = false;

      for(int i = 0; i < text.length(); ++i) {
         char c = Character.toLowerCase(text.charAt(i));
         if (c != '\'' && c != '\u2019' && c != '`') {
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
               if (gap && out.length() > 0) {
                  out.append(' ');
               }

               gap = false;
               out.append(c);
            } else {
               gap = true;
            }
         }
      }

      return out.toString();
   }
   private static void skipSegment() {
      Step step = (Step)steps.get(segmentIndex);
      long from = step.startMillis;
      step.row.finish(0L, from - runStart);

      segmentStart = from;
      ++segmentIndex;
      if (segmentIndex < steps.size()) {
         openStep(from);
      }

   }

   private static void closeSegment(Segment segment) {
      long now = System.currentTimeMillis();
      Step step = (Step)steps.get(segmentIndex);
      step.row.finish(now - step.startMillis, now - runStart);
      markSplit(step.row, step.key, now - step.startMillis, now - runStart);

      if (step.parent >= 0) {
         boolean last = segmentIndex + 1 >= steps.size() || ((Step)steps.get(segmentIndex + 1)).parent != step.parent;
         if (last) {
            Step parent = (Step)steps.get(step.parent);
            parent.row.finish(now - parent.startMillis, now - runStart);
            markSplit(parent.row, parent.key, now - parent.startMillis, now - runStart);
         }
      }

      segmentStart = now;
      ++segmentIndex;
      if (segmentIndex >= steps.size()) {
         boolean record = recordRun(active.dungeon, now - runStart);
         Split run = new Split(active.dungeon, 0, now - runStart, now - runStart);
         run.key = record ? "PB" : "";
         finished.add(run);

         while(finished.size() > Math.max(1, Config.splitsMaxRows)) {
            finished.remove(0);
         }

         endMillis = now;
      } else {
         openStep(System.currentTimeMillis());
      }

   }
   private static void loadBests() {
      bests.clear();

      try {
         File file = new File(new File(FabricLoader.getInstance().getConfigDir().toFile(), "favelaaddons"), "pb.json");
         if (file.exists()) {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);

            try {
               Type type = (new TypeToken<Map<String, Best>>() {
               }).getType();
               Map<String, Best> loaded = (Map)GSON.fromJson(reader, type);
               if (loaded != null) {
                  bests.putAll(loaded);
               }
            } catch (Throwable var5) {
               try {
                  reader.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }

               throw var5;
            }

            reader.close();
         }
      } catch (Exception e) {
         System.out.println("[FA Splits] Could not read pb.json: " + e.getMessage());
      }

   }

   private static void saveBests() {
      try {
         File folder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "favelaaddons");
         if (!folder.exists()) {
            folder.mkdirs();
         }

         OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(folder, "pb.json")), StandardCharsets.UTF_8);

         try {
            GSON.toJson(bests, writer);
         } catch (Throwable var4) {
            try {
               writer.close();
            } catch (Throwable var3) {
               var4.addSuppressed(var3);
            }

            throw var4;
         }

         writer.close();
      } catch (Exception e) {
         System.out.println("[FA Splits] Could not write pb.json: " + e.getMessage());
      }

   }

   private static boolean recordRun(String dungeon, long total) {
      Best best = (Best)bests.get(dungeon);
      if (best == null) {
         best = new Best();
         bests.put(dungeon, best);
      }

      boolean record = best.total <= 0L || total < best.total;
      if (record) {
         best.total = total;
         best.order.clear();

         for(Split split : done) {
            if (split.duration >= 0L) {
               best.order.add(split.key);
            }
         }
      }

      for(Split split : done) {
         if (split.duration >= 0L) {
            Long gold = (Long)best.golds.get(split.key);
            if (gold == null || split.duration < gold) {
               best.golds.put(split.key, split.duration);
            }

            if (record) {
               best.run.put(split.key, split.duration);
               best.cum.put(split.key, split.total);
            }
         }
      }

      saveBests();
      return record;
   }

   public static String deleteLastSplit() {
      if (active == null) {
         return "§cNo run in progress.";
      } else if (segmentIndex <= 0) {
         return "§cNothing to undo.";
      } else {
         Step curr = (Step)steps.get(segmentIndex);
         if (curr.row != null) {
            done.remove(curr.row);
            curr.row = null;
         }

         --segmentIndex;

         while(segmentIndex >= 0 && ((Step)steps.get(segmentIndex)).segment == null) {
            Step parent = (Step)steps.get(segmentIndex);
            if (parent.row != null) {
               done.remove(parent.row);
               parent.row = null;
            }

            --segmentIndex;
         }

         if (segmentIndex < 0) {
            segmentIndex = 0;
            openStep(System.currentTimeMillis());
            return "§cNothing to undo.";
         } else {
            Step prev = (Step)steps.get(segmentIndex);
            prev.armed = false;
            if (prev.row != null) {
               prev.row.duration = -1L;
               prev.row.total = -1L;
            }

            segmentStart = prev.startMillis;
            if (prev.parent >= 0) {
               Step parent = (Step)steps.get(prev.parent);
               if (parent.row != null) {
                  parent.row.duration = -1L;
                  parent.row.total = -1L;
               }
            }

            return "§aSplit undone, back to §e" + prev.name;
         }
      }
   }

   public static String status() {
      if (active == null) {
         StringBuilder text = new StringBuilder("§cNo run in progress.");
         text.append("\n§7Portals that start a run:");

         for(Route route : ROUTES) {
            text.append("\n §e").append(route.dungeon).append(" §7<- §f").append(portalKey(route));
         }

         return text.toString();
      } else {
         StringBuilder text = new StringBuilder();
         text.append("§7Run: §e").append(active.dungeon).append(" §f").append(formatTime(System.currentTimeMillis() - runStart));
         Step step = segmentIndex < steps.size() ? (Step)steps.get(segmentIndex) : null;
         if (step == null) {
            return text.append("\n§7No current segment.").toString();
         } else {
            text.append("\n§7Segment: §e").append(step.key);
            Segment segment = step.segment;
            if (segment != null) {
               if (segment.chatArm != null && !segment.chatArm.trim().isEmpty()) {
                  text.append("\n§7Requires first: §f").append(segment.chatArm).append(step.armed ? " §a[seen]" : " §c[waiting]");
               }

               if (segment.chat != null && !segment.chat.trim().isEmpty()) {
                  text.append("\n§7Waiting for chat: §f").append(segment.chat);
               }

               if (segment.portal != null && !segment.portal.trim().isEmpty()) {
                  text.append("\n§7Waiting for portal: §f").append(segment.portal);
               }

               if ((segment.chat == null || segment.chat.trim().isEmpty()) && (segment.portal == null || segment.portal.trim().isEmpty())) {
                  text.append("\n§cThis segment has no cue at all.");
               }
            }

            return text.toString();
         }
      }
   }

   public static String cancelRun() {
      if (active == null) {
         if (pendingRoute == null) {
            return "§cNo run in progress.";
         } else {
            String armed = pendingRoute.dungeon;
            pendingRoute = null;
            pendingStart = 0L;
            return "§aRun cancelled: §e" + armed;
         }
      } else {
         String dungeon = active.dungeon;
         pendingRoute = null;
         pendingStart = 0L;
         stop();
         return "§aRun cancelled: §e" + dungeon;
      }
   }

   public static String resetBests() {
      int count = bests.size();
      bests.clear();

      try {
         File file = new File(new File(FabricLoader.getInstance().getConfigDir().toFile(), "favelaaddons"), "pb.json");
         if (file.exists()) {
            file.delete();
         }
      } catch (Exception var3) {
      }

      return "§a" + count + " personal best(s) wiped.";
   }

   public static String personalBests(String query) {
      if (bests.isEmpty()) {
         return "§7No personal bests yet.";
      } else {
         String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
         StringBuilder text = new StringBuilder();
         if (needle.isEmpty()) {
            text.append("§7Personal bests:");

            for(Map.Entry<String, Best> entry : bests.entrySet()) {
               text.append("\n §e").append(entry.getKey()).append(" §f").append(formatTime(((Best)entry.getValue()).total));
            }

            text.append("\n§8Use /fa splits pb <dungeon> to see the segments.");
         } else {
            for(Map.Entry<String, Best> entry : bests.entrySet()) {
               if (((String)entry.getKey()).toLowerCase(Locale.ROOT).contains(needle)) {
                  Best best = (Best)entry.getValue();
                  text.append("§e").append(entry.getKey()).append(" §f").append(formatTime(best.total));

                  for(String key : best.order) {
                     Long run = (Long)best.run.get(key);
                     Long gold = (Long)best.golds.get(key);
                     String name = key.indexOf(47) >= 0 ? "   " + key.substring(key.indexOf(47) + 1) : " " + key;
                     text.append("\n§7").append(name).append(" §f").append(run == null ? "-" : formatTime(run));
                     if (gold != null && (run == null || gold < run)) {
                        text.append(" §6(gold ").append(formatTime(gold)).append(")");
                     }
                  }

                  return text.toString();
               }
            }

            return "§cNo personal best for that dungeon.";
         }

         return text.toString();
      }
   }

   private static void markSplit(Split row, String key, long duration, long cumulative) {
      row.delta = pbDelta(key, duration);
      row.runDelta = pbRunDelta(key, cumulative);
      row.gold = isGold(key, duration);
   }

   private static long pbRunDelta(String key, long cumulative) {
      if (active == null) {
         return Long.MIN_VALUE;
      } else {
         Best best = (Best)bests.get(active.dungeon);
         if (best == null) {
            return Long.MIN_VALUE;
         } else {
            Long reference = (Long)best.cum.get(key);
            return reference == null ? Long.MIN_VALUE : cumulative - reference;
         }
      }
   }

   private static boolean isGold(String key, long duration) {
      if (active == null) {
         return false;
      } else {
         Best best = (Best)bests.get(active.dungeon);
         if (best == null) {
            return false;
         } else {
            Long gold = (Long)best.golds.get(key);
            return gold != null && duration < gold;
         }
      }
   }

   private static int deltaColor(Split row) {
      if (row.gold) {
         return GOLD_SPLIT;
      } else if (row.runDelta == Long.MIN_VALUE) {
         return row.delta < 0L ? AHEAD_GAINING : BEHIND_LOSING;
      } else {
         boolean ahead = row.runDelta < 0L;
         boolean gaining = row.delta < 0L;
         if (ahead) {
            return gaining ? AHEAD_GAINING : AHEAD_LOSING;
         } else {
            return gaining ? BEHIND_GAINING : BEHIND_LOSING;
         }
      }
   }

   private static long pbDelta(String key, long duration) {
      if (active == null) {
         return Long.MIN_VALUE;
      } else {
         Best best = (Best)bests.get(active.dungeon);
         if (best == null) {
            return Long.MIN_VALUE;
         } else {
            Long reference = (Long)best.run.get(key);
            return reference == null ? Long.MIN_VALUE : duration - reference;
         }
      }
   }

   private static String formatDelta(long delta) {
      String body = formatTime(Math.abs(delta));
      String sign = delta < 0L ? "-" : "+";
      return sign + body + (body.indexOf(58) >= 0 ? "" : "s");
   }

   public static String formatTime(long millis) {
      long total = Math.max(0L, millis);
      long minutes = total / 60000L;
      double seconds = (double)(total % 60000L) / (double)1000.0F;
      return minutes > 0L ? String.format(Locale.ROOT, "%d:%05.2f", minutes, seconds) : String.format(Locale.ROOT, "%.2f", seconds);
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      if (FavelaPower.on() && !FavelaCrateReel.spinning()) {
         Minecraft client = Minecraft.getInstance();
         if (Config.splits && client.player != null && (!finished.isEmpty() || active != null)) {
            Font font = client.font;
            long now = endMillis > 0L ? endMillis : System.currentTimeMillis();
            int width = Config.splitsWidth;
            graphics.pose().pushMatrix();
            graphics.pose().translate((float)Config.splitsX, (float)Config.splitsY);
            graphics.pose().scale(Config.splitsScale, Config.splitsScale);
            int y = 0;

            for(Split run : finished) {
               if (endMillis > 0L || now - run.start > FINISHED_TTL) {
                  continue;
               }

               boolean record = "PB".equals(run.key);
               graphics.text(font, run.name + (record ? " PB" : ""), 0, y, record ? GOLD_COLOR : DONE_COLOR, true);
               String total = formatTime(run.total);
               graphics.text(font, total, width - font.width(total), y, record ? GOLD_COLOR : DONE_COLOR, true);
               y += ROW_HEIGHT;
            }

            if (active != null) {
               graphics.fill(-2, y - 1, width + 2, y + ROW_HEIGHT - 1, TITLE_BG);
               graphics.text(font, active.dungeon, 0, y, TITLE_COLOR, true);
               y += ROW_HEIGHT + 1;
               if (Config.splitsShowPhases) {
                  String openGroup = activeGroup();

                  for(Split split : done) {
                     boolean running = split.duration < 0L;
                     if ((!split.hidden || running) && (split.depth <= 0 || split.key.startsWith(openGroup))) {
                        if (running) {
                           graphics.fill(-2, y - 1, width + 2, y + ROW_HEIGHT - 1, CURRENT_BG);
                        }

                        String total = running ? formatTime(now - runStart) : formatTime(split.total);
                        String delta = !running && split.runDelta != Long.MIN_VALUE ? formatDelta(split.runDelta) : "";
                        splitRow(graphics, font, indent(split.depth) + split.name, delta, deltaColor(split), total, y, running ? TITLE_COLOR : PAST_COLOR);
                        y += ROW_HEIGHT;
                     }
                  }

                  for(int next = segmentIndex + 1; next < steps.size(); ++next) {
                     Step step = (Step)steps.get(next);
                     if (step.depth <= 0 || step.key.startsWith(openGroup)) {
                        splitRow(graphics, font, indent(step.depth) + step.name, "", PENDING_COLOR, pbCumulativeTime(step.key), y, PENDING_COLOR);
                        y += ROW_HEIGHT;
                     }
                  }
               }

               y += 9;
               String big = formatTime(now - runStart);
               graphics.pose().pushMatrix();
               graphics.pose().translate((float)width - (float)font.width(big) * 1.6F, (float)y);
               graphics.pose().scale(1.6F, 1.6F);
               graphics.text(font, big, 0, 0, RUN_TIMER, true);
               graphics.pose().popMatrix();
               y += 16;
               String side = formatTime(now - segmentStart);
               graphics.text(font, side, width - font.width(side), y, SPLIT_TIMER, true);
               String bestSide = currentBest();
               if (!bestSide.isEmpty()) {
                  graphics.text(font, "Best: " + bestSide, 0, y, PAST_COLOR, true);
               }

               y += ROW_HEIGHT + 2;
               int[] tone = new int[]{PAST_COLOR};
               String previous = segmentDelta(false, now, tone);
               footerColored(graphics, font, "Previous Segment", previous, tone[0], y, width);
               y += ROW_HEIGHT;
               String previousBest = segmentDelta(true, now, tone);
               footerColored(graphics, font, "Previous Segment (Best)", previousBest, tone[0], y, width);
               y += ROW_HEIGHT;
               footer(graphics, font, "Sum of Best", sumOfBest(), y, width);
               y += ROW_HEIGHT;
               footer(graphics, font, "Personal Best", personalBestTotal(), y, width);
            }

            graphics.pose().popMatrix();
         }

            }

   }

   private static String pbCumulativeTime(String key) {
      if (active == null) {
         return "-";
      } else {
         Best best = (Best)bests.get(active.dungeon);
         if (best == null) {
            return "-";
         } else {
            Long cumulative = (Long)best.cum.get(key);
            return cumulative == null ? "-" : formatTime(cumulative);
         }
      }
   }

   private static String sumOfBest() {
      if (active == null) {
         return "-";
      } else {
         Best best = (Best)bests.get(active.dungeon);
         if (best == null) {
            return "-";
         } else {
            long sum = 0L;

            for(Step step : steps) {
               if (step.segment != null) {
                  Long gold = (Long)best.golds.get(step.key);
                  if (gold == null) {
                     return "-";
                  }

                  sum += gold;
               }
            }

            return formatTime(sum);
         }
      }
   }

   private static String personalBestTotal() {
      if (active == null) {
         return "-";
      } else {
         Best best = (Best)bests.get(active.dungeon);
         return best != null && best.total > 0L ? formatTime(best.total) : "-";
      }
   }

   private static String activeGroup() {
      if (segmentIndex < steps.size()) {
         Step current = (Step)steps.get(segmentIndex);
         if (current.parent >= 0) {
            return ((Step)steps.get(current.parent)).key + "/";
         }
      }

      return "/";
   }

   private static String indent(int depth) {
      return depth > 0 ? "  " : "";
   }

   private static void splitRow(GuiGraphicsExtractor graphics, Font font, String name, String delta, int deltaColor, String middle, int y, int color) {
      int middleX = Config.splitsWidth - font.width(middle);
      int deltaX = middleX - 3 - font.width(delta);
      int available = Math.max(10, (delta.isEmpty() ? middleX : deltaX) - 2);
      graphics.text(font, fit(font, name, available), 0, y, color, true);
      if (!delta.isEmpty()) {
         graphics.text(font, delta, deltaX, y, deltaColor, true);
      }

      graphics.text(font, middle, middleX, y, color, true);
   }

   private static Split runningSplit() {
      for(int i = done.size() - 1; i >= 0; --i) {
         Split split = (Split)done.get(i);
         if (split.duration < 0L) {
            return split;
         }
      }

      return null;
   }

   private static long referenceFor(String key, boolean gold) {
      if (active == null) {
         return Long.MIN_VALUE;
      } else {
         Best best = (Best)bests.get(active.dungeon);
         if (best == null) {
            return Long.MIN_VALUE;
         } else {
            Long value = (Long)(gold ? best.golds : best.run).get(key);
            return value == null ? Long.MIN_VALUE : value;
         }
      }
   }

   private static String currentBest() {
      Split running = runningSplit();
      if (running == null) {
         return "";
      } else {
         long reference = referenceFor(running.key, true);
         return reference == Long.MIN_VALUE ? "" : formatTime(reference);
      }
   }

   private static String segmentDelta(boolean gold, long now, int[] tone) {
      Split running = runningSplit();
      if (running != null) {
         long reference = referenceFor(running.key, gold);
         long elapsed = now - segmentStart;
         if (reference != Long.MIN_VALUE && elapsed > reference) {
            tone[0] = LIVE_DELTA;
            return formatDelta(elapsed - reference);
         }
      }

      for(int i = done.size() - 1; i >= 0; --i) {
         Split split = (Split)done.get(i);
         if (split.duration >= 0L) {
            long reference = referenceFor(split.key, gold);
            if (reference != Long.MIN_VALUE) {
               long value = split.duration - reference;
               tone[0] = gold ? (value < 0L ? GOLD_SPLIT : BEHIND_LOSING) : deltaColor(split);
               return formatDelta(value);
            }
         }
      }

      tone[0] = PAST_COLOR;
      return "-";
   }

   private static void footerColored(GuiGraphicsExtractor graphics, Font font, String label, String value, int color, int y, int width) {
      graphics.text(font, label, 0, y, PAST_COLOR, true);
      graphics.text(font, value, width - font.width(value), y, color, true);
   }

   private static void footer(GuiGraphicsExtractor graphics, Font font, String label, String value, int y, int width) {
      graphics.text(font, label, 0, y, PAST_COLOR, true);
      graphics.text(font, value, width - font.width(value), y, PAST_COLOR, true);
   }
   private static void row(GuiGraphicsExtractor graphics, Font font, String name, String time, int y, int color) {
      row(graphics, font, name, time, "", 0, y, color);
   }

   private static void row(GuiGraphicsExtractor graphics, Font font, String name, String time, String delta, int deltaColor, int y, int color) {
      int timeWidth = font.width(time);
      int deltaWidth = delta.isEmpty() ? 0 : font.width(delta) + 6;
      int available = Math.max(10, Config.splitsWidth - timeWidth - deltaWidth - 6);
      graphics.text(font, fit(font, name, available), 0, y, color, true);
      if (!delta.isEmpty()) {
         graphics.text(font, delta, Config.splitsWidth - timeWidth - deltaWidth, y, deltaColor, true);
      }

      graphics.text(font, time, Config.splitsWidth - timeWidth, y, color, true);
   }
   private static String fit(Font font, String text, int available) {
      if (font.width(text) <= available) {
         return text;
      } else {
         int limit = Math.max(0, available - font.width(".."));
         StringBuilder out = new StringBuilder();
         int width = 0;

         for(int i = 0; i < text.length(); ++i) {
            int step = font.width(String.valueOf(text.charAt(i)));
            if (width + step > limit) {
               break;
            }

            out.append(text.charAt(i));
            width += step;
         }

         return out.append("..").toString();
      }
   }

   private static class Split {
      String name;
      String key = "";
      boolean hidden;
      long delta = Long.MIN_VALUE;
      long runDelta = Long.MIN_VALUE;
      boolean gold;
      final int depth;
      final long start;
      long duration = -1L;
      long total = -1L;

      Split(String name, int depth, long start) {
         this.name = name;
         this.depth = depth;
         this.start = start;
      }

      Split(String name, int depth, long duration, long total) {
         this.name = name;
         this.depth = depth;
         this.start = System.currentTimeMillis();
         this.duration = duration;
         this.total = total;
      }

      void rename(String newName) {
         this.name = newName;
      }

      void finish(long duration, long total) {
         this.duration = duration;
         this.total = total;
      }
   }

   private static class Best {
      long total = -1L;
      final Map<String, Long> cum = new java.util.LinkedHashMap();
      final List<String> order = new ArrayList();
      final Map<String, Long> run = new java.util.LinkedHashMap();
      final Map<String, Long> golds = new java.util.LinkedHashMap();
   }

   private static class Step {
      String name;
      String key;
      boolean armed;
      boolean hidden;
      int depth;
      int parent;
      long startMillis;
      Segment segment;
      Split row;
   }

   private static class Route {
      int version;
      String dungeon;
      String portal;
      String startChat;
      long startDelay;
      Integer portalStart;
      String world;
      Segment[] segments;
   }

   private static class Segment {
      String name;
      String portal;
      Boolean hidden;
      String chatArm;
      String boss;
      String bossStart;
      String bossKill;
      Float hp;
      String chat;
      Boolean optional;
      Segment[] children;
   }
}
