package com.kangalia.projectdinosaur.common.item;

import com.kangalia.projectdinosaur.common.entity.PrehistoricEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LovePotion extends Item {

    public LovePotion(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (pInteractionTarget instanceof PrehistoricEntity prehistoric) {
            prehistoric.setMatingTicks(0);
        }
        return InteractionResult.PASS;
    }
}
