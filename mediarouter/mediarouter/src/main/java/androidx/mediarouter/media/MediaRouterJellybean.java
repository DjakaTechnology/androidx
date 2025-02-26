/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.mediarouter.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(16)
final class MediaRouterJellybean {
    private static final String TAG = "MediaRouterJellybean";

    // android.media.AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP = 0x80;
    // android.media.AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES = 0x100;
    // android.media.AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER = 0x200;
    public static final int DEVICE_OUT_BLUETOOTH = 0x80 | 0x100 | 0x200;

    public static final int ROUTE_TYPE_LIVE_AUDIO = 0x1;
    public static final int ROUTE_TYPE_LIVE_VIDEO = 0x2;
    public static final int ROUTE_TYPE_USER = 0x00800000;

    public static final int ALL_ROUTE_TYPES =
            MediaRouterJellybean.ROUTE_TYPE_LIVE_AUDIO
                    | MediaRouterJellybean.ROUTE_TYPE_LIVE_VIDEO
                    | MediaRouterJellybean.ROUTE_TYPE_USER;

    public static Object getMediaRouter(Context context) {
        return context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List getRoutes(Object routerObj) {
        final android.media.MediaRouter router = (android.media.MediaRouter) routerObj;
        final int count = router.getRouteCount();
        List out = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            out.add(router.getRouteAt(i));
        }
        return out;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List getCategories(Object routerObj) {
        final android.media.MediaRouter router = (android.media.MediaRouter) routerObj;
        final int count = router.getCategoryCount();
        List out = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            out.add(router.getCategoryAt(i));
        }
        return out;
    }

    public static Object getSelectedRoute(Object routerObj, int type) {
        return ((android.media.MediaRouter) routerObj).getSelectedRoute(type);
    }

    public static void selectRoute(Object routerObj, int types, Object routeObj) {
        ((android.media.MediaRouter) routerObj).selectRoute(types,
                (android.media.MediaRouter.RouteInfo) routeObj);
    }

    public static void addCallback(Object routerObj, int types, Object callbackObj) {
        ((android.media.MediaRouter) routerObj).addCallback(types,
                (android.media.MediaRouter.Callback) callbackObj);
    }

    public static void removeCallback(Object routerObj, Object callbackObj) {
        ((android.media.MediaRouter) routerObj).removeCallback(
                (android.media.MediaRouter.Callback) callbackObj);
    }

    public static Object createRouteCategory(Object routerObj,
            String name, boolean isGroupable) {
        return ((android.media.MediaRouter) routerObj).createRouteCategory(name, isGroupable);
    }

    public static Object createUserRoute(Object routerObj, Object categoryObj) {
        return ((android.media.MediaRouter) routerObj).createUserRoute(
                (android.media.MediaRouter.RouteCategory) categoryObj);
    }

    public static void addUserRoute(Object routerObj, Object routeObj) {
        ((android.media.MediaRouter) routerObj).addUserRoute(
                (android.media.MediaRouter.UserRouteInfo) routeObj);
    }

    public static void removeUserRoute(Object routerObj, Object routeObj) {
        try {
            ((android.media.MediaRouter) routerObj).removeUserRoute(
                    (android.media.MediaRouter.UserRouteInfo) routeObj);
        } catch (IllegalArgumentException e) {
            // Work around for https://issuetracker.google.com/issues/202931542.
            Log.w(TAG, "Failed to remove user route", e);
        }
    }

    public static Object createCallback(Callback callback) {
        return new CallbackProxy<Callback>(callback);
    }

    public static Object createVolumeCallback(VolumeCallback callback) {
        return new VolumeCallbackProxy<VolumeCallback>(callback);
    }

    public static final class RouteInfo {
        @NonNull
        public static CharSequence getName(@NonNull Object routeObj, @NonNull Context context) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getName(context);
        }

        @NonNull
        public static CharSequence getStatus(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getStatus();
        }

        public static int getSupportedTypes(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getSupportedTypes();
        }

        @Nullable
        public static Object getCategory(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getCategory();
        }

        @Nullable
        public static Drawable getIconDrawable(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getIconDrawable();
        }

        public static int getPlaybackType(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getPlaybackType();
        }

        public static int getPlaybackStream(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getPlaybackStream();
        }

        public static int getVolume(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getVolume();
        }

        public static int getVolumeMax(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getVolumeMax();
        }

        public static int getVolumeHandling(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getVolumeHandling();
        }

        @Nullable
        public static Object getTag(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getTag();
        }

        public static void setTag(@NonNull Object routeObj, @Nullable Object tag) {
            ((android.media.MediaRouter.RouteInfo) routeObj).setTag(tag);
        }

        public static void requestSetVolume(@NonNull Object routeObj, int volume) {
            ((android.media.MediaRouter.RouteInfo) routeObj).requestSetVolume(volume);
        }

        public static void requestUpdateVolume(@NonNull Object routeObj, int direction) {
            ((android.media.MediaRouter.RouteInfo) routeObj).requestUpdateVolume(direction);
        }

