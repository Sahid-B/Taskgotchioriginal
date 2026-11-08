package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.*;
import java.util.List;

public interface KitchenService {
    List<FoodResponse> getFoods();
    KitchenStateResponse getPantry();
    KitchenStateResponse buyFood(BuyFoodRequest request);
    KitchenStateResponse feedFood(FeedFoodRequest request);
}
