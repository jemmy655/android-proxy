package com.lechucksoftware.proxy.proxysettings.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.lechucksoftware.proxy.proxysettings.ApplicationGlobals;
import com.lechucksoftware.proxy.proxysettings.R;
import com.lechucksoftware.proxy.proxysettings.components.InputExclusionList;
import com.lechucksoftware.proxy.proxysettings.components.InputField;
import com.lechucksoftware.proxy.proxysettings.components.WifiSignal;
import com.lechucksoftware.proxy.proxysettings.constants.Constants;
import com.lechucksoftware.proxy.proxysettings.constants.FragmentMode;
import com.lechucksoftware.proxy.proxysettings.db.ProxyEntity;
import com.lechucksoftware.proxy.proxysettings.fragments.base.BaseFragment;
import com.lechucksoftware.proxy.proxysettings.fragments.base.IBaseFragment;
import com.lechucksoftware.proxy.proxysettings.utils.EventReportingUtils;
import com.lechucksoftware.proxy.proxysettings.utils.NavigationUtils;
import com.lechucksoftware.proxy.proxysettings.utils.UIUtils;
import com.shouldit.proxy.lib.APL;
import com.shouldit.proxy.lib.ProxyConfiguration;
import com.shouldit.proxy.lib.reflection.android.ProxySetting;
import com.shouldit.proxy.lib.utils.ProxyUtils;

import java.util.List;
import java.util.UUID;


public class WiFiApDetailFragment extends BaseFragment implements IBaseFragment
{
    public static final String TAG = "WiFiApDetailFragment";

    private ProxyConfiguration selectedWiFiAP;
    private ProxyEntity selectedProxy;

    private TextView wifiName;
//    private TextView wifiStatus;
    private WifiSignal wifiSignal;
    private Switch proxySwitch;
    private TextView proxySelector;
    private ViewGroup wifiLayout;
    private InputField proxyHost;
    private InputField proxyPort;
    private InputExclusionList proxyBypass;
//    private InputTags proxyTags;
    private UUID confId;
    private LinearLayout proxyFieldsLayout;

    /**
     * Create a new instance of WiFiApDetailFragment
     */
    public static WiFiApDetailFragment newInstance(UUID selectedId)
    {
        WiFiApDetailFragment instance = new WiFiApDetailFragment();

        Bundle args = new Bundle();
        args.putSerializable(Constants.SELECTED_AP_CONF_ARG, selectedId);
        instance.setArguments(args);

        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        confId = (UUID) getArguments().getSerializable(Constants.SELECTED_AP_CONF_ARG);
        selectedWiFiAP = ApplicationGlobals.getProxyManager().getConfiguration(confId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
//        LogWrapper.startTrace(TAG, "onCreateView", Log.DEBUG);

        View v = inflater.inflate(R.layout.wifi_ap_preferences, container, false);

//        LogWrapper.getPartial(TAG, "onCreateView", Log.DEBUG);

        wifiLayout = (ViewGroup) v.findViewById(R.id.wifi_layout);
//        wifiLayout.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                ActionsListFragment actionsListFragment = ActionsListFragment.newInstance(FragmentMode.DIALOG, selectedWiFiAP);
//                actionsListFragment.show(getFragmentManager(), TAG);
//            }
//        });

        wifiName = (TextView) v.findViewById(R.id.wifi_name);
//        wifiStatus = (TextView) v.findViewById(R.id.wifi_status);
        wifiSignal = (WifiSignal) v.findViewById(R.id.wifi_signal);
        proxySwitch = (Switch) v.findViewById(R.id.wifi_proxy_switch);
        proxySwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (proxySwitch.isChecked())
                {
                    selectedWiFiAP.setProxySetting(ProxySetting.STATIC);
                }
                else
                {
                    if (selectedWiFiAP.proxySetting == ProxySetting.STATIC)
                    {
                        long proxyId = ApplicationGlobals.getDBManager().findProxy(selectedWiFiAP.getProxyHost(), selectedWiFiAP.getProxyPort());
                        if (proxyId != -1)
                        {
                            ApplicationGlobals.getDBManager().clearInUseFlag(proxyId);
                        }
                    }

                    selectedWiFiAP.setProxySetting(ProxySetting.NONE);
                }

                saveConfiguration();
                refreshUI();
            }
        });

        proxySelector = (TextView) v.findViewById(R.id.proxy_selector);
        proxySelector.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openProxySelectorDialog();
            }
        });

        proxyFieldsLayout = (LinearLayout) v.findViewById(R.id.proxy_input_fields);

        proxyHost = (InputField) v.findViewById(R.id.proxy_host);
