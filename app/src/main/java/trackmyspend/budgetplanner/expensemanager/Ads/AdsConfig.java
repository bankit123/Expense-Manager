package trackmyspend.budgetplanner.expensemanager.Ads;


public class AdsConfig {
    public String appID;
    public String banner;
    public String inter;
    public String inter_reward;
    public String nativeAd;
    public String reward;

    public AdsConfig() {} // Needed for Firebase

    public AdsConfig(String appID, String banner, String inter,
                     String inter_reward, String nativeAd, String reward) {
        this.appID = appID;
        this.banner = banner;
        this.inter = inter;
        this.inter_reward = inter_reward;
        this.nativeAd = nativeAd;
        this.reward = reward;
    }

    @Override
    public String toString() {
        return "AdsConfig{" +
                "appID='" + appID + '\'' +
                ", banner='" + banner + '\'' +
                ", inter='" + inter + '\'' +
                ", inter_reward='" + inter_reward + '\'' +
                ", nativeAd='" + nativeAd + '\'' +
                ", reward='" + reward + '\'' +
                '}';
    }
}
