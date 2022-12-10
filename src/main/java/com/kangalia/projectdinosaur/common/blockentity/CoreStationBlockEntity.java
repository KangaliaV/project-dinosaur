package com.kangalia.projectdinosaur.common.blockentity;

import com.kangalia.projectdinosaur.core.data.recipes.ExtractingRecipe;
import com.kangalia.projectdinosaur.core.init.BlockEntitiesInit;
import com.kangalia.projectdinosaur.core.init.ItemInit;
import com.kangalia.projectdinosaur.core.util.OutputStackHandler;
import com.kangalia.projectdinosaur.core.util.RandomNumGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CoreStationBlockEntity extends BlockEntity {

    static final int WORK_TIME = 5 * 20;
    private int progress = 0;
    private int inputIndex;
    SimpleContainer inventory;
    private final NonNullList<ItemStack> items;
    private final RandomNumGen rng = new RandomNumGen();

    protected ItemStackHandler inputs = createInputHandler();
    protected ItemStackHandler outputs;
    protected ItemStackHandler outputWrapper;

    private final LazyOptional<IItemHandler> inputHandler = LazyOptional.of(() -> inputs);
    private final LazyOptional<IItemHandler> outputWrapperHandler = LazyOptional.of(() -> outputWrapper);
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> new CombinedInvWrapper(inputs, outputWrapper));

    public CoreStationBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntitiesInit.CORE_STATION_ENTITY.get(), blockPos, blockState);
        this.items = NonNullList.withSize(13, ItemStack.EMPTY);
        outputs = new ItemStackHandler(6);
        outputWrapper = new OutputStackHandler(outputs);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        handler.invalidate();
    }

    @Override
    public void load(CompoundTag nbt) {
        inputs.deserializeNBT(nbt.getCompound("inputs"));
        outputs.deserializeNBT(nbt.getCompound("outputs"));
        this.progress = nbt.getInt("progress");
        super.load(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.put("inputs", inputs.serializeNBT());
        nbt.put("outputs", outputs.serializeNBT());
        nbt.putInt("progress", this.progress);
        super.saveAdditional(nbt);
    }

    private ItemStackHandler createInputHandler() {
        return new ItemStackHandler(7) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot >= 1 && slot < 7) {
                    return stack.getItem() == ItemInit.QUATERNARY_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.NEOGENE_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.PALEOGENE_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.CRETACEOUS_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.JURASSIC_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.TRIASSIC_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.PERMIAN_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.CARBONIFEROUS_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.DEVONIAN_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.SILURIAN_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.ORDOVICIAN_FOSSIL_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.CAMBRIAN_FOSSIL_SPECIMEN.get();
                }
                if (slot == 0) {
                    return stack.getItem() == ItemInit.SYRINGE.get();
                }
                return false;

            }
            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            this.setChanged();
            if(level != null && level.getBlockState(getBlockPos()).getBlock() != this.getBlockState().getBlock()) {
                return handler.cast();
            }
            if (side == null) {
                return handler.cast();
            }
            if (level == null) {
                if (side == Direction.UP) {
                    return inputHandler.cast();
                }
                if (side == Direction.DOWN) {
                    return outputWrapperHandler.cast();
                }
            }
            if (side == Direction.UP) {
                return inputHandler.cast();
            }
            if (side == Direction.DOWN) {
                return outputWrapperHandler.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    public void tick() {
        if (this.level == null) {
            return;
        }
        BlockState blockState = level.getBlockState(worldPosition);
        if (this.canExtract()) {
            level.setBlock(worldPosition, blockState.setValue(BlockStateProperties.POWERED, true), Block.UPDATE_ALL);
            if (progress < WORK_TIME) {
                ++progress;
                level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                setChanged();
            }
            if (progress == WORK_TIME) {
                progress = 0;
                this.doExtract();
            }
        } else {
            level.setBlock(worldPosition, blockState.setValue(BlockStateProperties.POWERED, false), Block.UPDATE_ALL);
            progress = 0;
        }
        setChanged();
    }

    private boolean canExtract() {
        int outputIndex = -1;
        this.inputIndex = -1;
        boolean flag = false;
        ItemStack inputSlot = ItemStack.EMPTY;
        ItemStack outputSlot = ItemStack.EMPTY;
        for (int slot = 1; slot < 7; slot++) {
            inputSlot = inputs.getStackInSlot(slot);
            if(!inputSlot.isEmpty()) {
                this.inputIndex = slot;
                ItemStack syringeSlot = inputs.getStackInSlot(0);
                if(!syringeSlot.isEmpty()) {
                    flag = true;
                    break;
                }
            }
        }
        if (inputIndex == -1 || !flag) {
            return false;
        } else {
            for (int slot = 0; slot < 6; slot++) {
                outputSlot = outputs.getStackInSlot(slot);
                if(outputSlot.isEmpty()) {
                    outputIndex = slot;
                    break;
                }
            }
            return outputIndex != -1 && this.inputIndex != -1;
        }
    }

    @Nullable
    public ExtractingRecipe craft() {
        inventory = new SimpleContainer(inputs.getSlots());
        for (int i = 0; i < 7; i++) {
            inventory.addItem(inputs.getStackInSlot(i));
            List<ExtractingRecipe> recipes = level.getRecipeManager().getRecipesFor(ExtractingRecipe.ExtractingRecipeType.INSTANCE, inventory, level);
            if (!recipes.isEmpty()) {
                ExtractingRecipe selectedRecipe;
                if (recipes.size() == 1) {
                    selectedRecipe = recipes.get(0);
                } else {
                    int totalWeight = recipes.stream().map(r -> r.getWeight()).mapToInt(Integer::intValue).sum();
                    int[] weightArray = new int[totalWeight];
                    int pos = 0;
                    for (int j = 0; j < recipes.size(); j++) {
                        ExtractingRecipe er = recipes.get(j);
                        int weight = er.getWeight();
                        for (int k = 0; k < weight; k++) {
                            weightArray[pos] = j;
                            pos++;
                        }
                    }
                    int randomNum = rng.nextInt(weightArray.length);
                    int recipeIndex = weightArray[randomNum];
                    inventory.removeAllItems();
                    return recipes.get(recipeIndex);
                }
            return selectedRecipe;
            }
        }
        return null;
    }

    private ItemStack getOutput(@Nullable ExtractingRecipe selectedRecipe) {
        if (selectedRecipe != null) {
            craft();
            return selectedRecipe.getResultItem();
        }
        return ItemStack.EMPTY;
    }

    public void doExtract() {
        assert this.level != null;
        if (this.canExtract()) {
            ExtractingRecipe selectedRecipe = craft();
            ItemStack input = inputs.getStackInSlot(inputIndex);
            ItemStack syringe = inputs.getStackInSlot(0);
            ItemStack output = getOutput(selectedRecipe);
            if (!output.isEmpty()) {
                for (int slot = 0; slot < 6; slot++) {
                    ItemStack stack = outputs.getStackInSlot(slot);
                    if (stack.isEmpty()) {
                        outputs.insertItem(slot, output, false);
                        input.shrink(1);
                        syringe.shrink(1);
                        break;
                    } else {
                        if (ItemStack.isSame(stack, output) && stack.getCount() + 1 < 64) {
                            stack.grow(1);
                            input.shrink(1);
                            syringe.shrink(1);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag tags = this.getUpdateTag();
        ContainerHelper.saveAllItems(tags, this.items);
        tags.putInt("progress", this.progress);
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag.contains("progress")) {
            progress = tag.getInt("progress");
            this.getPersistentData().putInt("progress", this.progress);
        }
    }
}
