package no.kristiania.pgr200.orm;

public class GenericRecord extends BaseRecord {
    @Override
    public String getTable() {
        return null;
    }

    @Override
    public boolean save() {
        return false;
    }
}