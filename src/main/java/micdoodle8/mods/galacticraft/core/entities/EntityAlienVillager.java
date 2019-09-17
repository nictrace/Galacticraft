package micdoodle8.mods.galacticraft.core.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
    private Village villageObj;
    private EntityPlayer buyingPlayer;
    private MerchantRecipeList buyingList;
    private int wealth;
    private boolean field_82190_bM;
    private float field_82191_bN;
    /** Selling list of Villagers items. */
    public static final Map<Item, Tuple> villagersSellingList = new HashMap<Item, Tuple>();
    

    public EntityAlienVillager(World par1World)
    {
        super(par1World);
        this.randomTickDivider = 0;
        this.isMating = false;
        this.isPlaying = false;
        this.villageObj = null;
        this.setSize(0.6F, 2.35F);
        this.getNavigator().setBreakDoors(true);
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIMoveIndoors(this));
        this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.3F));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 15.0F, 1.0F));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityVillager.class, 15.0F, 0.05F));
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

                if (this.field_82190_bM)
                {
                    this.field_82190_bM = false;
                    this.villageObj.setDefaultPlayerReputation(5);
                }
            }
        }

        super.updateAITick();
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

    public void setProfession(int par1)
    {
        this.dataWatcher.updateObject(16, Integer.valueOf(par1));
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
    
    public void func_82187_q()
    {
        this.field_82190_bM = true;
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
    private float adjustProbability(float p_82188_1_)
    {
        float f1 = p_82188_1_ + this.field_82191_bN;
        return f1 > 0.9F ? 0.9F - (f1 - 0.9F) : f1;
    }
    /**
     * based on the villagers profession add items, equipment, and recipies adds par1 random items to the list of things
     * that the villager wants to buy. (at most 1 of each wanted type is added)
     */
    private void addDefaultEquipmentAndRecipies(int p_70950_1_)
    {
        if (this.buyingList != null) {
        	// корень из количества рецептов деленный на 5 =)
            //this.field_82191_bN = MathHelper.sqrt_float((float)this.buyingList.size()) * 0.2F;
        }
        else {
            //this.field_82191_bN = 0.0F;
        }

        MerchantRecipeList merchantrecipelist;
        merchantrecipelist = new MerchantRecipeList(); // local recipes
        func_146091_a(merchantrecipelist, GCItems.fuelCanister, this.rand, this.adjustProbability(0.5F));
        func_146091_a(merchantrecipelist, GCItems.sensorGlasses, this.rand, this.adjustProbability(0.5F));
        func_146091_a(merchantrecipelist, GCItems.partNoseCone, this.rand, this.adjustProbability(0.1F));
        func_146091_a(merchantrecipelist, GCItems.heavyPlatingTier1, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, GCItems.oxMask, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, Item.getItemFromBlock(GCBlocks.glowstoneTorch), this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, Item.getItemFromBlock(GCBlocks.airLockSeal), this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, GCItems.oxygenConcentrator, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, GCItems.oxygenFan, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, GCItems.oxygenGear, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, GCItems.oxygenVent, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, GCItems.basicItem, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, Items.ender_pearl, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, Items.ender_eye, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, GCItems.oxTankHeavy, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, GCItems.oxTankMedium, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, GCItems.oxTankLight, this.rand, this.adjustProbability(0.3F));
        func_146091_a(merchantrecipelist, Item.getItemFromBlock(GCBlocks.solarPanel), this.rand, this.adjustProbability(0.3F));
    }
    
	@Override
	public MerchantRecipeList getRecipes(EntityPlayer p_70934_1_) {
		// Show list of recipes
        if (this.buyingList == null)
        {
            this.addDefaultEquipmentAndRecipies(1);
        }
        return this.buyingList;
	}

	@Override
	public void setRecipes(MerchantRecipeList p_70930_1_) {
		// Set list of recipes
		
	}
    public static void func_146091_a(MerchantRecipeList p_146091_0_, Item p_146091_1_, Random p_146091_2_, float p_146091_3_)
    {
        if (p_146091_2_.nextFloat() < p_146091_3_)
        {
            p_146091_0_.add(new MerchantRecipe(func_146088_a(p_146091_1_, p_146091_2_), GCItems.meteoricIronRaw));
        }
    }
    private static ItemStack func_146088_a(Item p_146088_0_, Random p_146088_1_)
    {
        return new ItemStack(p_146088_0_, func_146092_b(p_146088_0_, p_146088_1_), 0);
    }
    // get quantity of goods
    private static int func_146092_b(Item p_146092_0_, Random p_146092_1_)
    {
        Tuple tuple = (Tuple)villagersSellingList.get(p_146092_0_);
        return tuple == null ? 1 : (((Integer)tuple.getFirst()).intValue() >= ((Integer)tuple.getSecond()).intValue() ? ((Integer)tuple.getFirst()).intValue() : ((Integer)tuple.getFirst()).intValue() + p_146092_1_.nextInt(((Integer)tuple.getSecond()).intValue() - ((Integer)tuple.getFirst()).intValue()));
    }

	@Override
	public void func_110297_a_(ItemStack p_110297_1_) {
		// talk "yes" or "no" - not implemented ;)
	}
    static {
    	// item, mimimum to sell, maximum to sell
        villagersSellingList.put(GCItems.fuelCanister, new Tuple(Integer.valueOf(1), Integer.valueOf(1)));
        villagersSellingList.put(GCItems.sensorGlasses, new Tuple(Integer.valueOf(1), Integer.valueOf(1)));
        villagersSellingList.put(GCItems.partNoseCone, new Tuple(Integer.valueOf(1), Integer.valueOf(3)));
        villagersSellingList.put(GCItems.heavyPlatingTier1, new Tuple(Integer.valueOf(4), Integer.valueOf(16)));
        villagersSellingList.put(GCItems.oxMask, new Tuple(Integer.valueOf(1), Integer.valueOf(1)));
        villagersSellingList.put(Item.getItemFromBlock(GCBlocks.glowstoneTorch), new Tuple(Integer.valueOf(12), Integer.valueOf(32)));
        villagersSellingList.put(Item.getItemFromBlock(GCBlocks.airLockSeal), new Tuple(Integer.valueOf(1), Integer.valueOf(1)));
        villagersSellingList.put(Items.ender_pearl, new Tuple(Integer.valueOf(3), Integer.valueOf(4)));
        villagersSellingList.put(Items.ender_eye, new Tuple(Integer.valueOf(2), Integer.valueOf(3)));
        villagersSellingList.put(GCItems.oxygenConcentrator, new Tuple(Integer.valueOf(4), Integer.valueOf(16)));
        villagersSellingList.put(GCItems.oxygenFan, new Tuple(Integer.valueOf(10), Integer.valueOf(18)));
        villagersSellingList.put(GCItems.oxygenGear, new Tuple(Integer.valueOf(8), Integer.valueOf(18)));
        villagersSellingList.put(GCItems.oxygenVent, new Tuple(Integer.valueOf(9), Integer.valueOf(13)));
        villagersSellingList.put(GCItems.oxTankHeavy, new Tuple(Integer.valueOf(1), Integer.valueOf(1)));
        villagersSellingList.put(GCItems.oxTankMedium, new Tuple(Integer.valueOf(1), Integer.valueOf(2)));
        villagersSellingList.put(GCItems.oxTankLight, new Tuple(Integer.valueOf(1), Integer.valueOf(4)));
        villagersSellingList.put(GCItems.basicItem, new Tuple(Integer.valueOf(10), Integer.valueOf(25)));
        villagersSellingList.put(Item.getItemFromBlock(GCBlocks.oxygenPipe), new Tuple(Integer.valueOf(16), Integer.valueOf(32)));
        villagersSellingList.put(Item.getItemFromBlock(GCBlocks.aluminumWire), new Tuple(Integer.valueOf(16), Integer.valueOf(32)));
        villagersSellingList.put(Item.getItemFromBlock(GCBlocks.solarPanel), new Tuple(Integer.valueOf(1), Integer.valueOf(4)));
    }
}
