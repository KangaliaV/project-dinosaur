package com.kangalia.projectdinosaur.core.init;

import com.kangalia.projectdinosaur.ProjectDinosaur;
import com.kangalia.projectdinosaur.common.entity.PetrifiedBoatEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {

    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, ProjectDinosaur.MODID);

    public static final RegistryObject<EntityType<PetrifiedBoatEntity>> PETRIFIED_BOAT = ENTITY_TYPES.register("petrified_boat",
            () -> EntityType.Builder.<PetrifiedBoatEntity>of(PetrifiedBoatEntity::new, MobCategory.MISC).sized(0.5f, 0.5f)
                    .build(new ResourceLocation(ProjectDinosaur.MODID, "petrified_boat").toString()));
}
