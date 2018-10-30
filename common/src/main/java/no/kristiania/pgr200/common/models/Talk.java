package no.kristiania.pgr200.common.models;

import com.google.gson.JsonObject;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

public class Talk extends BaseModel<Talk> {

    protected UUID id;
    @NotNull
    public String title;
    @NotNull
    public String description;
    public UUID topic_id;

    public Talk() {
    }

    public Talk(String title, String description) {
        setTitle(title);
        setDescription(description);
    }

    public Talk(UUID uuid) {
        setId(uuid);
    }

    public Talk(JsonObject talk) {
        if(talk.get("id") != null) setId(UUID.fromString(talk.get("id").getAsString()));
        if(talk.get("title") != null) setTitle(talk.get("title").getAsString());
        if(talk.get("description") != null) setDescription(talk.get("description").getAsString());
        if(talk.get("topic_id") != null) setTopic_id(UUID.fromString(talk.get("topic_id").getAsString()));
    }

    public Talk(UUID uuid, JsonObject talk) {
        this(talk);
        setId(uuid);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getTopic_id() {
        return topic_id;
    }

    public void setTopic_id(UUID topic_id) {
        this.topic_id = topic_id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Talk) {
            return this.getId().equals(((Talk) obj).getId()) &&
                    this.getTitle().equals(((Talk) obj).getTitle()) &&
                    this.getDescription().equals(((Talk) obj).getDescription());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description);
    }
}
