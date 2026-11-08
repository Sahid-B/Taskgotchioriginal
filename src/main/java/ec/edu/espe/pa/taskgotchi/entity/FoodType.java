package ec.edu.espe.pa.taskgotchi.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FoodType {
    APPLE("Manzana", 5, 15, 2, 0),
    CARROT("Zanahoria", 4, 12, 0, 3),
    MUSHROOM_SOUP("Sopa de hongos", 12, 35, 5, 0),
    HONEY_TEA("Té de miel", 8, 5, 5, 25),
    BERRY_PIE("Pastel de bayas", 20, 30, 20, 0),
    STAR_CANDY("Caramelo estelar", 15, 5, 30, 5);

    private final String displayName;
    private final int price;
    private final int hunger;
    private final int mood;
    private final int energy;
}
