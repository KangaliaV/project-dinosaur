package com.kangalia.projectdinosaur.common.entity;

import com.kangalia.projectdinosaur.core.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class AphanerammaEntity extends TamableAnimal implements IAnimatable {
    
    private AnimationFactory factory = new AnimationFactory(this);

    public AphanerammaEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.moveControl = new AphanerammaEntity.AphanerammaMoveControl(this);
        this.lookControl = new AphanerammaEntity.AphanerammaLookControl(this, 20);
        this.maxUpStep = 1.0F;
    }

    public static AttributeSupplier.Builder setCustomAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH,6.0F)
                .add(Attributes.MOVEMENT_SPEED,0.2F)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0F);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (!(event.getLimbSwingAmount() > -0.05F && event.getLimbSwingAmount() < 0.05F)) {
            event.getController().setAnimation(new AnimationBuilder()
                    .addAnimation("animation.Aphaneramma.walk", true));
            event.getController().setAnimationSpeed(1.0);
        } else {
            event.getController().setAnimation(new AnimationBuilder()
                    .addAnimation("animation.Aphaneramma.idle", true));
            event.getController().setAnimationSpeed(1.0);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<AphanerammaEntity>(this, "controller", 4, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(3, new AphanerammaRandomStrollGoal(this, 1.0D, 200));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    protected int getExperienceReward(Player player) {
        return 1 + this.level.random.nextInt(4);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.15F, 1.0F);
    }

    @Override
    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public MobType getMobType() {
        return MobType.WATER;
    }

    protected PathNavigation createNavigation(Level level) {
        return new AphanerammaEntity.AphanerammaPathNavigation(this, level);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageableMob) {
        return EntityInit.APHANERAMMA.get().create(serverWorld);
    }

    public void travel(Vec3 p_149181_) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), p_149181_);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(p_149181_);
        }
    }

    class AphanerammaLookControl extends SmoothSwimmingLookControl {
        public AphanerammaLookControl(AphanerammaEntity p_149210_, int p_149211_) {
            super(p_149210_, p_149211_);
        }
        public void tick() {
            super.tick();
        }
    }

    static class AphanerammaMoveControl extends SmoothSwimmingMoveControl {
        private final AphanerammaEntity aphaneramma;

        AphanerammaMoveControl(AphanerammaEntity aphanerammaEntity) {
            super(aphanerammaEntity, 85, 10, 0.1F, 0.5F, false);
            this.aphaneramma = aphanerammaEntity;
        }

        public void tick() {
            super.tick();
        }
    }

    static class AphanerammaPathNavigation extends WaterBoundPathNavigation {
        AphanerammaPathNavigation(AphanerammaEntity p_30294_, Level p_30295_) {
            super(p_30294_, p_30295_);
        }
        protected boolean canUpdatePath() {
            return true;
        }

        protected PathFinder createPathFinder(int p_149222_) {
            this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
            return new PathFinder(this.nodeEvaluator, p_149222_);
        }

        public boolean isStableDestination(BlockPos p_149224_) {
            return !this.level.getBlockState(p_149224_.below()).isAir();
        }
    }

    static class AphanerammaRandomStrollGoal extends RandomStrollGoal {
        private final AphanerammaEntity aphaneramma;
        private final boolean checkNoActionTime;

        public AphanerammaRandomStrollGoal(AphanerammaEntity entity, double pSpeedModifier, int pInterval) {
            this(entity, pSpeedModifier, pInterval, true);
        }

        public AphanerammaRandomStrollGoal(AphanerammaEntity entity, double pSpeedModifier, int pInterval, boolean pCheckNoActionTime) {
            super(entity, pSpeedModifier, pInterval);
            this.aphaneramma = entity;
            this.checkNoActionTime = pCheckNoActionTime;
        }

        @Override
        public boolean canUse() {
            if (this.mob.isVehicle()) {
                return false;
            } else {
                if (!this.forceTrigger) {
                    if (this.checkNoActionTime && this.mob.getNoActionTime() >= 100) {
                        return false;
                    }

                    if (this.mob.getRandom().nextInt(reducedTickDelay(this.interval)) != 0) {
                        return false;
                    }
                }
                Vec3 vec3;
                if(aphaneramma.isInWater()) {
                    vec3 = this.getSwimmablePosition();
                } else {
                    vec3 = this.getPosition();
                }
                if (vec3 == null) {
                    return false;
                } else {
                    this.wantedX = vec3.x;
                    this.wantedY = vec3.y;
                    this.wantedZ = vec3.z;
                    this.forceTrigger = false;
                    return true;
                }
            }
        }

        @Override
        public void start() {
            if (this.aphaneramma.isInWater()) {
                getPosition();
                this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier * 2);
            } else {
                this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
            }
        }
        @Override
        @javax.annotation.Nullable
        protected Vec3 getPosition() {
            return DefaultRandomPos.getPos(this.mob, 10, 7);
        }

        protected Vec3 getSwimmablePosition() {
            return BehaviorUtils.getRandomSwimmablePos(this.mob, 10, 7);
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
            super.stop();
        }
    }
}
