package hoowe.locationmanagerlibrary.hoowe;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import hoowe.locationmanagerlibrary.db.LocationDBHelper;
import hoowe.locationmanagerlibrary.utils.TimeUtils;

/**
 * @author DreamFish
 */
public class HooweLocationTracker extends BDAbstractLocationListener {
    public static final String TAG = "HooweLocationTracker";

    private Context mContext;

    private LocationClient client = null;

    private LocationClientOption mOption, DIYoption;

    private Object objLock = new Object();

    private OnLocationTrackerListener mListener;

    /***
     *
     * @param locationContext
     */
    public HooweLocationTracker(Context locationContext) {
        synchronized (objLock) {
            mContext = locationContext;
            if (client == null) {
                client = new LocationClient(locationContext);
                client.setLocOption(getDefaultLocationClientOption());
            }
        }
    }

    /***
     *
     * @param listener
     * @return
     */
    public boolean registerListener(OnLocationTrackerListener listener) {
        boolean isSuccess = false;
        if (listener != null) {
            this.mListener = listener;
            client.registerLocationListener(this);
            isSuccess = true;
        }
        return isSuccess;
    }

    public void unregisterListener() {
        this.mListener = null;
        client.unRegisterLocationListener(this);
    }

    /***
     *
     * @param option
     * @return isSuccessSetOption
     */
    public boolean setLocationOption(LocationClientOption option) {
        boolean isSuccess = false;
        if (option != null) {
            if (client.isStarted())
                client.stop();
            DIYoption = option;
            client.setLocOption(option);
        }
        return isSuccess;
    }

    public LocationClientOption getOption() {
        return DIYoption;
    }

    /***
     *
     * @return DefaultLocationClientOption
     */
    public LocationClientOption getDefaultLocationClientOption() {
        if (mOption == null) {
            mOption = new LocationClientOption();
            mOption.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
            mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
            mOption.setScanSpan(3000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
            mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
            mOption.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
            mOption.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
            mOption.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
            mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
            mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
            mOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
            mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
            mOption.setIsNeedAltitude(false);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        }
        return mOption;
    }

    public void start() {
        synchronized (objLock) {
            if (client != null && !client.isStarted()) {
                client.start();
            }
        }
    }

    public void reStart() {
        synchronized (objLock) {
            if (client != null) {
                client.restart();
            }
        }
    }

    public void stop() {
        synchronized (objLock) {
            if (client != null && client.isStarted()) {
                client.stop();
            }
        }
    }

    /**
     * 定位返回
     *
     * @param bdLocation
     */
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        Log.d(TAG, "tracker onReceiveLocation");
        HooweLocation location = new HooweLocation();
        location.setLocationID(bdLocation.getLocationID());
        location.setLocType(bdLocation.getLocType());
        location.setLocTime(TimeUtils.string2Millis(bdLocation.getTime()));
        location.setLocTimeText(bdLocation.getTime());
        location.setLatitude(bdLocation.getLatitude());
        location.setLongitude(bdLocation.getLongitude());
        location.setRadius(bdLocation.getRadius());
        location.setAddrStr(bdLocation.getAddrStr());
        location.setCountry(bdLocation.getCountry());
        location.setCountryCode(bdLocation.getCountryCode());
        location.setCity(bdLocation.getCity());
        location.setCityCode(bdLocation.getCityCode());
        location.setDistrict(bdLocation.getDistrict());
        location.setStreet(bdLocation.getStreet());
        location.setStreetNumber(bdLocation.getStreetNumber());
        location.setLocationDescribe(bdLocation.getLocationDescribe());
        location.setBuildingID(bdLocation.getBuildingID());
        location.setBuildingName(bdLocation.getBuildingName());
        location.setFloor(bdLocation.getFloor());
        location.setSpeed(bdLocation.getSpeed());
        location.setSatelliteNumber(bdLocation.getSatelliteNumber());
        location.setAltitude(bdLocation.getAltitude());
        location.setDirection(bdLocation.getDirection());
        location.setOperators(bdLocation.getOperators());

        // 将数据插入数据库
        LocationDBHelper.getHelper(mContext).locationInsert(location);
        // 每次位置更新回调通知界面
        mListener.onReceiveLocation(location);
    }
}
