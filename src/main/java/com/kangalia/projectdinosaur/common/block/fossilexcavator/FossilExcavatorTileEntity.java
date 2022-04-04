package com.kangalia.projectdinosaur.common.block.fossilexcavator;

import com.kangalia.projectdinosaur.common.data.recipes.ExcavatingRecipe;
import com.kangalia.projectdinosaur.core.init.BlockInit;
import com.kangalia.projectdinosaur.core.init.ItemInit;
import com.kangalia.projectdinosaur.core.init.RecipeInit;
import com.kangalia.projectdinosaur.core.init.TileEntitiesInit;
import com.kangalia.projectdinosaur.core.util.RandomNumGen;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FossilExcavatorTileEntity extends TileEntity implements ITickableTileEntity {

    static final int WORK_TIME = 5 * 20;
    private int progress = 0;
    private int inputIndex;
    Inventory inventory;
    private final NonNullList<ItemStack> items;
    private final RandomNumGen rng = new RandomNumGen();
    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);

    public FossilExcavatorTileEntity() {
        super(TileEntitiesInit.FOSSIL_EXCAVATOR_ENTITY.get());
        this.items = NonNullList.withSize(13, ItemStack.EMPTY);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        itemHandler.deserializeNBT(nbt.getCompound("inv"));
        this.progress = nbt.getInt("progress");
        super.load(state, nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.put("inv", itemHandler.serializeNBT());
        compound.putInt("progress", this.progress);
        return super.save(compound);
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(13) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot >= 1 && slot < 7) {
                    return stack.getItem() == BlockInit.ENCASED_ALPINE_ROCK_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_AQUATIC_ROCK_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_ARID_ROCK_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_FROZEN_ROCK_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_GRASSLAND_ROCK_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_TEMPERATE_ROCK_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_TROPICAL_ROCK_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_WETLAND_ROCK_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_ALPINE_CRYSTALLISED_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_AQUATIC_CRYSTALLISED_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_ARID_CRYSTALLISED_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_FROZEN_CRYSTALLISED_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_GRASSLAND_CRYSTALLISED_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_TEMPERATE_CRYSTALLISED_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_TROPICAL_CRYSTALLISED_FOSSIL.get().asItem() ||
                            stack.getItem() == BlockInit.ENCASED_WETLAND_CRYSTALLISED_FOSSIL.get().asItem();
                }
                if (slot >= 7 && slot < 13) {
                    return stack.getItem() == ItemInit.ALPINE_ROCK_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.AQUATIC_ROCK_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.ARID_ROCK_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.FROZEN_ROCK_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.GRASSLAND_ROCK_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.TEMPERATE_ROCK_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.TROPICAL_ROCK_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.WETLAND_ROCK_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.ALPINE_CRYSTALLISED_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.AQUATIC_CRYSTALLISED_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.ARID_CRYSTALLISED_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.FROZEN_CRYSTALLISED_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.GRASSLAND_CRYSTALLISED_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.TEMPERATE_CRYSTALLISED_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.TROPICAL_CRYSTALLISED_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.WETLAND_CRYSTALLISED_SPECIMEN.get() ||
                            stack.getItem() == ItemInit.AMBER.get() ||
                            stack.getItem() == Items.BONE.getItem() ||
                            stack.getItem() == Items.CLAY_BALL.getItem() ||
                            stack.getItem() == Items.FLINT.getItem() ||
                            stack.getItem() == Items.SNOWBALL.getItem() ||
                            stack.getItem() == Items.COAL.getItem() ||
                            stack.getItem() == Blocks.CLAY.asItem() ||
                            stack.getItem() == Blocks.COBBLESTONE.asItem() ||
                            stack.getItem() == Blocks.GRAVEL.asItem() ||
                            stack.getItem() == Blocks.ICE.asItem() ||
                            stack.getItem() == Blocks.SAND.asItem();
                }
                if (slot == 0) {
                    return stack.getItem() == ItemInit.IRON_CHISEL.get() ||
                            stack.getItem() == ItemInit.DIAMOND_CHISEL.get() ||
                            stack.getItem() == ItemInit.NETHERITE_CHISEL.get();
                }
                return false;

            }
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, ItemStack stack , boolean simulate) {
                return(isItemValid(slot, stack)) ? super.insertItem(slot, stack, simulate) : stack;
            }

            //Hopper extraction code doesn't work. Needs to be worked on.
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot < 7) {
                    return super.extractItem(slot, amount, simulate);
                } else {
                    return (slot == 7 || slot == 8 || slot == 9 || slot == 10 || slot == 11 || slot == 12) ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }



    @Override
    public void tick() {
        if (this.level == null || level.isClientSide) {
            return;
        }
        if (this.canExcavate()) {
            if (progress < WORK_TIME) {
                ++progress;
                level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                setChanged();
            }
            if (progress == WORK_TIME) {
                progress = 0;
                this.doExcavate();
            }
        } else {
            progress = 0;
        }
        setChanged();
    }

    private boolean canExcavate() {
        int outputIndex = -1;
        this.inputIndex = -1;
        boolean flag = false;
        ItemStack inputSlot = ItemStack.EMPTY;
        ItemStack outputSlot = ItemStack.EMPTY;
        for (int slot = 1; slot < 7; slot++) {
            inputSlot = itemHandler.getStackInSlot(slot);
            if(!inputSlot.isEmpty()) {
                this.inputIndex = slot;
                ItemStack chiselSlot = itemHandler.getStackInSlot(0);
                if(!chiselSlot.isEmpty()) {
                    flag = true;
                    break;
                }
            }
        }
        if (inputIndex == -1 || !flag) {
            return false;
        } else {
            for (int slot = 7; slot < 13; slot++) {
                outputSlot = itemHandler.getStackInSlot(slot);
                if(outputSlot.isEmpty()) {
                    outputIndex = slot;
                    break;
                }
            }
            return outputIndex != -1 && this.inputIndex != -1;
        }
    }

    @Nullable
    public ExcavatingRecipe craft() {
        inventory = new Inventory(itemHandler.getSlots());
        for (int i = 0; i < 7; i++) {
            inventory.addItem(itemHandler.getStackInSlot(i));
            List<ExcavatingRecipe> recipes = level.getRecipeManager().getRecipesFor(RecipeInit.EXCAVATING_RECIPE, inventory, level);
            if (!recipes.isEmpty()) {
                ExcavatingRecipe selectedRecipe;
                if (recipes.size() == 1) {
                    selectedRecipe = recipes.get(0);
                } else {
                    int totalWeight = recipes.stream().map(r -> r.getWeight()).mapToInt(Integer::intValue).sum();
                    int[] weightArray = new int[totalWeight];
                    int pos = 0;
                    for (int j = 0; j < recipes.size(); j++) {
                        ExcavatingRecipe er = recipes.get(j);
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

            }
        }
        return null;
    }

    private ItemStack getOutput(@Nullable ExcavatingRecipe selectedRecipe) {
        if (selectedRecipe != null) {
            craft();
            return selectedRecipe.getResultItem();
        }
        return ItemStack.EMPTY;
    }

    public void doExcavate() {
        assert this.level != null;
        if (this.canExcavate()) {
            ExcavatingRecipe selectedRecipe = craft();
            ItemStack input = itemHandler.getStackInSlot(inputIndex);
            ItemStack output = getOutput(selectedRecipe);
            if (!output.isEmpty()) {
                for (int slot = 7; slot < 13; slot++) {
                    ItemStack stack = itemHandler.getStackInSlot(slot);
                    if (stack.isEmpty()) {
                        itemHandler.insertItem(slot, output, false);
                        input.shrink(1);
                        break;
                    } else {
                        if (ItemStack.isSame(stack, output) && stack.getCount() + 1 < 64) {
                            stack.grow(1);
                            input.shrink(1);
                            break;
                        }
                    }
                }
            }
        }
    }

    public int getProgress() {
        return progress;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT tags = this.getUpdateTag();
        ItemStackHelper.saveAllItems(tags, this.items);
        tags.putInt("progress", this.progress);
        return new SUpdateTileEntityPacket(this.worldPosition, 1, tags);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT tag = pkt.getTag();
        if (tag.contains("progress")) {
            progress = tag.getInt("progress");
            this.getTileData().putInt("progress", this.progress);
        }
    }
}