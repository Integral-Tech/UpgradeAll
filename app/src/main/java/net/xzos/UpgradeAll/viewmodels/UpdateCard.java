package net.xzos.UpgradeAll.viewmodels;

public class UpdateCard {
    private int databaseId;
    private String name;
    private String url;
    private String api;

    public UpdateCard(int databaseId, String name, String url, String api) {
        this.databaseId = databaseId;
        this.name = name;
        this.url = url;
        this.api = api;
    }

    public int getDatabaseId() {
        return databaseId;
    }
    public String getName() {
        return name;
    }

    public String getApi() {
        return api;
    }

    public String getUrl() {
        return url;
    }

}