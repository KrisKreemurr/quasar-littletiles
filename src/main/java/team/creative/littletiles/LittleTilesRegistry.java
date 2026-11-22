package team.creative.littletiles;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializer;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import team.creative.littletiles.common.block.entity.BESignalConverter;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.entity.BETilesRendered;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.mc.BlockArrow;
import team.creative.littletiles.common.block.mc.BlockFlowingLava;
import team.creative.littletiles.common.block.mc.BlockFlowingWater;
import team.creative.littletiles.common.block.mc.BlockLava;
import team.creative.littletiles.common.block.mc.BlockSignalConverter;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.block.mc.BlockWater;
import team.creative.littletiles.common.entity.EntitySit;
import team.creative.littletiles.common.entity.PrimedSizedTnt;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.item.ItemBlockIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient.ColorIngredientType;
import team.creative.littletiles.common.item.ItemLittleBag;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemLittleGlove;
import team.creative.littletiles.common.item.ItemLittleHammer;
import team.creative.littletiles.common.item.ItemLittleMeasureTape;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.item.ItemLittleSaw;
import team.creative.littletiles.common.item.ItemLittleScrewdriver;
import team.creative.littletiles.common.item.ItemLittleWrench;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.item.component.MatrixDataComponent;
import team.creative.littletiles.common.item.component.MeasurementTypeComponent;
import team.creative.littletiles.common.item.component.MeasurementsComponent;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.item.component.TileFilterComponent;
import team.creative.littletiles.common.item.component.TransformMode;
import team.creative.littletiles.common.placement.shape.LittleShapeInstance;
import team.creative.littletiles.common.recipe.StructureIngredient;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittlePremadeType;

public class LittleTilesRegistry {
    