//        proxyHost.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                openProxyEditorDialog();
//            }
//        });


        proxyPort = (InputField) v.findViewById(R.id.proxy_port);
        proxyBypass = (InputExclusionList) v.findViewById(R.id.proxy_bypass);
//        proxyTags = (InputTags) v.findViewById(R.id.proxy_tags);

        refreshUI();
        return v;
    }

//    private void openProxyEditorDialog()
//    {
//        ProxyDetailFragment dialog = ProxyDetailFragment.newInstance(selectedProxy);
//        dialog.show(getFragmentManager(),TAG);
//    }

    private void openProxySelectorDialog()
    {
        List<ProxyEntity> availableProxies = ApplicationGlobals.getCacheManager().getAllProxiesList();
        if (availableProxies != null && availableProxies.size() > 0)
        {
            ProxyListFragment proxiesListFragment = ProxyListFragment.newInstance(FragmentMode.DIALOG, selectedWiFiAP);
            proxiesListFragment.show(getFragmentManager(), TAG);
        }
        else
        {
            UIUtils.showError(getActivity(), R.string.no_proxy_defined);
        }
    }

    private void refreshVisibility()
    {
        if (proxySwitch.isChecked())
        {
            proxySelector.setVisibility(View.VISIBLE);

            if (selectedProxy == null)
            {
                proxyFieldsLayout.setVisibility(View.GONE);
//                proxySelector.setError("SELECT A PROXY");
            }
            else
            {
                proxyFieldsLayout.setVisibility(View.VISIBLE);
//                proxySelector.setError(null);
            }
        }
        else
        {
            proxySelector.setVisibility(View.GONE);
            proxyFieldsLayout.setVisibility(View.GONE);
        }
    }

    private void saveConfiguration()
    {
        try
        {
            if (selectedWiFiAP != null && selectedWiFiAP.isValidConfiguration())
            {
                selectedWiFiAP.writeConfigurationToDevice();
            }
        }
        catch (Exception e)
        {
            EventReportingUtils.sendException(e);
            UIUtils.showError(getActivity(), R.string.exception_apl_writeconfig_error_message);
        }
    }

    public void refreshUI()
    {
//        LogWrapper.startTrace(TAG, "refreshUI", Log.DEBUG);

        if (!APL.getWifiManager().isWifiEnabled())
        {
            NavigationUtils.GoToMainActivity(getActivity());
        }

        if (selectedWiFiAP.proxySetting == ProxySetting.STATIC)
        {
            proxySwitch.setChecked(true);
            proxySwitch.setText(R.string.status_proxy_enabled);
            refreshFieldsValues();
        }
        else
        {
            proxySwitch.setChecked(false);
            proxySwitch.setText(R.string.status_proxy_disabled);
        }

        if (selectedWiFiAP.ap.getLevel() == -1)
        {
            wifiLayout.setBackgroundResource(R.color.DarkGrey);
        }
        else
        {
            if (selectedWiFiAP.isCurrentNetwork())
            {
                wifiLayout.setBackgroundResource(R.color.Holo_Blue_Dark);
            }
            else
            {
                wifiLayout.setBackgroundResource(R.color.Holo_Green_Dark);
            }
        }

        wifiName.setText(ProxyUtils.cleanUpSSID(selectedWiFiAP.getSSID()));
//        wifiStatus.setText(selectedWiFiAP.toStatusString());
        wifiSignal.setConfiguration(selectedWiFiAP);

        refreshVisibility();

//        LogWrapper.stopTrace(TAG, "refreshUI", Log.DEBUG);
    }

    private void refreshFieldsValues()
    {
        long proxyId = ApplicationGlobals.getDBManager().findProxy(selectedWiFiAP.getProxyHost(), selectedWiFiAP.getProxyPort());
        if (proxyId != -1)
        {
            selectedProxy = ApplicationGlobals.getDBManager().getProxy(proxyId);
            proxyHost.setValue(selectedProxy.host);
            proxyPort.setValue(selectedProxy.port);
            proxyBypass.setExclusionString(selectedProxy.exclusion);
//            proxyTags.setTags(selectedProxy.getTags());
        }
        else
        {
            selectedProxy = null;
            proxyHost.setValue("");
            proxyPort.setValue("");
            proxyBypass.setExclusionString("");
//            proxyTags.setTags(null);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        refreshUI();
    }
}
