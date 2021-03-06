package slimeknights.tconstruct.tools.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.util.Locale;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.tools.IAoeTool;

public class ModHarvestSize extends Modifier {

  public ModHarvestSize(String name) {
    super("harvest" + name.toLowerCase(Locale.US));

    addAspects(new ModifierAspect.SingleAspect(this), new ModifierAspect.DataAspect(this, 0xcaf6a2), ModifierAspect.harvestOnly, ModifierAspect.freeModifier);
  }

  @Override
  protected boolean canApplyCustom(ItemStack stack) throws TinkerGuiException {
    // we can only apply this to AOE tools
    if(!(stack.getItem() instanceof IAoeTool)) {
      return false;
    }

    return super.canApplyCustom(stack);
  }


  @Override
  public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {
    // no extra data needed
  }
}
