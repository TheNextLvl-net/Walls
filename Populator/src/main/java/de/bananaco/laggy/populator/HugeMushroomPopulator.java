package de.bananaco.laggy.populator;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import de.bananaco.laggy.Utils;

public class HugeMushroomPopulator extends BlockPopulator
{
    @Override
    public void populate(World world, Random rnd, Chunk chunk){
        Block base = Utils.getHighestGrassBlock(chunk, rnd.nextInt(16), rnd.nextInt(16));
        if (base != null) {
            Block block = base.getRelative(0, 1, 0);
            grow(world, rnd, block.getX(), block.getY(), block.getZ());
        }
    }

    public boolean grow(World world, Random random, int i, int j, int k)
    {
        int l = random.nextInt(2);
        
        int m = (l == 0) ? Material.HUGE_MUSHROOM_1.getId() : Material.HUGE_MUSHROOM_2.getId();

        int i1 = random.nextInt(3) + 4;
        boolean flag = true;

        if ((j >= 1) && (j + i1 + 1 < 256))
        {
            for (int j1 = j; j1 <= j + 1 + i1; j1++) {
                byte b0 = 3;

                if (j1 <= j + 3) {
                    b0 = 0;
                }

                for (int k1 = i - b0; (k1 <= i + b0) && (flag); k1++) {
                    for (int l1 = k - b0; (l1 <= k + b0) && (flag); l1++) {
                        if ((j1 >= 0) && (j1 < 256)) {
                            int i2 = world.getBlockTypeIdAt(k1, j1, l1);
                            if ((i2 != 0) && (i2 != Material.LEAVES.getId()))
                                flag = false;
                        }
                        else {
                            flag = false;
                        }
                    }
                }
            }

            if (!flag) {
                return false;
            }
            int j1 = world.getBlockTypeIdAt(i, j - 1, k);
            if ((j1 != Material.DIRT.getId()) && (j1 != Material.GRASS.getId()) && (j1 != Material.MYCEL.getId())) {
                return false;
            }

            world.getBlockAt(i, j - 1, k).setType(Material.DIRT);

            int j2 = j + i1;

            if (l == 1) {
                j2 = j + i1 - 3;
            }

            for (int k1 = j2; k1 <= j + i1; k1++) {
                int l1 = 1;
                if (k1 < j + i1) {
                    l1++;
                }

                if (l == 0) {
                    l1 = 3;
                }

                for (int i2 = i - l1; i2 <= i + l1; i2++) {
                    for (int k2 = k - l1; k2 <= k + l1; k2++) {
                        int l2 = 5;

                        if (i2 == i - l1) {
                            l2--;
                        }

                        if (i2 == i + l1) {
                            l2++;
                        }

                        if (k2 == k - l1) {
                            l2 -= 3;
                        }

                        if (k2 == k + l1) {
                            l2 += 3;
                        }

                        if ((l == 0) || (k1 < j + i1)) {
                            if (((i2 == i - l1) || (i2 == i + l1)) && ((k2 == k - l1) || (k2 == k + l1)))
                            {
                                continue;
                            }
                            if ((i2 == i - (l1 - 1)) && (k2 == k - l1)) {
                                l2 = 1;
                            }

                            if ((i2 == i - l1) && (k2 == k - (l1 - 1))) {
                                l2 = 1;
                            }

                            if ((i2 == i + (l1 - 1)) && (k2 == k - l1)) {
                                l2 = 3;
                            }

                            if ((i2 == i + l1) && (k2 == k - (l1 - 1))) {
                                l2 = 3;
                            }

                            if ((i2 == i - (l1 - 1)) && (k2 == k + l1)) {
                                l2 = 7;
                            }

                            if ((i2 == i - l1) && (k2 == k + (l1 - 1))) {
                                l2 = 7;
                            }

                            if ((i2 == i + (l1 - 1)) && (k2 == k + l1)) {
        						l2 = 9;
							}

							if ((i2 == i + l1) && (k2 == k + (l1 - 1))) {
								l2 = 9;
							}
						}

						if ((l2 == 5) && (k1 < j + i1)) {
							l2 = 0;
						}

						world.getBlockAt(i2, k1, k2).setTypeIdAndData(m, (byte) l2, true);
					}

				}

			}

			for (int k1 = 0; k1 < i1; k1++) {
				world.getBlockAt(i, j + k1, k).setTypeIdAndData(m, (byte) 10, true);
			}

			return true;
		}

		return false;
	}
}