    // ITEMS
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, LittleTiles.MODID);
    
    public static final Holder<Item> HAMMER = ITEMS.register("hammer", () -> new ItemLittleHammer(itemProps("hammer")));
    public static final Holder<Item> BLUEPRINT = ITEMS.register("blueprint", () -> new ItemLittleBlueprint(itemProps("blueprint")));
    public static final Holder<Item> ITEM_TILES = ITEMS.register("multi_tiles", () -> new ItemMultiTiles(itemProps("multi_tiles")));
    public static final Holder<Item> SAW = ITEMS.register("saw", () -> new ItemLittleSaw(itemProps("saw")));
    public static final Holder<Item> BAG = ITEMS.register("bag", () -> new ItemLittleBag(itemProps("bag")));
    public static final Holder<Item> WRENCH = ITEMS.register("wrench", () -> new ItemLittleWrench(itemProps("wrench")));
    public static final Holder<Item> SCREWDRIVER = ITEMS.register("screwdriver", () -> new ItemLittleScrewdriver(itemProps("screwdriver")));
    public static final Holder<Item> CHISEL = ITEMS.register("chisel", () -> new ItemLittleChisel(itemProps("chisel")));
    public static final Holder<Item> PAINT_BRUSH = ITEMS.register("paint_brush", () -> new ItemLittlePaintBrush(itemProps("paint_brush")));
    public static final Holder<Item> GLOVE = ITEMS.register("glove", () -> new ItemLittleGlove(itemProps("glove")));
    public static final Holder<Item> PREMADE = ITEMS.register("premade", () -> new ItemPremadeStructure(itemProps("premade")));
    public static final Holder<Item> MEASURE_TAPE = ITEMS.register("measure_tape", () -> new ItemLittleMeasureTape(itemProps("measure_tape")));
    
    public static final Holder<Item> BLOCK_INGREDIENT = ITEMS.register("blockingredient", () -> new ItemBlockIngredient(itemProps("blockingredient")));
    
    public static final Holder<Item> BLACK_COLOR = ITEMS.register("bottle_black", () -> new ItemColorIngredient(itemProps("bottle_black"), ColorIngredientType.black));
    public static final Holder<Item> CYAN_COLOR = ITEMS.register("bottle_cyan", () -> new ItemColorIngredient(itemProps("bottle_cyan"), ColorIngredientType.cyan));
    public static final Holder<Item> MAGENTA_COLOR = ITEMS.register("bottle_magenta", () -> new ItemColorIngredient(itemProps("bottle_magenta"), ColorIngredientType.magenta));
    public static final Holder<Item> YELLOW_COLOR = ITEMS.register("bottle_yellow", () -> new ItemColorIngredient(itemProps("bottle_yellow"), ColorIngredientType.yellow));
    public static final Holder<Item> ALPHA_COLOR = ITEMS.register("bottle_alpha", () -> new ItemColorIngredient(itemProps("bottle_alpha"), ColorIngredientType.alpha));
    
    // DATA COMPONENTS
    
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, LittleTiles.MODID);
    
    private static final StreamCodec<ByteBuf, CustomData> CUSTOM_DATA_STREAM_CODEC = ByteBufCodecs.TRUSTED_COMPOUND_TAG.map(x -> CustomData.of(x), x -> x.copyTag());
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> DATA = DATA_COMPONENTS.register("tool_config", x -> DataComponentType
            .<CustomData>builder().persistent(CustomData.CODEC).networkSynchronized(CUSTOM_DATA_STREAM_CODEC).build());
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COLOR_AMOUNT = DATA_COMPONENTS.register("color_amount", x -> DataComponentType
            .<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COLOR = DATA_COMPONENTS.register("color", x -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COLOR_SECONDARY = DATA_COMPONENTS.register("color_secondary", x -> DataComponentType
            .<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockIngredientEntry>> BLOCK_INGREDIENT_ENTRY = DATA_COMPONENTS.register("block_ingredient",
        x -> DataComponentType.<BlockIngredientEntry>builder().persistent(BlockIngredientEntry.CODEC).networkSynchronized(BlockIngredientEntry.STREAM_CODEC).build());
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MatrixDataComponent>> MATRIX = DATA_COMPONENTS.register("matrix", x -> DataComponentType
            .<MatrixDataComponent>builder().persistent(MatrixDataComponent.CODEC).networkSynchronized(MatrixDataComponent.STREAM_CODEC).build());
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LittleElement>> ELEMENT = DATA_COMPONENTS.register("element", x -> DataComponentType
            .<LittleElement>builder().persistent(LittleElement.CODEC).networkSynchronized(LittleElement.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LittleShapeInstance>> SHAPE = DATA_COMPONENTS.register("shape", x -> DataComponentType
            .<LittleShapeInstance>builder().persistent(LittleShapeInstance.CODEC).networkSynchronized(LittleShapeInstance.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TileFilterComponent>> FILTER = DATA_COMPONENTS.register("filter", x -> DataComponentType
            .<TileFilterComponent>builder().persistent(TileFilterComponent.CODEC).networkSynchronized(TileFilterComponent.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SelectionComponent>> SELECTION = DATA_COMPONENTS.register("selection", x -> DataComponentType
            .<SelectionComponent>builder().persistent(SelectionComponent.CODEC).networkSynchronized(SelectionComponent.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MeasurementsComponent>> MEASUREMENTS = DATA_COMPONENTS.register("measurements",
        x -> DataComponentType.<MeasurementsComponent>builder().persistent(MeasurementsComponent.CODEC).networkSynchronized(MeasurementsComponent.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MeasurementTypeComponent>> MEASUREMENT_TYPE = DATA_COMPONENTS.register("measurement_type",
        x -> DataComponentType.<MeasurementTypeComponent>builder().persistent(MeasurementTypeComponent.CODEC).networkSynchronized(MeasurementTypeComponent.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TransformMode>> TRANSFORM_MODE = DATA_COMPONENTS.register("transform_mode", x -> DataComponentType
            .<TransformMode>builder().persistent(TransformMode.CODEC).networkSynchronized(TransformMode.STREAM_CODEC).build());
    
    // BLOCKS
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, LittleTiles.MODID);
    
    public static final Holder<Block> BLOCK_TILES = BLOCKS.register("tiles", () -> new BlockTile(blockProps("tiles"), false, false));
    public static final Holder<Block> BLOCK_TILES_TICKING = BLOCKS.register("tiles_ticking", () -> new BlockTile(blockProps("tiles_ticking"), true, false));
    public static final Holder<Block> BLOCK_TILES_RENDERED = BLOCKS.register("tiles_rendered", () -> new BlockTile(blockProps("tiles_rendered"), false, true));
    public static final Holder<Block> BLOCK_TILES_TICKING_RENDERED = BLOCKS.register("tiles_ticking_rendered", () -> new BlockTile(blockProps("tiles_ticking_rendered"), true, true));
    
    public static final Holder<Block> CLEAN = register("colored_clean", () -> new Block(blockProps("colored_clean").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> FLOOR = register("colored_floor", () -> new Block(blockProps("colored_floor").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> GRAINY_BIG = register("colored_grainy_big", () -> new Block(blockProps("colored_grainy_big").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> GRAINY = register("colored_grainy", () -> new Block(blockProps("colored_grainy").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> GRAINY_LOW = register("colored_grainy_low", () -> new Block(blockProps("colored_grainy_low").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> BRICK = register("colored_brick", () -> new Block(blockProps("colored_brick").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> BRICK_BIG = register("colored_brick_big", () -> new Block(blockProps("colored_brick_big").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> BORDERED = register("colored_bordered", () -> new Block(blockProps("colored_bordered").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> CHISELED = register("colored_chiseled", () -> new Block(blockProps("colored_chiseled").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> BROKEN_BRICK_BIG = register("colored_broken_brick_big", () -> new Block(blockProps("colored_broken_brick_big").strength(1.5F, 6.0F).mapColor(
        MapColor.SNOW)));
    public static final Holder<Block> CLAY = register("colored_clay", () -> new Block(blockProps("colored_clay").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> STRIPS = register("colored_strips", () -> new Block(blockProps("colored_strips").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> GRAVEL = register("colored_gravel", () -> new Block(blockProps("colored_gravel").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> SAND = register("colored_sand", () -> new Block(blockProps("colored_sand").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> STONE = register("colored_stone", () -> new Block(blockProps("colored_stone").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> CORK = register("colored_cork", () -> new Block(blockProps("colored_cork").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    
    public static final Holder<Block> WATER = register("colored_water", () -> new BlockWater(blockProps("colored_water").strength(1.5F, 6.0F).mapColor(MapColor.COLOR_BLUE)
            .noCollision()));
    
    public static final Holder<Block> LAVA = register("colored_lava", () -> new BlockLava(blockProps("colored_lava").strength(1.5F, 6.0F).mapColor(MapColor.COLOR_RED)
            .noCollision()));
    public static final Holder<Block> WHITE_LAVA = register("colored_white_lava", () -> new BlockLava(blockProps("colored_white_lava").strength(1.5F, 6.0F).mapColor(MapColor.SNOW)
            .noCollision().lightLevel(x -> 15)));
    
    public static final Holder<Block> STORAGE_BLOCK = register("storage", () -> new Block(blockProps("storage").sound(SoundType.WOOD).destroyTime(1.5F).strength(1.5F)));
    
    public static final Holder<Block> FLOWING_WATER = BLOCKS.register("colored_water_flowing", () -> new BlockFlowingWater(blockProps("colored_water_flowing"), WATER));
    
    public static final Holder<Block> FLOWING_LAVA = BLOCKS.register("colored_lava_flowing", () -> new BlockFlowingLava(blockProps("colored_lava_flowing"), LAVA));
    public static final Holder<Block> WHITE_FLOWING_LAVA = BLOCKS.register("colored_white_lava_flowing", () -> new BlockFlowingLava(blockProps("colored_white_lava_flowing"), WHITE_LAVA));
    
    public static final Holder<Block> SINGLE_CABLE = BLOCKS.register("cable_single", () -> new RotatedPillarBlock(blockProps("cable_single").strength(1.5F, 6.0F).sound(
        SoundType.WOOD)));
    
    public static final Holder<Block> INPUT_ARROW = BLOCKS.register("arrow_input", () -> new BlockArrow(blockProps("arrow_input")));
    public static final Holder<Block> OUTPUT_ARROW = BLOCKS.register("arrow_output", () -> new BlockArrow(blockProps("arrow_output")));
    
    public static final Holder<Block> SIGNAL_CONVERTER = register("signal_converter", () -> new BlockSignalConverter(blockProps("signal_converter")));
    
    public static final Holder<Block> MISSING = register("missing", () -> new Block(blockProps("missing")));
    
    /** 26.x: every item/block needs its registry id in its properties before construction. */
    private static Item.Properties itemProps(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(LittleTiles.MODID, name)));
    }
    
    private static BlockBehaviour.Properties blockProps(String name) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(LittleTiles.MODID, name)));
    }
    
    private static <T extends Block> DeferredHolder<Block, ? extends T> register(String name, Supplier<? extends T> sup) {
        var ret = BLOCKS.register(name, sup);
        ITEMS.register(name, () -> new BlockItem(ret.value(), itemProps(name).useBlockDescriptionPrefix()));
        return ret;
    }
    
    // BLOCK-TAGS
    
    public static final TagKey<Block> STORAGE_BLOCKS_TAG = BlockTags.create(Identifier.tryBuild(LittleTiles.MODID, "storage_blocks"));
    public static final TagKey<Block> INCOMPATIBLE_TAG = BlockTags.create(Identifier.tryBuild(LittleTiles.MODID, "incompatible"));
    
    // BLOCK_ENTITY
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LittleTiles.MODID);
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BETiles>> BE_TILES_TYPE = registerBlockEntity("tiles", () -> new BlockEntityType<>(BETiles::new,
        BLOCK_TILES.value(), BLOCK_TILES_TICKING.value()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BETilesRendered>> BE_TILES_TYPE_RENDERED = registerBlockEntity("tiles_rendered",
        () -> new BlockEntityType<>(BETilesRendered::new, BLOCK_TILES_RENDERED.value(), BLOCK_TILES_TICKING_RENDERED.value()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BESignalConverter>> BE_SIGNALCONVERTER_TYPE = registerBlockEntity("converter",
        () -> new BlockEntityType<>(BESignalConverter::new, SIGNAL_CONVERTER.value()));
    
    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(String name, Supplier<BlockEntityType<T>> sup) {
        return BLOCK_ENTITIES.register(name, sup);
    }
    
    // ENTITIES
    
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, LittleTiles.MODID);
    
    public static final DeferredHolder<EntityType<?>, EntityType<PrimedSizedTnt>> SIZED_TNT_TYPE = ENTITIES.register("primed_size_tnt", () -> EntityType.Builder.<PrimedSizedTnt>of(
        PrimedSizedTnt::new, MobCategory.MISC).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(LittleTiles.MODID, "primed_size_tnt"))));
    public static final DeferredHolder<EntityType<?>, EntityType<EntitySit>> SIT_TYPE = ENTITIES.register("sit", () -> EntityType.Builder.<EntitySit>of(EntitySit::new,
        MobCategory.MISC).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(LittleTiles.MODID, "sit"))));
    
    public static final DeferredHolder<EntityType<?>, EntityType<LittleAnimationEntity>> ENTITY_ANIMATION = ENTITIES.register("litte_animation", () -> EntityType.Builder
            .<LittleAnimationEntity>of(LittleAnimationEntity::new, MobCategory.MISC).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(LittleTiles.MODID, "litte_animation"))));
    public static final DeferredHolder<EntityType<?>, EntityType<LittleLevelEntity>> ENTITY_LEVEL = ENTITIES.register("little_level", () -> EntityType.Builder
            .<LittleLevelEntity>of(LittleLevelEntity::new, MobCategory.MISC).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(LittleTiles.MODID, "little_level"))));
    
    // DIMENSION
    
    public static final ResourceKey FAKE_DIMENSION = ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.tryBuild(LittleTiles.MODID, "fake"));
    
    // Recipe Serializer
    
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, LittleTiles.MODID);
    
    // Premade structure recipes are plain vanilla shaped/shapeless recipes now: the structure id sits in the result's littletiles:tool_config component.
    
    // CREATIVE_TAB
    
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LittleTiles.MODID);
    
    public static final Holder<CreativeModeTab> TAB = CREATIVE_TABS.register("items", () -> CreativeModeTab.builder().icon(() -> new ItemStack(LittleTilesRegistry.HAMMER.value()))
            .title(Component.translatable("creative_tab.littletiles")).displayItems((features, output) -> {
                ItemMultiTiles.collectExamples(output);
                
                for (LittlePremadeType entry : LittlePremadeRegistry.types())
                    if (entry.showInCreativeTab && !entry.hasCustomTab())
                        output.accept(entry.createItemStack());
                    
                output.accept(LittleTilesRegistry.HAMMER.value());
                output.accept(LittleTilesRegistry.CHISEL.value());
                output.accept(LittleTilesRegistry.BLUEPRINT.value());
                
                output.accept(LittleTilesRegistry.BAG.value());
                output.accept(LittleTilesRegistry.GLOVE.value());
                
                output.accept(LittleTilesRegistry.PAINT_BRUSH.value());
                output.accept(LittleTilesRegistry.MEASURE_TAPE.value());
                output.accept(LittleTilesRegistry.SCREWDRIVER.value());
                output.accept(LittleTilesRegistry.WRENCH.value());
                
                output.accept(LittleTilesRegistry.SIGNAL_CONVERTER.value());
                output.accept(LittleTilesRegistry.STORAGE_BLOCK.value());
                
                output.accept(LittleTilesRegistry.CLEAN.value());
                output.accept(LittleTilesRegistry.FLOOR.value());
                output.accept(LittleTilesRegistry.GRAINY_BIG.value());
                output.accept(LittleTilesRegistry.GRAINY.value());
                output.accept(LittleTilesRegistry.GRAINY_LOW.value());
                output.accept(LittleTilesRegistry.BRICK.value());
                output.accept(LittleTilesRegistry.BRICK_BIG.value());
                output.accept(LittleTilesRegistry.BORDERED.value());
                output.accept(LittleTilesRegistry.CHISELED.value());
                output.accept(LittleTilesRegistry.BROKEN_BRICK_BIG.value());
                output.accept(LittleTilesRegistry.CLAY.value());
                output.accept(LittleTilesRegistry.STRIPS.value());
                output.accept(LittleTilesRegistry.GRAVEL.value());
                output.accept(LittleTilesRegistry.SAND.value());
                output.accept(LittleTilesRegistry.STONE.value());
                output.accept(LittleTilesRegistry.CORK.value());
                
                output.accept(LittleTilesRegistry.WATER.value());
                
                output.accept(LittleTilesRegistry.LAVA.value());
                output.accept(LittleTilesRegistry.WHITE_LAVA.value());
                
            }).build());
    
    // INGREDIENT_TYPES
    
    /** 26.x dropped the vanilla CompoundTag entity data serializer; LittleTiles registers its own (used by EntitySit). */
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG_SERIALIZER = EntityDataSerializer.forValueType(ByteBufCodecs.COMPOUND_TAG);
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, LittleTiles.MODID);
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<CompoundTag>> COMPOUND_TAG_SERIALIZER_HOLDER = ENTITY_DATA_SERIALIZERS.register("compound_tag", () -> COMPOUND_TAG_SERIALIZER);
    
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, LittleTiles.MODID);
    
    public static final DeferredHolder<IngredientType<?>, IngredientType<StructureIngredient>> STRUCTURE_INGREDIENT_TYPE = INGREDIENT_TYPES.register("structure",
        () -> new IngredientType<>(StructureIngredient.CODEC));
    
}
