package abyssalmc.fruit;

import abyssalmc.GlobalDataHandler;
import abyssalmc.fruit.command.mapgen;
import abyssalmc.fruit.command.metronome;
import abyssalmc.fruit.command.mmt;
import abyssalmc.fruit.command.slot;
import abyssalmc.fruit.sound.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static abyssalmc.fruit.command.metronome.periodArray;
import static abyssalmc.fruit.command.metronome.startMetro;
import static abyssalmc.fruit.command.mmt.*;
import static abyssalmc.fruit.command.slot.autohotkeyenabled;
import static abyssalmc.fruit.command.slot.slotArray;
import static java.lang.Math.floor;


public class Fruit implements ModInitializer {
	public static final String MOD_ID = "fruit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean isBridging = false;
	public static boolean metronomeEnabled = false;
	public static int metroClock = 10;
	public static int bridgingClock = 19; // max metro 12
	public static int metroIndex = 0;
	public static boolean prevelo = false;
	public static int hotkeyIndex = 0;

	public static int dy;
	public static int distance = 0;
	public static boolean freezedistance = true;
	public static Item activeblock = Items.OAK_PLANKS;

	public static double centreOffset = 0;
	public static List<Double> sumoffset1 = new ArrayList<>();
	public static List<Double> sumoffset2 = new ArrayList<>();

	public static int sessionpb = 0;

	public static boolean lastLanded = false;

	private double lastTickTime = 0;
	public static List<Double> tickavg = new ArrayList<>(){};

	public static boolean isTas = false;
	public static boolean isAhk = false;
	public static boolean isMmt = false;



	public static double calculateAverage(List<Double> doubleList) {
		if (doubleList.isEmpty()) {
			throw new IllegalArgumentException("List is empty, cannot calculate average.");
		}

		double sum = 0;
		for (double num : doubleList) {
			sum += num;
		}

		return sum / doubleList.size();
	}

	public static double thresholdTest(List<Double> doubleList) {
		if (doubleList.isEmpty()) {
			throw new IllegalArgumentException("List is empty, cannot calculate average.");
		}
		double flag = 0;
		for (double num : doubleList) {
			if (num > 50)
			flag++;
		}
		return flag;
	}


	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(metronome::register);
		CommandRegistrationCallback.EVENT.register(mmt::register);
		CommandRegistrationCallback.EVENT.register(slot::register);
		CommandRegistrationCallback.EVENT.register(mapgen::register);

		ModSounds.registerSounds();