        @Nullable
        public static Object getGroup(@NonNull Object routeObj) {
            return ((android.media.MediaRouter.RouteInfo) routeObj).getGroup();
        }

        public static boolean isGroup(@NonNull Object routeObj) {
            return routeObj instanceof android.media.MediaRouter.RouteGroup;
        }

        private RouteInfo() {
        }
    }

    public static final class RouteGroup {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @NonNull
        public static List getGroupedRoutes(@NonNull Object groupObj) {
            final android.media.MediaRouter.RouteGroup group =
                    (android.media.MediaRouter.RouteGroup) groupObj;
            final int count = group.getRouteCount();
            List out = new ArrayList(count);
            for (int i = 0; i < count; i++) {
                out.add(group.getRouteAt(i));
            }
            return out;
        }

        private RouteGroup() {
        }
    }

    public static final class UserRouteInfo {
        public static void setName(@NonNull Object routeObj, @NonNull CharSequence name) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setName(name);
        }

        public static void setStatus(@NonNull Object routeObj, @NonNull CharSequence status) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setStatus(status);
        }

        public static void setIconDrawable(@NonNull Object routeObj, @Nullable Drawable icon) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setIconDrawable(icon);
        }

        public static void setPlaybackType(@NonNull Object routeObj, int type) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setPlaybackType(type);
        }

        public static void setPlaybackStream(@NonNull Object routeObj, int stream) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setPlaybackStream(stream);
        }

        public static void setVolume(@NonNull Object routeObj, int volume) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setVolume(volume);
        }

        public static void setVolumeMax(@NonNull Object routeObj, int volumeMax) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setVolumeMax(volumeMax);
        }

        public static void setVolumeHandling(@NonNull Object routeObj, int volumeHandling) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setVolumeHandling(volumeHandling);
        }

        public static void setVolumeCallback(@NonNull Object routeObj,
                @NonNull Object volumeCallbackObj) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setVolumeCallback(
                    (android.media.MediaRouter.VolumeCallback) volumeCallbackObj);
        }

        public static void setRemoteControlClient(@NonNull Object routeObj,
                @Nullable Object rccObj) {
            ((android.media.MediaRouter.UserRouteInfo) routeObj).setRemoteControlClient(
                    (android.media.RemoteControlClient) rccObj);
        }

        private UserRouteInfo() {
        }
    }

    public static final class RouteCategory {
        @Nullable
        public static CharSequence getName(@NonNull Object categoryObj, @NonNull Context context) {
            return ((android.media.MediaRouter.RouteCategory) categoryObj).getName(context);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @NonNull
        public static List getRoutes(@NonNull Object categoryObj) {
            ArrayList out = new ArrayList();
            ((android.media.MediaRouter.RouteCategory) categoryObj).getRoutes(out);
            return out;
        }

        public static int getSupportedTypes(@NonNull Object categoryObj) {
            return ((android.media.MediaRouter.RouteCategory) categoryObj).getSupportedTypes();
        }

        public static boolean isGroupable(@NonNull Object categoryObj) {
            return ((android.media.MediaRouter.RouteCategory) categoryObj).isGroupable();
        }

        private RouteCategory() {
        }
    }

    public interface Callback {
        void onRouteSelected(int type, @NonNull Object routeObj);

        void onRouteUnselected(int type, @NonNull Object routeObj);

        void onRouteAdded(@NonNull Object routeObj);

        void onRouteRemoved(@NonNull Object routeObj);

        void onRouteChanged(@NonNull Object routeObj);

        void onRouteGrouped(@NonNull Object routeObj, @NonNull Object groupObj, int index);

        void onRouteUngrouped(@NonNull Object routeObj, @NonNull Object groupObj);

        void onRouteVolumeChanged(@NonNull Object routeObj);
    }

    public interface VolumeCallback {
        void onVolumeSetRequest(@NonNull Object routeObj, int volume);

        void onVolumeUpdateRequest(@NonNull Object routeObj, int direction);
    }

    /**
     * Workaround for limitations of selectRoute() on JB and JB MR1.
     * Do not use on JB MR2 and above.
     */
    public static final class SelectRouteWorkaround {
        private Method mSelectRouteIntMethod;

        SelectRouteWorkaround() {
            if (Build.VERSION.SDK_INT < 16 || Build.VERSION.SDK_INT > 17) {
                throw new UnsupportedOperationException();
            }
            try {
                mSelectRouteIntMethod = android.media.MediaRouter.class.getMethod(
                        "selectRouteInt", int.class, android.media.MediaRouter.RouteInfo.class);
            } catch (NoSuchMethodException ex) {
            }
        }

        @SuppressLint("BanUncheckedReflection") // TODO: b/232075564
        public void selectRoute(@NonNull Object routerObj, int types, @NonNull Object routeObj) {
            android.media.MediaRouter router = (android.media.MediaRouter) routerObj;
            android.media.MediaRouter.RouteInfo route =
                    (android.media.MediaRouter.RouteInfo) routeObj;

            int routeTypes = route.getSupportedTypes();
            if ((routeTypes & ROUTE_TYPE_USER) == 0) {
                // Handle non-user routes.
                // On JB and JB MR1, the selectRoute() API only supports programmatically
                // selecting user routes.  So instead we rely on the hidden selectRouteInt()
                // method on these versions of the platform.
                // This limitation was removed in JB MR2.
                if (mSelectRouteIntMethod != null) {
                    try {
                        mSelectRouteIntMethod.invoke(router, types, route);
                        return; // success!
                    } catch (IllegalAccessException ex) {
                        Log.w(TAG, "Cannot programmatically select non-user route.  "
                                + "Media routing may not work.", ex);
                    } catch (InvocationTargetException ex) {
                        Log.w(TAG, "Cannot programmatically select non-user route.  "
                                + "Media routing may not work.", ex);
                    }
                } else {
                    Log.w(TAG, "Cannot programmatically select non-user route "
                            + "because the platform is missing the selectRouteInt() "
                            + "method.  Media routing may not work.");
                }
            }

            // Default handling.
            router.selectRoute(types, route);
        }
    }

    /**
     * Workaround the fact that the getDefaultRoute() method does not exist in JB and JB MR1.
     * Do not use on JB MR2 and above.
     */
    public static final class GetDefaultRouteWorkaround {
        private Method mGetSystemAudioRouteMethod;

        GetDefaultRouteWorkaround() {
            if (Build.VERSION.SDK_INT < 16 || Build.VERSION.SDK_INT > 17) {
                throw new UnsupportedOperationException();
            }
            try {
                mGetSystemAudioRouteMethod =
                        android.media.MediaRouter.class.getMethod("getSystemAudioRoute");
            } catch (NoSuchMethodException ex) {
            }
        }

        @SuppressLint("BanUncheckedReflection") // TODO: b/232075564
        @NonNull
        public Object getDefaultRoute(@NonNull Object routerObj) {
            android.media.MediaRouter router = (android.media.MediaRouter) routerObj;

            if (mGetSystemAudioRouteMethod != null) {
                try {
                    return mGetSystemAudioRouteMethod.invoke(router);
                } catch (IllegalAccessException ex) {
                } catch (InvocationTargetException ex) {
                }
            }

            // Could not find the method or it does not work.
            // Return the first route and hope for the best.
            return router.getRouteAt(0);
        }
    }

    static class CallbackProxy<T extends Callback>
            extends android.media.MediaRouter.Callback {
        protected final T mCallback;

        CallbackProxy(T callback) {
            mCallback = callback;
        }

        @Override
        public void onRouteSelected(android.media.MediaRouter router,
                int type, android.media.MediaRouter.RouteInfo route) {
            mCallback.onRouteSelected(type, route);
        }

        @Override
        public void onRouteUnselected(android.media.MediaRouter router,
                int type, android.media.MediaRouter.RouteInfo route) {
            mCallback.onRouteUnselected(type, route);
        }

        @Override
        public void onRouteAdded(android.media.MediaRouter router,
                android.media.MediaRouter.RouteInfo route) {
            mCallback.onRouteAdded(route);
        }

        @Override
        public void onRouteRemoved(android.media.MediaRouter router,
                android.media.MediaRouter.RouteInfo route) {
            mCallback.onRouteRemoved(route);
        }

        @Override
        public void onRouteChanged(android.media.MediaRouter router,
                android.media.MediaRouter.RouteInfo route) {
            mCallback.onRouteChanged(route);
        }

        @Override
        public void onRouteGrouped(android.media.MediaRouter router,
                android.media.MediaRouter.RouteInfo route,
                android.media.MediaRouter.RouteGroup group, int index) {
            mCallback.onRouteGrouped(route, group, index);
        }

        @Override
        public void onRouteUngrouped(android.media.MediaRouter router,
                android.media.MediaRouter.RouteInfo route,
                android.media.MediaRouter.RouteGroup group) {
            mCallback.onRouteUngrouped(route, group);
        }

        @Override
        public void onRouteVolumeChanged(android.media.MediaRouter router,
                android.media.MediaRouter.RouteInfo route) {
            mCallback.onRouteVolumeChanged(route);
        }
    }

    static class VolumeCallbackProxy<T extends VolumeCallback>
            extends android.media.MediaRouter.VolumeCallback {
        protected final T mCallback;

        VolumeCallbackProxy(T callback) {
            mCallback = callback;
        }

        @Override
        public void onVolumeSetRequest(android.media.MediaRouter.RouteInfo route,
                int volume) {
            mCallback.onVolumeSetRequest(route, volume);
        }

        @Override
        public void onVolumeUpdateRequest(android.media.MediaRouter.RouteInfo route,
                int direction) {
            mCallback.onVolumeUpdateRequest(route, direction);
        }
    }

    private MediaRouterJellybean() {
    }
}
