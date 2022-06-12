package io.github.moehreag.axolotlclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.moehreag.axolotlclient.AxolotlClient;
import io.github.moehreag.axolotlclient.config.options.Option;
import io.github.moehreag.axolotlclient.config.options.OptionCategory;
import org.quiltmc.loader.api.QuiltLoader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ConfigManager{
    private static final List<OptionCategory> categories = AxolotlClient.CONFIG.config;
    private static final Path confPath = QuiltLoader.getConfigDir().resolve("AxolotlClient.json");

    public static void save(){
        try{
            saveFile();
        } catch (IOException e) {
            AxolotlClient.LOGGER.error("Failed to save config!");
        }
    }

    private static void saveFile() throws IOException {

        JsonObject config = new JsonObject();
        for(OptionCategory category : categories) {
            JsonObject object = new JsonObject();
            for (Option option : category.getOptions()) {

                object.add(option.getName(), option.getJson());
            }

            if(!category.getSubCategories().isEmpty()){
                for(OptionCategory sub:category.getSubCategories()){
                    JsonObject subOption = new JsonObject();
                    sub.getOptions().forEach(option ->  subOption.add(option.getName(), option.getJson()));
                    object.add(sub.getName(), subOption);
                }
            }

            config.add(category.getName(), object);

        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Files.write(confPath, Collections.singleton(gson.toJson(config)));
    }

    public static void load() {
        loadDefaults();
        try {
            JsonObject config = JsonParser.parseReader(new FileReader(confPath.toString())).getAsJsonObject();

            for(OptionCategory category:categories) {
                for (Option option : category.getOptions()) {
                    JsonObject cat = config.get(category.getName()).getAsJsonObject();
                    if (cat.has(option.getName())) {
                        JsonElement part = cat.get(option.getName());
                        option.setValueFromJsonElement(part);
                    }
                }
                if(!category.getSubCategories().isEmpty()){
                    for (OptionCategory sub: category.getSubCategories()) {
                        JsonObject subCat = config.get(category.getName()).getAsJsonObject().get(sub.getName()).getAsJsonObject();
                        for(Option option: sub.getOptions()){
                            if(subCat.has(option.getName())){
                                option.setValueFromJsonElement(subCat.get(option.getName()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            AxolotlClient.LOGGER.error("Failed to load config! Using default values...");}
        save();
    }

    private static void loadDefaults(){
        AxolotlClient.CONFIG.config.forEach(OptionCategory -> {
            OptionCategory.getOptions().forEach(Option::setDefaults);
            if(!OptionCategory.getSubCategories().isEmpty()){
                for(OptionCategory category : OptionCategory.getSubCategories()){
                    category.getOptions().forEach(Option::setDefaults);
                }
            }
        });
    }


}
