package io.github.axolotlclient.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.axolotlclient.util.clientCommands.CommandResponse;
import org.jetbrains.annotations.NotNull;

public class DoubleOption extends NumericOption<Double> {

    public DoubleOption(String name, String tooltipLocation, double Default, double min, double max) {
        super(name, tooltipLocation, ()->{}, Default, min, max);
    }

    public DoubleOption(String name, ChangedListener onChange, double Default, double min, double max) {
        super(name, onChange, Default, min, max);
    }

    public DoubleOption(String name, double Default, double min, double max) {
        this(name, (String) null, Default, min, max);
    }

    @Override
    public OptionType getType() {
        return OptionType.DOUBLE;
    }


    @Override
    public void setValueFromJsonElement(@NotNull JsonElement element) {
        option = element.getAsDouble();
    }

    @Override
    public JsonElement getJson() {
        return new JsonPrimitive(option);
    }

    @Override
    protected CommandResponse onCommandExecution(String[] args) {
        try {
            if (args.length > 0) {
                set(Double.parseDouble(args[0]));
                return new CommandResponse(true, "Successfully set "+getName()+" to "+get()+"!");
            }
        } catch (NumberFormatException ignored){
            return new CommandResponse(false, "Please specify the number to set "+getName()+" to!");
        }

        return new CommandResponse(true, getName() + " is currently set to '"+get()+"'.");

    }


}