		GlobalDataHandler.loadGlobalData();
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> GlobalDataHandler.saveGlobalData());

		MinecraftClient mc = MinecraftClient.getInstance();


		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			PlayerEntity p = mc.player;
			if (p != null) {
				// INDICATORS
				double currentTime = System.currentTimeMillis();
				if (lastTickTime != 0) {
					if (tickavg != null){
						double tickDuration = currentTime - lastTickTime;
						if (!(tickavg.size() < 10)) {
							tickavg.remove(0);
						}
						tickavg.add(tickDuration);
						if (calculateAverage(tickavg) >= 54 && thresholdTest(tickavg) > 4){
							isTas = true;
						} else {
							isTas = false;
						}
					}
				}
				lastTickTime = currentTime;
				if (autohotkeyenabled){ isAhk = true; } else { isAhk = false; }
				if (momentumthreshold != 0.003){ isMmt = true; } else { isMmt = false; }

				// ACTIVE BLOCK
				if (p.getMainHandStack().getItem() instanceof BlockItem){
					activeblock = p.getMainHandStack().getItem();
				}

				// CHECK LANDING
				if (p.getY()-floor(p.getY()) == 0){
					lastLanded = true;
				}

				// MOMENTUM THRESHOLD
				if (momentumthreshold != 0.003) {
					if (Math.abs(p.getVelocity().getY()) < momentumthreshold) {
						p.setVelocity(p.getVelocity().getX(), 0, p.getVelocity().getZ());
					}
					if (Math.abs(p.getVelocity().getX()) < momentumthreshold) {
						p.setVelocity(0, p.getVelocity().getY(), p.getVelocity().getZ());
					}
					if (Math.abs(p.getVelocity().getZ()) < momentumthreshold) {
						p.setVelocity(p.getVelocity().getX(), p.getVelocity().getY(), 0);
					}
				}


				// BRIDGING CHECK
				if (isBridging){
					if (Math.round(10000*(p.getY()- floor(p.getY()))) == 6765 || Math.round(10000*(p.getY()- floor(p.getY()))) == 9216){
						isBridging = false;
						startMetro = true;

						// jumped over
						if (lastLanded == false && distance % 2 == 0){
							distance-=2;
						}
						// misplaced second block
						if (distance % 2 != 0){
							distance--;
						}

						freezedistance = true;

						int pbstatus = 0;

						if (distance > sessionpb) {
							pbstatus = 2;
							sessionpb = distance;
						}
						if (distance > GlobalDataHandler.getPb()) {
							pbstatus = 1;
							GlobalDataHandler.setPb(distance);
						}


						if (!sumoffset1.isEmpty() && !sumoffset2.isEmpty() && GlobalDataHandler.getUtils()) {

							p.sendMessage(Text.literal("§lSummary"));

							if (pbstatus == 1){
								p.sendMessage(Text.literal("Distance: " + "§e" + distance + " (New PB!)"));
							} else {
								if (pbstatus == 2) {
									p.sendMessage(Text.literal("Distance: " + "§e" + distance + " (New session PB!)"));
								} else {
									p.sendMessage(Text.literal("Distance: " + "§e" + distance));
								}
							}

							p.sendMessage(Text.literal("First block: " + "§e" + Math.round(calculateAverage(sumoffset1) * 100.0) / 100.0));
							p.sendMessage(Text.literal("Second block: " + "§e" + Math.round(calculateAverage(sumoffset2) * 100.0) / 100.0));

						}
						sumoffset1 = new ArrayList<>();
						sumoffset2 = new ArrayList<>();
					}

					if (bridgingClock == 0){
						isBridging = false;
						startMetro = true;
						if (distance % 2 != 0){
							distance--;
						}
						freezedistance = true;

						int pbstatus = 0;

						if (distance > sessionpb) {
							pbstatus = 2;
							sessionpb = distance;
						}
						if (distance > GlobalDataHandler.getPb()) {
							pbstatus = 1;
							GlobalDataHandler.setPb(distance);
						}


						if (!sumoffset1.isEmpty() && !sumoffset2.isEmpty() && GlobalDataHandler.getUtils()) {
							p.sendMessage(Text.literal("§lSummary"));

							if (pbstatus == 1){
								p.sendMessage(Text.literal("Distance: " + "§e" + distance + " (New PB!)"));
							} else {
								if (pbstatus == 2) {
									p.sendMessage(Text.literal("Distance: " + "§e" + distance + " (New session PB!)"));
								} else {
									p.sendMessage(Text.literal("Distance: " + "§e" + distance));
								}
							}

							p.sendMessage(Text.literal("First block: " + "§e" + Math.round(calculateAverage(sumoffset1) * 100.0) / 100.0));
							p.sendMessage(Text.literal("Second block: " + "§e" + Math.round(calculateAverage(sumoffset2) * 100.0) / 100.0));
						}
						sumoffset1 = new ArrayList<>();
						sumoffset2 = new ArrayList<>();


						//send report
					}
					else{
						bridgingClock--;
					}
				}


				// METRONOME
				if (metronomeEnabled && isBridging){
					if (startMetro){
						metroIndex = 0;
						startMetro = false;
						metroClock = periodArray[0]+1;
					}
					if (metroClock != 1){
						metroClock--;
					}
					else{
						p.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value(), SoundCategory.MASTER, 999, 1);
						metroIndex++;
						if (metroIndex >= periodArray.length){
							metroIndex = 0;
						}
						metroClock = periodArray[metroIndex];
					}
				}
			}
		});

		AttackBlockCallback.EVENT.register((p, world, hand, pos, hitResult) -> {
			Item item = p.getMainHandStack().getItem();
			if (item instanceof ToolItem toolItem) {
				Block b = world.getBlockState(pos).getBlock();

				if (p.getBlockBreakingSpeed(b.getDefaultState())>=35) {
					world.breakBlock(pos, true, p);

					p.getMainHandStack().damage(1, p, EquipmentSlot.MAINHAND);
				}
			}
			return ActionResult.PASS;
		});

		UseBlockCallback.EVENT.register((p, world, hand, hitResult) -> {
			Item item = p.getMainHandStack().getItem();


			if (world.isClient) {
				// SOUND REPLACE
				if (GlobalDataHandler.getPlaceSounds()){
					p.playSoundToPlayer(ModSounds.OSU, SoundCategory.MASTER, 999, 1);
				}


				// METRONOME
				if(item instanceof BlockItem ||
						item instanceof MinecartItem ||
						item instanceof BoatItem ||
						item instanceof ArrowItem ||
						item instanceof ProjectileItem ||
						item instanceof ArmorStandItem ||
						item instanceof BucketItem ||
						item instanceof AirBlockItem) {

					if (hitResult.getBlockPos().getY() <= p.getY()-1){
						isBridging = true;
						bridgingClock = 20;


						if (metronomeEnabled && startMetro) {
							p.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value(), SoundCategory.MASTER, 999, 1);
						}
						if (freezedistance){
							freezedistance = false;
							distance = 1;
							dy = hitResult.getBlockPos().getY();
						}
						else{
							if (hitResult.getBlockPos().getY() == dy){
								if (hitResult.getSide() != Direction.UP && hitResult.getSide() != Direction.DOWN){
									distance++;
									lastLanded = false;
								}
							}
						}
					}

				}


				// AUTO HOTKEY
				if (autohotkeyenabled) {
					p.getInventory().selectedSlot = slotArray[hotkeyIndex] - 1;

					mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slotArray[hotkeyIndex] - 1));
					//p.sendMessage(Text.literal("" + (slotArray[hotkeyIndex] - 1) + ""));

					if (hotkeyIndex == slotArray.length - 1) {
						hotkeyIndex = 0;
					} else {
						hotkeyIndex++;
					}
				}



				//CENTREDNESS
				if(GlobalDataHandler.getUtils()) {
					if (item instanceof BlockItem) {
						if (hitResult.getPos().getY() != floor(hitResult.getPos().getY())) {

							centreOffset = -2 + 0.5 + 4 * (hitResult.getPos().getY() - floor(hitResult.getPos().getY()));
							System.out.println(centreOffset - 0.5);


							if (isBridging) {
								if (distance % 2 == 0) {
									sumoffset2.add(centreOffset - 0.5); // block 2
								} else {
									sumoffset1.add(centreOffset - 0.5); // block 1
								}

							}
						}
					}
				}
			}



			return ActionResult.PASS;
		});


		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			TextRenderer textRenderer = mc.textRenderer;
			//drawContext.drawText(textRenderer, textstring, textx, texty, 0xffffff, true);


			// pbs4: 423-6(l-1) - 12 if session, 250/260
			// pbs3: 583-6(l-1) - 12 if session, 340/350
			// pbs2.5: 743-6(l-1) - 12 if session, 430/440
			// pbs2: 903-6(l-1) - 12 if session, 520/530
			// pbs1.83: 1063-6(l-1) - 12 if session, 610/620
			// pbs1.67: 1223-6(l-1) - 12 if session, 700/710
			// pbs1.5: 1383-6(l-1) - 12 if session, 790/800
			// pbs1.33: 1543-6(l-1) - 12 if session, 880/890
			// pbs1.17: 1703-6(l-1) - 12 if session, 970/980
			// pbs1: 1863-6(l-1) - 12 if session, 1060/1070


			// d4: 237-3(l-1), 225(c)/211(s) + 10
			// d3: 317-3(l-1), 315(c)/301(s) + 10
			// d2.5: 397-3(l-1), 405(c)/391(s) + 10
			// d2: 477-3(l-1), 495(c)/481(s) + 10
			// d1.83: 557-3(l-1), 585(c)/571(s) + 10
			// d1.67: 637-3(l-1), 675(c)/661(s) + 10
			// d1.5: 717-3(l-1), 765(c)/751(s) + 10
			// d1.33: 797-3(l-1), 855(c)/841(s) + 10
			// d1.17: 877-3(l-1), 945(c)/931(s) + 10
			// d1: 957-3(l-1), 1035(c)/1021(s) + 10




			//0 center, 1 left, 2 right
			int dpos = 0;
			if (GlobalDataHandler.getUtils()) {

				if (dpos == 0) {
					int guiscale = MinecraftClient.getInstance().options.getGuiScale().getValue();

					if (GlobalDataHandler.getDist()) {
						int guiy = 0;
						if (MinecraftClient.getInstance().interactionManager.getCurrentGameMode() != GameMode.CREATIVE){
							guiy = -14;
						}
						switch (guiscale){
							case 3:
								drawContext.drawText(textRenderer, "" + distance, 317 - 3 * ((String.valueOf(distance).length() - 1)), 325+guiy, 0xffffff, true);
								break;
							case 2:
								drawContext.drawText(textRenderer, "" + distance, 477 - 3 * ((String.valueOf(distance).length() - 1)), 505+guiy, 0xffffff, true);
								break;
							case 1:
								drawContext.drawText(textRenderer, "" + distance, 957 - 3 * ((String.valueOf(distance).length() - 1)), 1045+guiy, 0xffffff, true);
								break;
							default:
								drawContext.drawText(textRenderer, "" + distance, 237 - 3 * ((String.valueOf(distance).length() - 1)), 235+guiy, 0xffffff, true);
								break;
						}
					}
					if (GlobalDataHandler.getPbHud()) {
						switch (guiscale){
							case 3:
								drawContext.drawText(textRenderer, "Local PB: §e" + GlobalDataHandler.getPb(), 583 - 6 * ((String.valueOf(GlobalDataHandler.getPb()).length() - 1)), 340, 0xffffff, true);
								drawContext.drawText(textRenderer, "Session PB: §e" + sessionpb, 572 - 6 * ((String.valueOf(sessionpb).length() - 1)), 350, 0xffffff, true);
								break;
							case 2:
								drawContext.drawText(textRenderer, "Local PB: §e" + GlobalDataHandler.getPb(), 903 - 6 * ((String.valueOf(GlobalDataHandler.getPb()).length() - 1)), 520, 0xffffff, true);
								drawContext.drawText(textRenderer, "Session PB: §e" + sessionpb, 892 - 6 * ((String.valueOf(sessionpb).length() - 1)), 530, 0xffffff, true);
								break;
							case 1:
								drawContext.drawText(textRenderer, "Local PB: §e" + GlobalDataHandler.getPb(), 1863 - 6 * ((String.valueOf(GlobalDataHandler.getPb()).length() - 1)), 1060, 0xffffff, true);
								drawContext.drawText(textRenderer, "Session PB: §e" + sessionpb, 1852 - 6 * ((String.valueOf(sessionpb).length() - 1)), 1070, 0xffffff, true);
								break;
							default:
								drawContext.drawText(textRenderer, "Local PB: §e" + GlobalDataHandler.getPb(), 423 - 6 * ((String.valueOf(GlobalDataHandler.getPb()).length() - 1)), 250, 0xffffff, true);
								drawContext.drawText(textRenderer, "Session PB: §e" + sessionpb, 412 - 6 * ((String.valueOf(sessionpb).length() - 1)), 260, 0xffffff, true);
								break;
						}
					}
				}
			}
		});


	}
}