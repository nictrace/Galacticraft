package micdoodle8.mods.galacticraft.core.entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import micdoodle8.mods.galacticraft.api.entity.IRocketType;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Tuple;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class EntityAlienVillager extends EntityAgeable implements IEntityBreathable, IMerchant
{
    private int randomTickDivider;
    private boolean isMating;
    private boolean isPlaying;
    @Nullable
    private Village villageObj;
    @Nullable
    private EntityPlayer buyingPlayer;
    private MerchantRecipeList buyingList;
    private int wealth;
    private boolean isLookingForHome;
    private float skill;
    private int timeUntilReset;
    /** Last player to trade with this villager, used for aggressivity. */
    private String lastBuyingPlayer;
    /** addDefaultEquipmentAndRecipies is called if this is true */
    private boolean needsInitilization;
    /** Selling list of Villagers items. */
    public static final Map<Item, Tuple> villagersSellingList = new HashMap<Item, Tuple>();
    public final static String GCMarsID = "GalacticraftMars";
    public final static String IC2ID = "IC2";

    public EntityAlienVillager(World par1World)
    {
        super(par1World);
        this.randomTickDivider = 0;
        this.isMating = false;
        this.isPlaying = false;
        this.villageObj = null;
        this.setSize(0.6F, 2.35F);
        this.setProfession(new Random().nextInt(4));
        this.getNavigator().setBreakDoors(true);
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(2, new EntityAIMoveIndoors(this));
        this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.3F));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 15.0F, 1.0F));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityAlienVillager.class, 15.0F, 0.05F));
        this.tasks.addTask(9, new EntityAIWander(this, 0.3F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 15.0F));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5D);
    }

    /**
     * Returns true if the newer Entity AI code should be run
     */
    @Override
    public boolean isAIEnabled()
    {
        return true;
    }

    /**
     * main AI tick function, replaces updateEntityActionState
     */
    @SuppressWarnings("rawtypes")
	@Override
    protected void updateAITick()
    {
        if (--this.randomTickDivider <= 0)
        {
            this.worldObj.villageCollectionObj.addVillagerPosition(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
            this.randomTickDivider = 70 + this.rand.nextInt(50);
            this.villageObj = this.worldObj.villageCollectionObj.findNearestVillage(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ), 32);

            if (this.villageObj == null)
            {
                this.detachHome();
            }
            else
            {
                ChunkCoordinates chunkcoordinates = this.villageObj.getCenter();
                this.setHomeArea(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, (int) (this.villageObj.getVillageRadius() * 0.6F));

                if (this.isLookingForHome)
                {
                    this.isLookingForHome = false;
                    this.villageObj.setDefaultPlayerReputation(5);
                }
            }
        }
        if (!this.isTrading() && this.timeUntilReset > 0)
        {
            --this.timeUntilReset;

            if (this.timeUntilReset <= 0)
            {
                if (this.needsInitilization)
                {
                    if (this.buyingList.size() > 1)
                    {
                        Iterator iterator = this.buyingList.iterator();

                        while (iterator.hasNext())
                        {
                            MerchantRecipe merchantrecipe = (MerchantRecipe)iterator.next();

                            if (merchantrecipe.isRecipeDisabled())
                            {
                                merchantrecipe.func_82783_a(this.rand.nextInt(6) + this.rand.nextInt(6) + 2);
                            }
                        }
                    }

                    this.addDefaultEquipmentAndRecipies(1);
                    this.needsInitilization = false;

                    if (this.villageObj != null && this.lastBuyingPlayer != null)
                    {
                        this.worldObj.setEntityState(this, (byte)14);
                        this.villageObj.setReputationForPlayer(this.lastBuyingPlayer, 1);
                    }
                }

                this.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
            }
        }
        super.updateAITick();
    }
    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean interact(EntityPlayer player)
    {
        ItemStack itemstack = player.inventory.getCurrentItem();
        boolean flag = itemstack != null && itemstack.getItem() == Items.spawn_egg;

        if (!flag && this.isEntityAlive() && !this.isTrading() && !this.isChild() && !player.isSneaking())
        {
            if (!this.worldObj.isRemote)
            {
                this.setCustomer(player);
                player.displayGUIMerchant(this, this.getCustomNameTag());
            }

            return true;
        }
        else
        {
            return super.interact(player);
        }
    }
    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Integer.valueOf(0));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("Profession", this.getProfession());
        par1NBTTagCompound.setInteger("Riches", this.wealth);

        if (this.buyingList != null)
        {
            par1NBTTagCompound.setTag("Offers", this.buyingList.getRecipiesAsTags());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.setProfession(par1NBTTagCompound.getInteger("Profession"));
        this.wealth = par1NBTTagCompound.getInteger("Riches");

        if (par1NBTTagCompound.hasKey("Offers"))
        {
            final NBTTagCompound nbttagcompound1 = par1NBTTagCompound.getCompoundTag("Offers");
            this.buyingList = new MerchantRecipeList(nbttagcompound1);
        }
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    protected boolean canDespawn()
    {
        return false;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected String getLivingSound()
    {
        return "mob.villager.idle";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound()
    {
        return "mob.villager.hit";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected String getDeathSound()
    {
        return "mob.villager.death";
    }

    public void setProfession(Integer par1)
    {
        this.dataWatcher.updateObject(16, par1); // 
    }

    public int getProfession()
    {
        return this.dataWatcher.getWatchableObjectInt(16);
    }

    public boolean isMating()
    {
        return this.isMating;
    }

    public void setMating(boolean par1)
    {
        this.isMating = par1;
    }

    public void setPlaying(boolean par1)
    {
        this.isPlaying = par1;
    }

    public boolean isPlaying()
    {
        return this.isPlaying;
    }

    @Override
    public void setRevengeTarget(EntityLivingBase par1EntityLiving)
    {
        super.setRevengeTarget(par1EntityLiving);

        if (this.villageObj != null && par1EntityLiving != null)
        {
            this.villageObj.addOrRenewAgressor(par1EntityLiving);

            if (par1EntityLiving instanceof EntityPlayer)
            {
                byte b0 = -1;

                if (this.isChild())
                {
                    b0 = -3;
                }

                this.villageObj.setReputationForPlayer(((EntityPlayer) par1EntityLiving).getCommandSenderName(), b0);

                if (this.isEntityAlive())
                {
                    this.worldObj.setEntityState(this, (byte) 13);
                }
            }
        }
    }

    /**
     * Called when the mob's health reaches 0.
     */
    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        if (this.villageObj != null)
        {
            final Entity entity = par1DamageSource.getEntity();

            if (entity != null)
            {
                if (entity instanceof EntityPlayer)
                {
                    this.villageObj.setReputationForPlayer(((EntityPlayer) entity).getCommandSenderName(), -2);
                }
                else if (entity instanceof IMob)
                {
                    this.villageObj.endMatingSeason();
                }
            }
            else if (entity == null)
            {
                final EntityPlayer entityplayer = this.worldObj.getClosestPlayerToEntity(this, 16.0D);

                if (entityplayer != null)
                {
                    this.villageObj.endMatingSeason();
                }
            }
        }

        super.onDeath(par1DamageSource);
    }

    public void setCustomer(EntityPlayer par1EntityPlayer)
    {
        this.buyingPlayer = par1EntityPlayer;
    }

    public EntityPlayer getCustomer()
    {
        return this.buyingPlayer;
    }

    public boolean isTrading()
    {
        return this.buyingPlayer != null;
    }

    public void useRecipe(MerchantRecipe par1MerchantRecipe)
    {
        par1MerchantRecipe.incrementToolUses();

        this.livingSoundTime = -this.getTalkInterval();
        this.playSound("mob.villager.yes", this.getSoundVolume(), this.getSoundPitch());

        if (par1MerchantRecipe.hasSameIDsAs((MerchantRecipe)this.buyingList.get(this.buyingList.size() - 1)))
        {
            this.timeUntilReset = 40;
            this.needsInitilization = true;

            if (this.buyingPlayer != null)
            {
                this.lastBuyingPlayer = this.buyingPlayer.getCommandSenderName();
            }
            else
            {
                this.lastBuyingPlayer = null;
            }
        }
        

        if (par1MerchantRecipe.getItemToBuy().getItem() == GCItems.meteoricIronRaw)
        {
            this.wealth += par1MerchantRecipe.getItemToBuy().stackSize;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleHealthUpdate(byte par1)
    {
        if (par1 == 12)
        {
            this.generateRandomParticles("heart");
        }
        else if (par1 == 13)
        {
            this.generateRandomParticles("angryVillager");
        }
        else if (par1 == 14)
        {
            this.generateRandomParticles("happyVillager");
        }
        else
        {
            super.handleHealthUpdate(par1);
        }
    }

    @SideOnly(Side.CLIENT)
    private void generateRandomParticles(String par1Str)
    {
        for (int i = 0; i < 5; ++i)
        {
            final double d0 = this.rand.nextGaussian() * 0.02D;
            final double d1 = this.rand.nextGaussian() * 0.02D;
            final double d2 = this.rand.nextGaussian() * 0.02D;
            this.worldObj.spawnParticle(par1Str, this.posX + this.rand.nextFloat() * this.width * 2.0F - this.width, this.posY + 1.0D + this.rand.nextFloat() * this.height, this.posZ + this.rand.nextFloat() * this.width * 2.0F - this.width, d0, d1, d2);
        }
    }
    
    public void setLookingForHome()
    {
        this.isLookingForHome = true;
    }

    // spawn a baby =)
    public EntityAlienVillager func_90012_b(EntityAgeable par1EntityAgeable)
    {
        return new EntityAlienVillager(this.worldObj);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable par1EntityAgeable)
    {
        return this.func_90012_b(par1EntityAgeable);
    }

    @Override
    public boolean canBreath()
    {
        return true;
    }
    /**
     * Adjusts the probability of obtaining a given recipe being offered by a villager
     */
    private float adjustProbability(float base)
    {
        float f1 = base + this.skill;
        return f1 > 0.9F ? 0.9F - (f1 - 0.9F) : f1;
    }
    /**
     * based on the villagers profession add items, equipment, and recipies adds par1 random items to the list of things
     * that the villager wants to buy. (at most 1 of each wanted type is added)
     */
    @SuppressWarnings("unchecked")
	private void addDefaultEquipmentAndRecipies(int itm_qty)
    {
    	String GCMarsID = "GalacticraftMars";
        if (this.buyingList != null) {
            this.skill = MathHelper.sqrt_float((float)this.buyingList.size()) * 0.2F;
        }
        else {
            this.skill = 0.0F;
        }

        MerchantRecipeList merchantrecipelist;
        merchantrecipelist = new MerchantRecipeList(); // local recipes
        switch (this.getProfession()) {
        case 0:	/* Rocketman */
            addSellRecipe(merchantrecipelist, GCItems.fuelCanister, this.rand, this.adjustProbability(0.5F));
            addSellRecipe(merchantrecipelist, GCItems.partNoseCone, this.rand, this.adjustProbability(0.3F));
            addSellRecipe(merchantrecipelist, GCItems.heavyPlatingTier1, this.rand, this.adjustProbability(0.9F));
            // buy rockets tier 1
            if (this.rand.nextFloat() < this.adjustProbability(0.07F)) {
                merchantrecipelist.add(new MerchantRecipe(new ItemStack(GCItems.rocketTier1, 1, IRocketType.EnumRocketType.INVENTORY54.ordinal()), new ItemStack(GCItems.meteoricIronRaw, 16, 0)));
                merchantrecipelist.add(new MerchantRecipe(new ItemStack(GCItems.rocketTier1, 1, IRocketType.EnumRocketType.INVENTORY36.ordinal()), new ItemStack(GCItems.meteoricIronRaw, 12, 0)));
                merchantrecipelist.add(new MerchantRecipe(new ItemStack(GCItems.rocketTier1, 1, IRocketType.EnumRocketType.INVENTORY27.ordinal()), new ItemStack(GCItems.meteoricIronRaw, 10, 0)));
                merchantrecipelist.add(new MerchantRecipe(new ItemStack(GCItems.rocketTier1, 1, IRocketType.EnumRocketType.DEFAULT.ordinal()), new ItemStack(GCItems.meteoricIronRaw, 8, 0)));                
            }
            if(Loader.isModLoaded(GCMarsID)) { // but how to do if not?
            	if (this.rand.nextFloat() < this.adjustProbability(0.07F)) {
                    merchantrecipelist.add(new MerchantRecipe(new ItemStack(GameRegistry.findItem(GCMarsID, "spaceshipTier2"),1,IRocketType.EnumRocketType.DEFAULT.ordinal()), new ItemStack(GCItems.meteoricIronRaw, 10, 0)));
                    merchantrecipelist.add(new MerchantRecipe(new ItemStack(GameRegistry.findItem(GCMarsID, "spaceshipTier2"),1,IRocketType.EnumRocketType.INVENTORY27.ordinal()), new ItemStack(GCItems.meteoricIronRaw, 12, 0)));
                    merchantrecipelist.add(new MerchantRecipe(new ItemStack(GameRegistry.findItem(GCMarsID, "spaceshipTier2"),1,IRocketType.EnumRocketType.INVENTORY36.ordinal()), new ItemStack(GCItems.meteoricIronRaw, 14, 0)));
                    merchantrecipelist.add(new MerchantRecipe(new ItemStack(GameRegistry.findItem(GCMarsID, "spaceshipTier2"),1,IRocketType.EnumRocketType.INVENTORY54.ordinal()), new ItemStack(GCItems.meteoricIronRaw, 20, 0)));
                    // Tier3?
            	}
            }
        	break;
        case 1: /* Powermaster */
            addSellRecipe(merchantrecipelist, GCItems.sensorGlasses, this.rand, this.adjustProbability(0.2F));
            addSellRecipe(merchantrecipelist, Item.getItemFromBlock(GCBlocks.glowstoneTorch), this.rand, this.adjustProbability(0.9F));
            addSellRecipe(merchantrecipelist, GCItems.basicItem, this.rand, this.adjustProbability(0.3F));
            addSellRecipe(merchantrecipelist, Item.getItemFromBlock(GCBlocks.solarPanel), this.rand, this.adjustProbability(0.3F));
            addSellRecipe(merchantrecipelist, Item.getItemFromBlock(Blocks.glowstone), this.rand, this.adjustProbability(0.8F));
            addSellRecipe(merchantrecipelist, Item.getItemFromBlock(GCBlocks.aluminumWire), this.rand, this.adjustProbability(0.8F));
            addSellRecipe(merchantrecipelist, GCItems.basicItem, this.rand, this.adjustProbability(0.9F));
            if(Loader.isModLoaded(IC2ID)) {
            	addSellRecipe(merchantrecipelist, GameRegistry.findItem(IC2ID, "itemOreIridium"), this.rand, this.adjustProbability(0.1F));
            }
            
            // add sell iridium / schematics
        	break;
        case 2: /* AirMaster */
            addSellRecipe(merchantrecipelist, GCItems.oxTankHeavy, this.rand, this.adjustProbability(0.1F));
            addSellRecipe(merchantrecipelist, GCItems.oxTankMedium, this.rand, this.adjustProbability(0.2F));
            addSellRecipe(merchantrecipelist, GCItems.oxTankLight, this.rand, this.adjustProbability(0.3F));
            addSellRecipe(merchantrecipelist, GCItems.oxygenGear, this.rand, this.adjustProbability(0.4F));
            addSellRecipe(merchantrecipelist, GCItems.oxMask, this.rand, this.adjustProbability(0.3F));
            addSellRecipe(merchantrecipelist, Item.getItemFromBlock(GCBlocks.airLockSeal), this.rand, this.adjustProbability(0.1F));
            addSellRecipe(merchantrecipelist, GCItems.oxygenConcentrator, this.rand, this.adjustProbability(0.8F));
            addSellRecipe(merchantrecipelist, GCItems.oxygenFan, this.rand, this.adjustProbability(0.8F));
            addSellRecipe(merchantrecipelist, GCItems.oxygenVent, this.rand, this.adjustProbability(0.8F));
        	break;
        default:	/* Cook */
            addSellRecipe(merchantrecipelist, Items.ender_pearl, this.rand, this.adjustProbability(0.3F));
            addSellRecipe(merchantrecipelist, Items.ender_eye, this.rand, this.adjustProbability(0.3F));
            addBuyRecipe(merchantrecipelist, Item.getItemFromBlock(Blocks.sapling), this.rand, this.adjustProbability(0.9F));
            if (this.rand.nextFloat() < this.adjustProbability(0.9F)) {
            	merchantrecipelist.add(new MerchantRecipe(new ItemStack(GCItems.basicItem, 16, 15), new ItemStack(GCItems.meteoricIronRaw, 1, 0)));
            	merchantrecipelist.add(new MerchantRecipe(new ItemStack(GCItems.basicItem, 16, 16), new ItemStack(GCItems.meteoricIronRaw, 1, 0)));
            	merchantrecipelist.add(new MerchantRecipe(new ItemStack(GCItems.basicItem, 16, 17), new ItemStack(GCItems.meteoricIronRaw, 1, 0)));
            	merchantrecipelist.add(new MerchantRecipe(new ItemStack(GCItems.basicItem, 16, 18), new ItemStack(GCItems.meteoricIronRaw, 1, 0)));
            }
            
            // add sell food, and buy saplings/seeds
        }
        
        if (merchantrecipelist.isEmpty())
        {
            addBuyRecipe(merchantrecipelist, Items.gold_ingot, this.rand, 1.0F);
        }

        Collections.shuffle(merchantrecipelist);

        if (this.buyingList == null) {
            this.buyingList = new MerchantRecipeList();
        }

        for (int l = 0; l < itm_qty && l < merchantrecipelist.size(); ++l) {
            this.buyingList.addToListWithCheck((MerchantRecipe) merchantrecipelist.get(l));
        }
    }
    
	@Override
	public MerchantRecipeList getRecipes(EntityPlayer p_70934_1_) {
        if (this.buyingList == null) {
            this.addDefaultEquipmentAndRecipies(1);
        }
        return this.buyingList;
	}

	@Override
	public void setRecipes(MerchantRecipeList p_70930_1_) {
		// Set list of recipes
	}

	@SuppressWarnings("unchecked")
	public static void addBuyRecipe(MerchantRecipeList mrl, Item byuGoods, Random rand, float probability) {
        if (rand.nextFloat() < probability) {
            mrl.add(new MerchantRecipe(makeStack(byuGoods, rand), GCItems.meteoricIronRaw));
        }
    }
    
    @SuppressWarnings("unchecked")
	public static void addSellRecipe(MerchantRecipeList mrl, Item goods, Random rand, float probability)
    {
        if (rand.nextFloat() < probability) {
            int i = getRandomQuantity(goods, rand);
            ItemStack itemstack;
            ItemStack itemstack1;

            if (i < 0) {
                itemstack = new ItemStack(GCItems.meteoricIronRaw, 1, 0);
                itemstack1 = new ItemStack(goods, -i, 0);	// if negative i: -i of items per one meteor
            }
            else {
                itemstack = new ItemStack(GCItems.meteoricIronRaw, i, 0);	// if positive i: i of meteors per one item
                itemstack1 = new ItemStack(goods, 1, 0);
            }

            mrl.add(new MerchantRecipe(itemstack, itemstack1));
        }
    }
    
    private static int getRandomQuantity(Item goods, Random rand) {
        Tuple tuple = villagersSellingList.get(goods);
        return tuple == null ? 1 : (((Integer)tuple.getFirst()).intValue() >= ((Integer)tuple.getSecond()).intValue() ? ((Integer)tuple.getFirst()).intValue() : ((Integer)tuple.getFirst()).intValue() + rand.nextInt(((Integer)tuple.getSecond()).intValue() - ((Integer)tuple.getFirst()).intValue()));
    }

    private static ItemStack makeStack(Item goods, Random rand) {
        return new ItemStack(goods, getRandomQuantity(goods, rand), 0);
    }

	@Override
	public void func_110297_a_(ItemStack goods) {
		// talk "yes" or "no" - not implemented ;)
	}
    static {
    	// item, minimum to sell, maximum to sell
        villagersSellingList.put(GCItems.fuelCanister, new Tuple(Integer.valueOf(1), Integer.valueOf(3)));
        villagersSellingList.put(GCItems.sensorGlasses, new Tuple(Integer.valueOf(2), Integer.valueOf(5)));
        villagersSellingList.put(GCItems.partNoseCone, new Tuple(Integer.valueOf(1), Integer.valueOf(3)));
        villagersSellingList.put(GCItems.heavyPlatingTier1, new Tuple(Integer.valueOf(-8), Integer.valueOf(-2)));
        villagersSellingList.put(GCItems.oxMask, new Tuple(Integer.valueOf(1), Integer.valueOf(4)));
        villagersSellingList.put(Item.getItemFromBlock(GCBlocks.glowstoneTorch), new Tuple(Integer.valueOf(-32), Integer.valueOf(-12)));
        villagersSellingList.put(Item.getItemFromBlock(GCBlocks.airLockSeal), new Tuple(Integer.valueOf(8), Integer.valueOf(20)));
        villagersSellingList.put(Items.ender_pearl, new Tuple(Integer.valueOf(-4), Integer.valueOf(-2)));
        villagersSellingList.put(Items.ender_eye, new Tuple(Integer.valueOf(-4), Integer.valueOf(-2)));
        villagersSellingList.put(GCItems.oxygenConcentrator, new Tuple(Integer.valueOf(-6), Integer.valueOf(-2)));
        villagersSellingList.put(GCItems.oxygenFan, new Tuple(Integer.valueOf(-8), Integer.valueOf(-4)));
        villagersSellingList.put(GCItems.oxygenGear, new Tuple(Integer.valueOf(2), Integer.valueOf(6)));
        villagersSellingList.put(GCItems.oxygenVent, new Tuple(Integer.valueOf(-8), Integer.valueOf(-4)));
        villagersSellingList.put(GCItems.oxTankHeavy, new Tuple(Integer.valueOf(3), Integer.valueOf(9)));
        villagersSellingList.put(GCItems.oxTankMedium, new Tuple(Integer.valueOf(2), Integer.valueOf(6)));
        villagersSellingList.put(GCItems.oxTankLight, new Tuple(Integer.valueOf(1), Integer.valueOf(3)));
        villagersSellingList.put(GCItems.basicItem, new Tuple(Integer.valueOf(-10), Integer.valueOf(-5)));
        villagersSellingList.put(Item.getItemFromBlock(GCBlocks.oxygenPipe), new Tuple(Integer.valueOf(-16), Integer.valueOf(-8)));
        villagersSellingList.put(Item.getItemFromBlock(GCBlocks.aluminumWire), new Tuple(Integer.valueOf(-16), Integer.valueOf(-8)));
        villagersSellingList.put(Item.getItemFromBlock(GCBlocks.solarPanel), new Tuple(Integer.valueOf(4), Integer.valueOf(8)));
        villagersSellingList.put(Item.getItemFromBlock(Blocks.glowstone), new Tuple(Integer.valueOf(-6), Integer.valueOf(-3)));
        villagersSellingList.put(Item.getItemFromBlock(Blocks.sapling), new Tuple(Integer.valueOf(8), Integer.valueOf(16)));

        if(Loader.isModLoaded(GCMarsID)) {
        	villagersSellingList.put(GameRegistry.findItem(GCMarsID, "spaceshipTier2"), new Tuple(Integer.valueOf(8), Integer.valueOf(16)));
        }
        if(Loader.isModLoaded(IC2ID)) {
        	villagersSellingList.put(GameRegistry.findItem(IC2ID, "itemOreIridium"), new Tuple(Integer.valueOf(32), Integer.valueOf(48)));
        }
    }
}
