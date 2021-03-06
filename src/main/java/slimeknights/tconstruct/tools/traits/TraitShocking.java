package slimeknights.tconstruct.tools.traits;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;

import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;

public class TraitShocking extends AbstractTrait {
  public TraitShocking() {
    super("shocking", 0xffffff);
  }

  @Override
  public float onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage, float newDamage, boolean isCritical) {
    if(player.worldObj.isRemote) {
      return super.onHit(tool, player, target, damage, newDamage, isCritical);
    }
    NBTTagCompound tag = TinkerUtil.getModifierTag(tool, identifier);
    Data data = Data.read(tag);
    if(data.charge >= 100f) {
      if(attackEntitySecondary(new EntityDamageSource("lightningBolt", player), 5f, target, false, true, false)) {
        //player.worldObj.playSoundEffect(target.posX, target.posY, target.posZ, "ambient.weather.thunder", 10000f, 1f);
        //player.worldObj.playSoundEffect(player.posX, player.posY, player.posZ, "ambient.weather.thunder", 10000.0F, 0.9F);
        //player.playSound(Sounds.shocking_discharge, 1f, 0.8f + random.nextFloat()*0.2f);
        if(player instanceof EntityPlayerMP) {
          ((EntityPlayerMP) player).worldObj.playSoundEffect(player.posX, player.posY, player.posZ, Sounds.shocking_discharge, 2f, 1f);
          //((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S29PacketSoundEffect(Sounds.shocking_discharge, player.posX, player.posY, player.posZ, 1f, 0.8f + 0.2f*random.nextFloat()));
        }
        data.charge = 0;

        NBTTagList tagList = TagUtil.getModifiersTagList(tool);
        int index = TinkerUtil.getIndexInCompoundList(tagList, identifier);
        data.write(tag);
        tagList.set(index, tag);
        TagUtil.setModifiersTagList(tool, tagList);
        TagUtil.setEnchantEffect(tool, false);
      }
    }
    return super.onHit(tool, player, target, damage, newDamage, isCritical);
  }

  @Override
  public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {
    if(!isSelected || world.isRemote) {
      return;
    }
    NBTTagList tagList = TagUtil.getModifiersTagList(tool);
    int index = TinkerUtil.getIndexInCompoundList(tagList, identifier);
    NBTTagCompound tag = tagList.getCompoundTagAt(index);

    Data data = Data.read(tag);

    // fully charged
    if(data.charge >= 100) {
      return;
    }

    // how far did we move?
    double dx = entity.posX - data.x;
    double dy = entity.posY - data.y;
    double dz = entity.posZ - data.z;

    double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
    if(dist < 0.1f) {
      return;
    }
    data.charge += dist*2f;

    // play sound when fully charged
    if(data.charge >= 100f) {
      TagUtil.setEnchantEffect(tool, true);
      // send only to the player that is charged
      if(entity instanceof EntityPlayerMP) {
        ((EntityPlayerMP) entity).playerNetServerHandler.sendPacket(new S29PacketSoundEffect(Sounds.shocking_charged, entity.posX, entity.posY, entity.posZ, 1f, 0.8f + 0.2f*random.nextFloat()));
      }
    }

    data.x = entity.posX;
    data.y = entity.posY;
    data.z = entity.posZ;
    data.write(tag);

    tagList.set(index, tag);
    TagUtil.setModifiersTagList(tool, tagList);
  }

  public static class Data {
    float charge;
    double x;
    double y;
    double z;

    public static Data read(NBTTagCompound tag) {
      Data data = new Data();
      data.charge = tag.getFloat("charge");
      data.x = tag.getDouble("x");
      data.y = tag.getDouble("y");
      data.z = tag.getDouble("z");
      return data;
    }

    public void write(NBTTagCompound tag) {
      tag.setFloat("charge", charge);
      tag.setDouble("x", x);
      tag.setDouble("y", y);
      tag.setDouble("z", z);
    }
  }
}
