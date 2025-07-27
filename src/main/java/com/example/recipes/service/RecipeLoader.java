package com.example.recipes.service;

import com.example.recipes.model.Recipe;
import com.example.recipes.repository.RecipeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class RecipeLoader {

    @Autowired
    private RecipeRepository recipeRepository;

    @PostConstruct
    public void loadRecipes() {
        try {
            if (recipeRepository.count() > 10) return;

            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("data.json").getInputStream();
            JsonNode root = mapper.readTree(inputStream);

            int success = 0, failed = 0;

            for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                JsonNode node = entry.getValue();

                try {
                    Recipe recipe = new Recipe();
                    recipe.setTitle(getText(node, "title"));
                    recipe.setCuisine(getText(node, "cuisine"));
                    recipe.setDescription(getText(node, "description"));
                    recipe.setServes(getText(node, "serves"));
                    recipe.setUrl(getText(node, "URL"));
                    recipe.setRating(getFloat(node, "rating"));
                    recipe.setPrepTime(getInt(node, "prep_time"));
                    recipe.setCookTime(getInt(node, "cook_time"));
                    recipe.setTotalTime(getInt(node, "total_time"));

                    recipe.setIngredients(mapper.convertValue(node.get("ingredients"), new TypeReference<List<String>>() {}));
                    recipe.setInstructions(mapper.convertValue(node.get("instructions"), new TypeReference<List<String>>() {}));
                    recipe.setNutrition(mapper.convertValue(node.get("nutrients"), new TypeReference<Map<String, String>>() {}));

                    recipeRepository.save(recipe);
                    success++;
                } catch (Exception e) {
                    System.out.println(" Skipping: " + node.get("title"));
                    failed++;
                }
            }

            System.out.println(" Recipes loaded: " + success);
            System.out.println(" Recipes skipped: " + failed);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    private Float getFloat(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? (float) node.get(field).asDouble() : Float.NaN;
    }

    private Integer getInt(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asInt() : null;
    }
}
