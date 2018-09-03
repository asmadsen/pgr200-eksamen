package no.kristiania.pgr200.commandline.Commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import no.kristiania.pgr200.commandline.CommandOptions;
import no.kristiania.pgr200.commandline.ConferenceCliClient;
import no.kristiania.pgr200.commandline.Http.Response;

public class AddTalkCommand extends ConferenceClientCommand {

    private String title;
    private String description;

    /**
     * Overrides ConferenceClientCommand.register
     *
     * @param client ConferenceCliClient to register the command with
     */
    public static void register(ConferenceCliClient client) {
        client.register("add", AddTalkCommand.class);
    }

    public AddTalkCommand withTitle(String title) {
        this.title = title;
        return this;
    }

    public AddTalkCommand withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public AddTalkCommand execute(CommandOptions options) {
        this.title = options.get("title");
        this.description = options.get("description");
        JsonObject object = new JsonObject();
        object.addProperty("title", title);
        object.addProperty("description", description);

        Response<JsonElement> response = this.client.post("/api/talks", object);

        return this;
    }

}
