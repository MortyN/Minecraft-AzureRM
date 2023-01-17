package org.mortyn.aksmanager.enums;

import net.minecraft.entity.SpawnGroup;

import java.util.Map;

public enum Color {
    WHITE(255, 255, 255),
    BLACK(0, 0, 0),
    RED(255, 0, 0),
    ORANGE(255, 127, 0),
    YELLOW(255, 255, 0),
    GREEN(0, 255, 0),
    BLUE(0, 0, 255),
    PURPLE(127, 0, 127),
    PINK(255, 155, 182);

    public int red;
    public int green;
    public int blue;

    private static final Map<SpawnGroup, Color> spawnGroupColors = Map.of(
            SpawnGroup.AMBIENT, Color.PURPLE,
            SpawnGroup.AXOLOTLS, Color.PINK,
            SpawnGroup.CREATURE, Color.YELLOW,
            SpawnGroup.MISC, Color.WHITE,
            SpawnGroup.MONSTER, Color.RED,
            SpawnGroup.UNDERGROUND_WATER_CREATURE, Color.ORANGE,
            SpawnGroup.WATER_AMBIENT, Color.GREEN,
            SpawnGroup.WATER_CREATURE, Color.BLUE
    );

    private static Color[] colors = Color.values();

    private Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public static Color of(SpawnGroup group) {
        return spawnGroupColors.get(group);
    }

    public Color next() {
        return get((this.ordinal() + 1) % colors.length);
    }

    public Color get(int index) {
        return colors[index];
    }
}