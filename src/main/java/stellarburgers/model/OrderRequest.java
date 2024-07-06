package stellarburgers.model;

import java.util.List;

public class OrderRequest {

    private List<String> ingredients;

    public OrderRequest(List<String> ingredients) {
        this.ingredients = ingredients;
    }

}
