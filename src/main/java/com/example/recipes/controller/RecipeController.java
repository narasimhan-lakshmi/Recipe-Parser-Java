package com.example.recipes.controller;

import com.example.recipes.model.Recipe;
import com.example.recipes.repository.RecipeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    private static final ObjectMapper mapper = new ObjectMapper();

    // Endpoint 1: Paginated or all
    @GetMapping
    public List<Recipe> getRecipes(@RequestParam(required = false) Integer page) {
        if (page == null) {
            return recipeRepository.findAll(Sort.by("rating").descending());
        }
        PageRequest pageable = PageRequest.of(page - 1, 10, Sort.by("rating").descending());
        return recipeRepository.findAll(pageable).getContent();
    }

    // Endpoint 2: Advanced search
    @GetMapping("/search")
    public List<Recipe> searchRecipes(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) Float minRating,
            @RequestParam(required = false) Integer maxCalories,
            @RequestParam(required = false) Integer maxTotalTime
    ) {
        return recipeRepository.findAll().stream()
                .filter(r -> title == null || (r.getTitle() != null && r.getTitle().toLowerCase().contains(title.toLowerCase())))
                .filter(r -> cuisine == null || (r.getCuisine() != null && r.getCuisine().equalsIgnoreCase(cuisine)))
                .filter(r -> minRating == null || (r.getRating() != null && r.getRating() >= minRating))
                .filter(r -> {
                    if (maxCalories == null) return true;
                    try {
                        JsonNode node = mapper.valueToTree(r.getNutrition());
                        JsonNode calNode = node.get("calories");
                        if (calNode == null || calNode.isNull()) return false;
                        String calString = calNode.asText().split(" ")[0];
                        return Integer.parseInt(calString) <= maxCalories;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(r -> maxTotalTime == null || (r.getTotalTime() != null && r.getTotalTime() <= maxTotalTime))
                .collect(Collectors.toList());
    }
}
