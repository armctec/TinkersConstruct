package slimeknights.tconstruct.tools.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.EntityUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;

public class FryPan extends ToolCore {

  public FryPan() {
    super(PartMaterialType.handle(TinkerTools.toolRod),
          PartMaterialType.head(TinkerTools.panHead));

    addCategory(Category.WEAPON);
  }

  @Override
  public boolean canUseSecondaryItem() {
    return false;
  }

  @Override
  public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int timeLeft) {
    if(world.isRemote)
      return;

    float progress = (float)(getMaxItemUseDuration(stack) - timeLeft)/30f;
    float strength = .1f + 1.5f*progress*progress;

    float range = 3.2f;

    // is the player currently looking at an entity?
    Vec3 eye = new Vec3(player.posX, player.posY + (double)player.getEyeHeight(), player.posZ); // Entity.getPositionEyes
    Vec3 look = player.getLook(1.0f);
    MovingObjectPosition mop = EntityUtil.raytraceEntity(player, eye, look, range, true);

    // nothing hit :(
    if(mop == null) {
      return;
    }

    // we hit something. let it FLYYYYYYYYY
    if(mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
      Entity entity = mop.entityHit;
      double x = look.xCoord * strength;
      double y = look.yCoord/3f * strength + 0.1f + 0.4f * progress;
      double z = look.zCoord * strength;

      // bonus damage!
      AttributeModifier modifier = new AttributeModifier(itemModifierUUID, "Weapon modifier", progress * 5f, 0);


      // we set the entity on fire for the hit if it was fully charged
      // this makes it so it drops cooked stuff.. and it'funny :D
      boolean flamingStrike = progress >= 1f && entity.isBurning();
      if(!flamingStrike) entity.setFire(1);
      player.getEntityAttribute(SharedMonsterAttributes.attackDamage).applyModifier(modifier);
      ToolHelper.attackEntity(stack, this, player, entity);
      player.getEntityAttribute(SharedMonsterAttributes.attackDamage).removeModifier(modifier);
      if(!flamingStrike) entity.extinguish();

      player.worldObj.playSoundAtEntity(player, Sounds.frypan_boing, 1.5f, 0.6f + 0.2f * TConstruct.random.nextFloat());
      entity.addVelocity(x,y,z);
      if(entity instanceof EntityPlayerMP) {
        ((EntityPlayerMP)entity).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(entity));
      }
    }
  }

  @Override
  public boolean dealDamage(ItemStack stack, EntityPlayer player, EntityLivingBase entity, float damage) {
    boolean hit = super.dealDamage(stack, player, entity, damage);
    if(hit) {
      player.worldObj.playSoundAtEntity(player, Sounds.frypan_boing, 1.2f,
                                          0.8f + 0.4f * TConstruct.random.nextFloat());
    }
    return hit;
  }

  /**
   * Called when the itemUseDuration runs out
   */
  @Override
  public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityPlayer playerIn) {
    // in our case we want it to also go BANG :D
    //onPlayerStoppedUsing(stack, worldIn, playerIn, 0);
    return stack;
  }

  @Override
  public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
    // has to be done in onUpdate because onTickUsing is too early and gets overwritten. bleh.
    preventSlowDown(entityIn, 0.7f);

    super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
  }

  /**
   * How long it takes to use or consume an item
   */
  public int getMaxItemUseDuration(ItemStack stack)
  {
    return 5 * 20;
  }

  /**
   * returns the action that specifies what animation to play when the items is being used
   */
  public EnumAction getItemUseAction(ItemStack stack)
  {
    return EnumAction.BOW;
  }

  /**
   * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
   */
  public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
  {
    playerIn.setItemInUse(itemStackIn, this.getMaxItemUseDuration(itemStackIn));
    return itemStackIn;
  }

  @Override
  public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
    return super.onItemUseFirst(stack, player, world, pos, side, hitX, hitY, hitZ);
  }

  @Override
  public float damagePotential() {
    return 1.0f;
  }

  @Override
  public float knockback() {
    return 2f;
  }

  @Override
  public NBTTagCompound buildTag(List<Material> materials) {
    return buildDefaultTag(materials).get();
  }
}
