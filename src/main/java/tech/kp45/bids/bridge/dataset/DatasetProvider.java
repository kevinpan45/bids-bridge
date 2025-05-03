package tech.kp45.bids.bridge.dataset;

public enum DatasetProvider {
    OPENNEURO("OpenNeuro", "OpenNeuro is a platform for sharing and analyzing neuroimaging data."),;

    private final String name;
    private final String description;

    DatasetProvider(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static DatasetProvider fromName(String name) {
        for (DatasetProvider provider : values()) {
            if (provider.getName().equalsIgnoreCase(name)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown dataset provider: " + name);
    }
}
