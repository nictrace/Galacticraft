package micdoodle8.mods.galacticraft.core.entities;

import java.util.Random;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TradePosition {
	protected Item item;
	protected int meta;
	protected int minPrice;
	protected int maxPrice;
	
	public TradePosition(Item itm, int min, int max, int meta) {
		this.item = itm;
		this.minPrice = min;
		this.maxPrice = max;
		this.meta = meta;
	}
	
	public TradePosition(Item itm, int min, int max) {
		this(itm, min, max, 0);
	}
	
	public ItemStack makeStack(Random rand) {
		return new ItemStack(this.item, rand.nextInt(this.maxPrice - this.minPrice)+this.minPrice, this.meta);
	}
}
