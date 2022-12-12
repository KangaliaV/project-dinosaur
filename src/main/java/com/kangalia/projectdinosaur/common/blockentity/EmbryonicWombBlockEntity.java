package com.kangalia.projectdinosaur.common.blockentity;

import com.kangalia.projectdinosaur.common.entity.creature.AphanerammaEntity;
import com.kangalia.projectdinosaur.core.data.recipes.GrowingRecipe;
import com.kangalia.projectdinosaur.core.init.BlockEntitiesInit;
import com.kangalia.projectdinosaur.core.init.BlockInit;
import com.kangalia.projectdinosaur.core.init.ItemInit;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class EmbryonicWombBlockEntity extends BlockEntity implements IAnimatable {

    static final int WORK_TIME = 300 * 20;
    private int progress = 0;
    SimpleContainer inventory;
    private final NonNullList<ItemStack> items;
    private final RandomNumGen rng = new RandomNumGen();
    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public EmbryonicWombBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntitiesInit.EMBRYONIC_WOMB_ENTITY.get(), blockPos, blockState);
        this.items = NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        handler.invalidate();
    }

    @Override
    public void load(CompoundTag nbt) {
        itemHandler.deserializeNBT(nbt.getCompound("inv"));
        this.progress = nbt.getInt("progress");
        super.load(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.put("inv", itemHandler.serializeNBT());
        nbt.putInt("progress", this.progress);
        super.saveAdditional(nbt);
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == 0) {
                    return stack.getItem() == ItemInit.DIRE_WOLF_EMBYRO.get() ||
                            stack.getItem() == ItemInit.DIRE_WOLF_FOETUS.get() ||
                            stack.getItem() == ItemInit.MEGALODON_EMBRYO.get() ||
                            stack.getItem() == ItemInit.MEGALODON_FOETUS.get() ||
                            stack.getItem() == ItemInit.ROTTEN_EGG.get();
                }
                if (slot == 1) {
                    return stack.getItem() == ItemInit.NUTRIENT_GEL.get();
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
                return super.extractItem(slot, amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != Direction.DOWN) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    public void tick() {
        if (this.level == null) {
            return;
        }
        BlockState blockState = level.getBlockState(worldPosition);
        if (this.canGrow()) {
            level.setBlock(worldPosition, blockState.setValue(BlockStateProperties.POWERED, true), Block.UPDATE_ALL);
            if (progress < WORK_TIME) {
                ++progress;
                level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                setChanged();
            }
            if (progress == WORK_TIME) {
                progress = 0;
                this.doGrow();
            }
        } else {
            level.setBlock(worldPosition, blockState.setValue(BlockStateProperties.POWERED, false), Block.UPDATE_ALL);
            progress = 0;
        }
        setChanged();
    }

    private boolean canGrow() {
        ItemStack inputSlot = ItemStack.EMPTY;
        inputSlot = itemHandler.getStackInSlot(0);
        boolean flag;
        if (!inputSlot.isEmpty() && inputSlot.getItem() == ItemInit.AUSTRALOVENATOR_EGG_FERTILISED.get()) {
            flag = true;
        } else if (!inputSlot.isEmpty() && inputSlot.getItem() == ItemInit.SCELIDOSAURUS_EGG_FERTILISED.get()) {
            flag = true;
        } else {
            flag = false;
        }
        if (flag) {
            ItemStack haySlot = itemHandler.getStackInSlot(1);
            if (!haySlot.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public GrowingRecipe craft() {
        inventory = new SimpleContainer(itemHandler.getSlots());
        inventory.addItem(itemHandler.getStackInSlot(0));
        inventory.addItem(itemHandler.getStackInSlot(1));
        List<GrowingRecipe> recipes = level.getRecipeManager().getRecipesFor(GrowingRecipe.GrowingRecipeType.INSTANCE, inventory, level);
        if (!recipes.isEmpty()) {
            GrowingRecipe selectedRecipe;
            if (recipes.size() == 1) {
                selectedRecipe = recipes.get(0);
            } else {
                int totalWeight = recipes.stream().map(r -> r.getWeight()).mapToInt(Integer::intValue).sum();
                int[] weightArray = new int[totalWeight];
                int pos = 0;
                for (int j = 0; j < recipes.size(); j++) {
                    GrowingRecipe er = recipes.get(j);
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
        return null;
    }

    private ItemStack getOutput(@Nullable GrowingRecipe selectedRecipe) {
        if (selectedRecipe != null) {
            return selectedRecipe.getResultItem();
        }
        return ItemStack.EMPTY;
    }

    public void doGrow() {
        assert this.level != null;
        ItemStack hay = itemHandler.getStackInSlot(1);
        if (this.canGrow()) {
            GrowingRecipe selectedRecipe = craft();
            ItemStack output = getOutput(selectedRecipe);
            if (!output.isEmpty()) {
                ItemStack stack = itemHandler.getStackInSlot(0);
                stack.shrink(1);
                itemHandler.insertItem(0, output, false);
            }
            hay.shrink(1);
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

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.embryonic_womb.embryo", ILoopType.EDefaultLoopTypes.LOOP));
        event.getController().setAnimationSpeed(0.5);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<EmbryonicWombBlockEntity>(this, "controller", 4, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
