package no.kristiania.pgr200.orm.test_data;

import java.util.Objects;
import java.util.UUID;

public class User extends BaseModel<User> {

    protected UUID id;
    public String name;
    public String email;

    public User() {
    }

    public User(UUID id, String name, String email) {
        setId(id);
        setName(name);
        setEmail(email);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return this.hashCode() == obj.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }
}
