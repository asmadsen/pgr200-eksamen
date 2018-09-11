package no.kristiania.pgr200.server;

public class Talk {

    private String id;
    private String title;
    private String description;

    public Talk(){
    }

    public Talk(String title, String description){
        this.title = title;
        this.description = description;
    }

    public Talk(String id, String title, String description){
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() { return this.id; }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public Talk withId(String id){
        this.id = id;
        return this;
    }

    public Talk withTitle(String title){
        this.title = title;
        return this;
    }

    public Talk withDescription(String description){
        this.description = description;
        return this;
    }
}
