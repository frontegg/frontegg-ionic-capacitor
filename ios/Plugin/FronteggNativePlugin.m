#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(FronteggNativePlugin, "FronteggNative",
           CAP_PLUGIN_METHOD(getAuthState, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getConstants, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(login, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(logout, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(switchTenant, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(refreshToken, CAPPluginReturnPromise);
)


