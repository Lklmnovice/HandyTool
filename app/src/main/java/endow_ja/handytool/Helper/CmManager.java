package endow_ja.handytool.Helper;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;

public class CmManager {
    public static boolean isFlashlightOn = false;
    public static void registerFlashlightState (Context context){
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        cameraManager.registerTorchCallback(torchCallback, null);
    }

    public static void unregisterFlashlightState(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        cameraManager.unregisterTorchCallback(torchCallback);
    }

    private static CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
            isFlashlightOn = enabled;
        }
    };
}
