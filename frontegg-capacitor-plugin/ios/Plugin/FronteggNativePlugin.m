#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(FronteggNativePlugin, "FronteggNative",
           CAP_PLUGIN_METHOD(subscribe, CAPPluginReturnNone);
           CAP_PLUGIN_METHOD(logout, CAPPluginReturnNone);
           CAP_PLUGIN_METHOD(login, CAPPluginReturnNone);
           CAP_PLUGIN_METHOD(getConstants, CAPPluginReturnNone);
)

