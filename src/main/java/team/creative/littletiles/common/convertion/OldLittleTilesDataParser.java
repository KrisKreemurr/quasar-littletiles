package team.creative.littletiles.common.convertion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.common.base.Charsets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTimeline.AnimationEventEntry;
import team.creative.littletiles.common.structure.animation.AnimationTransition;
import team.creative.littletiles.common.structure.animation.PhysicalPart;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.animation.curve.ValueCurveInterpolation;
import team.creative.littletiles.common.structure.animation.curve.ValueCurveInterpolation.CosineCurve;
import team.creative.littletiles.common.structure.animation.curve.ValueCurveInterpolation.CubicCurve;
import team.creative.littletiles.common.structure.animation.curve.ValueCurveInterpolation.HermiteCurve;
import team.creative.littletiles.common.structure.animation.curve.ValueCurveInterpolation.LinearCurve;
import team.creative.littletiles.common.structure.animation.event.AnimationEvent;
import team.creative.littletiles.common.structure.animation.event.ChildDoorEvent;
import team.creative.littletiles.common.structure.animation.event.PlaySoundEvent;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class OldLittleTilesDataParser {
    
    private static boolean LOADED_BLOCK_MAP = false;
    public static final HashMap<String, String> BLOCK_MAP = new HashMap<>();
    
    public static boolean isOld(CompoundTag nbt) {
        return nbt.contains("tiles");
    }
    
    public static LittleTile createTile(CompoundTag nbt) {
        if (!LOADED_BLOCK_MAP) {
            try {
                char splitter = 0x00A7;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(LittleStructurePremade.class.getClassLoader().getResourceAsStream(
                    "1.12.2.txt"), Charsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split("" + splitter);
                        if (data.length == 2)
                            BLOCK_MAP.put(data[0], data[1]);
                    }
                }
                LittleTiles.LOGGER.info("Loaded {} entries of block conversions", BLOCK_MAP.size());
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            LOADED_BLOCK_MAP = true;
        }
        
        String name = nbt.getStringOr("block", "");
        if (nbt.contains("meta") && nbt.getIntOr("meta", 0) != 0)
            name += ":" + nbt.getIntOr("meta", 0);
        int color = -1;
        if (nbt.contains("color"))
            color = nbt.getIntOr("color", 0);
        return new LittleTile(BLOCK_MAP.getOrDefault(name, name), color, Collections.EMPTY_LIST);
    }
    
    public static LittleGroup load(CompoundTag nbt) throws LittleConvertException {
        LittleGrid grid;
        try {
            grid = LittleGrid.getOrThrow(nbt);
        } catch (RuntimeException e) {
            throw new LittleMissingGridException("Invalid grid size " + nbt.getIntOr("grid", 0));
        }
        return loadGroup(nbt, grid);
    }
    
    public static void collect(ListTag tiles, Consumer<LittleTile> consumer) {
        for (int i = 0; i < tiles.size(); i++) {
            CompoundTag tileNbt = tiles.getCompoundOrEmpty(i);
            LittleTile tile;
            if (tileNbt.contains("tile"))
                tile = createTile(tileNbt.getCompoundOrEmpty("tile"));
            else
                tile = createTile(tileNbt);
            if (tileNbt.contains("boxes")) {
                ListTag boxes = tileNbt.getListOrEmpty("boxes");
                for (int j = 0; j < boxes.size(); j++)
                    tile.add(LittleBox.create(boxes.getIntArray(j).orElseGet(() -> new int[0])));
            } else if (tileNbt.contains("bBox"))
                tile.add(LittleBox.create(tileNbt.getIntArray("bBox").orElseGet(() -> new int[0])));
            else if (tileNbt.contains("box"))
                tile.add(LittleBox.create(tileNbt.getIntArray("box").orElseGet(() -> new int[0])));
            consumer.accept(tile);
        }
    }
    
    public static LittleGroup loadGroup(CompoundTag nbt, LittleGrid grid) throws LittleConvertException {
        List<LittleGroup> children = Collections.EMPTY_LIST;
        if (nbt.contains("children")) {
            ListTag list = nbt.getListOrEmpty("children");
            children = new ArrayList<>();
            for (int i = 0; i < list.size(); i++)
                children.add(loadGroup(list.getCompoundOrEmpty(i), grid));
        }
        LittleGroup group = new LittleGroup(convertStructureData(nbt.contains("structure") ? nbt.getCompoundOrEmpty("structure") : null), children);
        collect(nbt.getListOrEmpty("tiles"), x -> group.addTileFast(grid, x));
        return group;
    }
    
    private static void convertDoorBaseData(CompoundTag oldData, CompoundTag newData) {
        convertStructureDataBase(oldData, newData);
        
        var state = oldData.get("state");
        if (state != null)
            newData.put("state", state);
        
        newData.putBoolean("actP", oldData.getBooleanOr("activateParent", false));
        newData.putBoolean("hand", !oldData.getBooleanOr("disableRightClick", false));
        newData.putBoolean("stay", oldData.getBooleanOr("stayAnimated", false));
        newData.putBoolean("sound", oldData.contains("sounds") ? oldData.getBooleanOr("sounds", false) : true);
        newData.putBoolean("noClip", oldData.getBooleanOr("noClip", false));
        newData.putInt("du", oldData.getIntOr("duration", 0));
        newData.putInt("in", oldData.getIntOr("interpolation", 0));
        if (oldData.contains("axisCenter"))
            newData.putIntArray("center", oldData.getIntArray("axisCenter").orElseGet(() -> new int[0]));
        newData.putInt("aS", -1);
    }
    
    private static List<AnimationEventEntry> collectEvents(CompoundTag nbt, boolean opening) {
        ListTag list = nbt.getListOrEmpty("events");
        List<AnimationEventEntry> events = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag eventTag = list.getCompoundOrEmpty(i);
            AnimationEvent event = switch (eventTag.getStringOr("id", "")) {
                case "sound-event" -> eventTag.getBooleanOr("opening", false) == opening ? new PlaySoundEvent(PlaySoundEvent.get(Identifier.parse(eventTag.getStringOr(
                    "sound", ""))), eventTag.getFloatOr("volume", 0F), eventTag.getFloatOr("pitch", 0F)) : null;
                case "child" -> new ChildDoorEvent(eventTag.getIntOr("childId", 0));
                default -> null;
            };
            if (event != null)
                events.add(new AnimationEventEntry(eventTag.getIntOr("tick", 0), event));
        }
        return events;
    }
    
    private static void saveDoor(CompoundTag nbt, PhysicalState start, PhysicalState end, AnimationTimeline opening, AnimationTimeline closing) {
        List<AnimationState> states = new ArrayList<>();
        states.add(new AnimationState("closed", start, !nbt.getBooleanOr("stay", false)));
        states.add(new AnimationState("opened", end, !nbt.getBooleanOr("stay", false)));
        
        ListTag stateList = new ListTag();
        for (int i = 0; i < states.size(); i++)
            stateList.add(states.get(i).save());
        nbt.put("s", stateList);
        
        List<AnimationTransition> transitions = new ArrayList<>();
        if (opening != null)
            transitions.add(new AnimationTransition("opening", 0, 1, opening));
        if (closing != null)
            transitions.add(new AnimationTransition("closing", 1, 0, closing));
        ListTag transitionList = new ListTag();
        for (int i = 0; i < transitions.size(); i++)
            transitionList.add(transitions.get(i).save());
        nbt.put("t", transitionList);
    }
    
    private static ValueCurveInterpolation<Vec1d> loadValueTimelineAndPrepare(PhysicalPart part, int[] data, PhysicalState start, PhysicalState end, LittleGrid grid,
            int duration) {
        if (data.length == 0)
            return null;
        ValueCurveInterpolation<Vec1d> curve = switch (data[0]) {
            case 1 -> new CosineCurve<>();
            case 2 -> new CubicCurve<>();
            case 3 -> new HermiteCurve<>();
            default -> new LinearCurve<>();
        };
        for (int i = 0; i < data[1]; i++) {
            double value = Double.longBitsToDouble((((long) data[3 + i * 3]) << 32) | (data[4 + i * 3] & 0xffffffffL));
            if (Double.isNaN(value))
                value = 0;
            curve.add(data[2 + i * 3], new Vec1d(value));
        }
        
        return prepareValueCurve(part, curve, start, end, grid, duration);
    }
    
    private static ValueCurveInterpolation<Vec1d> prepareValueCurve(PhysicalPart part, ValueCurveInterpolation<Vec1d> curve, PhysicalState start, PhysicalState end,
            LittleGrid grid, int duration) {
        if (curve == null || curve.isEmpty())
            return null;
        
        if (curve.size() == 1) {
            start.set(part, curve.getFirst().x);
            end.set(part, curve.getFirst().x);
            return null;
        }
        
        Pair<Integer, Vec1d> first = curve.getFirstPair();
        if (part.offset)
            first.value.x = grid.toVanillaGrid(first.value.x);
        start.set(part, first.value.x);
        
        if (first.key == 0)
            curve.remove(0);
        
        Pair<Integer, Vec1d> last = curve.getLastPair();
        if (part.offset)
            last.value.x = grid.toVanillaGrid(last.value.x);
        end.set(part, last.value.x);
        
        if (last.key == duration)
            curve.remove(curve.size() - 1);
        
        if (curve.isEmpty())
            return null;
        return curve;
    }
    
    public static CompoundTag convertStructureDataBase(CompoundTag oldData, CompoundTag newData) {
        if (oldData.contains("signal")) {
            Tag signal = oldData.get("signal");
            oldData.remove("signal");
            newData.put("ex", signal);
        }
        
        if (oldData.contains("name")) {
            String name = oldData.getStringOr("name", "");
            oldData.remove("name");
            newData.putString("n", name);
        }
        
        if (oldData.contains("blocks")) {
            int[] array = oldData.getIntArray("blocks").orElseGet(() -> new int[0]);
            oldData.remove("blocks");
            newData.putIntArray("b", array);
        }
        
        if (oldData.contains("parent")) {
            CompoundTag parent = oldData.getCompoundOrEmpty("parent");
            oldData.remove("parent");
            newData.put(LittleGroup.PARENT_KEY, parent);
        }
        
        if (oldData.contains("children")) {
            ListTag children = oldData.getListOrEmpty("children");
            oldData.remove("children");
            newData.put(LittleGroup.CHILDREN_KEY, children);
        }
        
        return newData;
    }
    
    public static CompoundTag convertStructureData(CompoundTag nbt) throws LittleConvertException {
        if (nbt == null)
            return null;
        
        return switch (nbt.getStringOr("id", "")) {
            case "workbench", "importer", "exporter", "blankomatic", "particle_emitter", "signal_display", "structure_builder", "fixed", "ladder", "bed", "chair", "storage", "noclip", "message", "item_holder" -> convertStructureDataBase(
                nbt, nbt);
            case "single_cable1", "single_cable4", "single_cable16", "single_input1", "single_input4", "single_input16", "single_output1", "single_output4", "single_output16" -> convertStructureDataBase(
                nbt, nbt);
            case "light" -> {
                convertStructureDataBase(nbt, nbt);
                nbt.putBoolean("right", !nbt.getBooleanOr("disableRightClick", false));
                nbt.remove("disableRightClick");
                yield nbt;
            }
            case "door" -> {
                CompoundTag converted = new CompoundTag();
                convertDoorBaseData(nbt, converted);
                CompoundTag rotation = new CompoundTag();
                Axis axis = Axis.values()[nbt.getIntOr("axis", 0)];
                rotation.putInt("a", axis.ordinal());
                PhysicalState end = new PhysicalState();
                if (nbt.getIntOr("rot-type", 0) == 1) {
                    double degree = nbt.getDoubleOr("degree", 0D);
                    rotation.putDouble("d", degree);
                    end.rot(axis, degree);
                } else {
                    boolean clockwise = nbt.getBooleanOr("clockwise", false);
                    rotation.putBoolean("c", clockwise);
                    end.rot(axis, clockwise ? 90 : -90);
                }
                converted.put("rotation", rotation);
                
                List<AnimationEventEntry> openingEvents = collectEvents(nbt, true);
                AnimationTimeline opening = null;
                AnimationTimeline closing = null;
                if (openingEvents != null)
                    opening = new AnimationTimeline(nbt.getIntOr("duration", 0), openingEvents);
                List<AnimationEventEntry> closingEvents = collectEvents(nbt, false); // Cannot properly reverse events, but it should be fine in most cases
                if (closingEvents != null)
                    closing = new AnimationTimeline(nbt.getIntOr("duration", 0), closingEvents);
                
                saveDoor(converted, new PhysicalState(), end, opening, closing);
                
                converted.putString("id", "axis");
                yield converted;
            }
            case "slidingDoor" -> {
                CompoundTag converted = new CompoundTag();
                convertDoorBaseData(nbt, converted);
                PhysicalState end = new PhysicalState();
                Facing direction = Facing.VALUES[nbt.getIntOr("direction", 0)];
                converted.putInt("direction", direction.ordinal());
                int distance = nbt.getIntOr("distance", 0);
                converted.putInt("dis", distance);
                LittleGrid grid = LittleGrid.get(nbt);
                converted.putInt("disG", grid.count);
                
                end.off(direction, grid.toVanillaGrid(distance));
                
                List<AnimationEventEntry> openingEvents = collectEvents(nbt, true);
                AnimationTimeline opening = null;
                AnimationTimeline closing = null;
                if (openingEvents != null)
                    opening = new AnimationTimeline(nbt.getIntOr("duration", 0), openingEvents);
                List<AnimationEventEntry> closingEvents = collectEvents(nbt, false); // Cannot properly reverse events, but it should be fine in most cases
                if (closingEvents != null)
                    closing = new AnimationTimeline(nbt.getIntOr("duration", 0), closingEvents);
                
                saveDoor(converted, new PhysicalState(), end, opening, closing);
                
                converted.putString("id", "sliding");
                yield converted;
            }
            case "doorActivator" -> {
                CompoundTag converted = new CompoundTag();
                convertDoorBaseData(nbt, converted);
                int duration = 1;
                converted.putInt("du", duration);
                int[] toActivate = nbt.getIntArray("activate").orElseGet(() -> new int[0]);
                converted.putIntArray("act", toActivate);
                
                List<AnimationEventEntry> events = new ArrayList<>();
                for (int i = 0; i < toActivate.length; i++)
                    events.add(new AnimationEventEntry(0, new ChildDoorEvent(toActivate[i])));
                
                AnimationTimeline opening = null;
                AnimationTimeline closing = null;
                if (events != null) {
                    opening = new AnimationTimeline(duration, events);
                    closing = new AnimationTimeline(duration, events);
                }
                
                saveDoor(converted, new PhysicalState(), new PhysicalState(), opening, closing);
                
                converted.putString("id", "activator");
                yield converted;
            }
            case "advancedDoor" -> {
                CompoundTag converted = new CompoundTag();
                convertDoorBaseData(nbt, converted);
                
                int duration = nbt.getIntOr("duration", 0);
                
                CompoundTag animation = nbt.getCompoundOrEmpty("animation");
                
                PhysicalState start = new PhysicalState();
                PhysicalState end = new PhysicalState();
                
                LittleGrid grid = LittleGrid.get(animation.getIntOr("offGrid", 0));
                
                HashMap<PhysicalPart, ValueCurveInterpolation<Vec1d>> curves = new HashMap<>();
                for (PhysicalPart part : PhysicalPart.values())
                    curves.put(part, loadValueTimelineAndPrepare(part, animation.getIntArray(part.oldKey).orElseGet(() -> new int[0]), start, end, grid, duration));
                
                AnimationTimeline opening = new AnimationTimeline(duration, collectEvents(nbt, true));
                AnimationTimeline closing = new AnimationTimeline(duration, collectEvents(nbt, false));// Cannot properly reverse events, but it should be fine in most cases
                
                for (Entry<PhysicalPart, ValueCurveInterpolation<Vec1d>> entry : curves.entrySet()) {
                    if (entry.getValue() == null)
                        continue;
                    opening.set(entry.getKey(), entry.getValue());
                    closing.set(entry.getKey(), entry.getValue());
                }
                
                saveDoor(converted, start, end, opening, closing);
                
                converted.putString("id", "door");
                yield converted;
            }
            default -> throw new LittleMissingStructureException("Cannot convert " + nbt.getStringOr("id", "") + " yet");
        };
    }
    
    public static CompoundTag convert(CompoundTag nbt) throws LittleConvertException {
        return LittleGroup.save(load(nbt));
    }
    
    public static class LittleConvertException extends Exception {
        
        public LittleConvertException(String name) {
            super(name);
        }
        
        public Component translatable() {
            return Component.translatable("gui.error.convert.structure", getMessage());
        }
        
    }
    
    public static class LittleMissingStructureException extends LittleConvertException {
        
        public LittleMissingStructureException(String name) {
            super(name);
        }
        
    }
    
    public static class LittleMissingGridException extends LittleConvertException {
        
        public LittleMissingGridException(String name) {
            super(name);
        }
        
        @Override
        public Component translatable() {
            return Component.translatable("gui.error.convert.structure", getMessage());
        }
        
    }
}
