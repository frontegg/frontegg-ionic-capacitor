package com.frontegg.ionic;

import static org.junit.Assert.assertEquals;

import com.frontegg.android.AdminPortalLayout;
import org.junit.Test;

public class AdminPortalLayoutTest {

    @Test
    public void responsiveValueOpensTheResponsiveLayout() {
        assertEquals(AdminPortalLayout.RESPONSIVE, FronteggNativePlugin.adminPortalLayoutFrom("responsive"));
    }

    @Test
    public void desktopMissingOrUnknownValueOpensTheDesktopLayout() {
        assertEquals(AdminPortalLayout.DESKTOP, FronteggNativePlugin.adminPortalLayoutFrom("desktop"));
        assertEquals(AdminPortalLayout.DESKTOP, FronteggNativePlugin.adminPortalLayoutFrom(null));
        assertEquals(AdminPortalLayout.DESKTOP, FronteggNativePlugin.adminPortalLayoutFrom("tablet"));
    }
}
