package net.xzos.upgradeall.core.data.json.gson

import com.google.gson.annotations.SerializedName

/**
 * app_list: [{"app_config_file_name": "", "app_config_file_name": "", "app_config_file_name": ""}]
 * hub_list: [{"hub_config_file_name": "", "hub_config_file_name": "", "hub_config_file_name": ""}]
 */
data class CloudConfig(
        @SerializedName("app_list") var appList: List<AppListBean> = listOf(),
        @SerializedName("hub_list") var hubList: List<HubListBean> = listOf()
) {

    /**
     * app_config_name:
     * app_config_uuid:
     * app_config_file_name:
     */
    data class AppListBean(
        @SerializedName("app_config_name") var appConfigName: String,
        @SerializedName("app_config_uuid") var appConfigUuid: String,
        @SerializedName("app_config_file_name") var appConfigFileName: String
    )

    /**
     * hub_config_name:
     * hub_config_uuid:
     * hub_config_file_name:
     */
    data class HubListBean(
        @SerializedName("hub_config_name") var hubConfigName: String,
        @SerializedName("hub_config_uuid") var hubConfigUuid: String,
        @SerializedName("hub_config_file_name") var hubConfigFileName: String
    )
}