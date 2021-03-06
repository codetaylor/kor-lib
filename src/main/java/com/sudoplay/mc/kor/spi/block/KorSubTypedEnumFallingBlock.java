package com.sudoplay.mc.kor.spi.block;

import com.sudoplay.mc.kor.core.IntMap;
import com.sudoplay.mc.kor.spi.item.ISubType;
import com.sudoplay.mc.kor.spi.registry.KorOreDictionaryEntry;
import com.sudoplay.mc.kor.spi.registry.KorOreDictionaryEntryProvider;
import com.sudoplay.mc.kor.spi.registry.provider.KorClientInitStrategyProvider;
import com.sudoplay.mc.kor.spi.registry.provider.KorClientPreInitStrategyProvider;
import com.sudoplay.mc.kor.spi.registry.provider.KorPreInitStrategyProvider;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * Inspired by:
 * https://github.com/SlimeKnights/Mantle/blob/master/src/main/java/slimeknights/mantle/block/EnumBlock.java
 * <p>
 * Created by codetaylor on 1/25/2017.
 */
public class KorSubTypedEnumFallingBlock<E extends Enum<E> & ISubType & IStringSerializable>
    extends
    BlockFalling
    implements
    KorOreDictionaryEntryProvider,
    KorPreInitStrategyProvider.SubTypedBlock,
    KorClientPreInitStrategyProvider.SubTypedBlock,
    KorClientInitStrategyProvider.SubTypedBlock,
    IKorSubTypedEnumBlock<E> {

  private static PropertyEnum<?> TEMP_PROPERTY;

  private final PropertyEnum<E> property;

  /**
   * This map contains all enum values
   */
  private final IntMap<E> subTypeIntMap;

  public KorSubTypedEnumFallingBlock(
      String modId,
      String name,
      Material material,
      PropertyEnum<E> property,
      Class<E> enumClass
  ) {

    super(KorSubTypedEnumFallingBlock.hook(material, property));
    this.property = property;

    E[] enumConstants = enumClass.getEnumConstants();

    this.subTypeIntMap = new IntMap<>();

    for (int i = 0; i < enumConstants.length; i++) {
      this.subTypeIntMap.put(enumConstants[i].getMeta(), enumConstants[i]);
    }

    this.setUnlocalizedName(name);
    this.setRegistryName(modId, name);
  }

  private static Material hook(Material material, PropertyEnum<?> property) {

    TEMP_PROPERTY = property;
    return material;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> itemList) {

    for (ISubType subType : this.property.getAllowedValues()) {
      itemList.add(new ItemStack(this, 1, subType.getMeta()));
    }
  }

  @Nonnull
  @Override
  protected BlockStateContainer createBlockState() {

    if (this.property == null) {
      return new BlockStateContainer(this, TEMP_PROPERTY);

    } else {
      return new BlockStateContainer(this, this.property);
    }
  }

  @Nonnull
  @Override
  public IBlockState getStateFromMeta(int meta) {
    //noinspection unchecked
    return this.getDefaultState().withProperty(this.property, (E) getSubType(meta));
  }

  @Override
  public int getMetaFromState(IBlockState state) {

    return state.getValue(this.property).getMeta();
  }

  @Override
  public int damageDropped(IBlockState state) {

    return getMetaFromState(state);
  }

  @Nonnull
  @Override
  public ItemStack getPickBlock(
      @Nonnull IBlockState state,
      RayTraceResult target,
      @Nonnull World world,
      @Nonnull BlockPos pos,
      EntityPlayer player
  ) {

    Item itemFromBlock = Item.getItemFromBlock(this);
    assert itemFromBlock != null;
    return new ItemStack(itemFromBlock, 1, getMetaFromState(world.getBlockState(pos)));
  }

  @Override
  @Nonnull
  public List<KorOreDictionaryEntry> getKorOreDictionaryEntries(@Nonnull List<KorOreDictionaryEntry> store) {

    return store;
  }

  @Override
  public Collection<E> getSubTypes() {

    return this.property.getAllowedValues();
  }

  @Override
  public ISubType getSubType(int meta) {

    return this.subTypeIntMap.get(meta);
  }
}
