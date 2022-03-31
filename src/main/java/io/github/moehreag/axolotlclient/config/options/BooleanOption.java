package io.github.moehreag.axolotlclient.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanOption extends OptionBase implements Option{

    private boolean option;
    private boolean Default;

    public BooleanOption(String name, boolean Default) {
        super(name);
        this.Default = Default;
    }

    public boolean get(){
        return option;
    }

    @Override
    public OptionType getType() {
        return OptionType.BOOLEAN;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        option = element.getAsBoolean();
    }

    @Override
    public JsonElement getJson() {
        return new JsonPrimitive(option);
    }

    public void setDefaults(){
        option=Default;
    }

    public void toggle(){
        this.option=!option;
    }
}
