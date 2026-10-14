package placementprep.model;

public abstract class User {
    private final String name;

    protected User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String getRole();
}
