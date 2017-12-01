// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space
// Source File Name:   SimpleIdGenerator.java

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.sys.IdGenerator;

public class SimpleIdGenerator
    implements IdGenerator
{
    private static final String PREFIX = "zk_comp_";
    private static final String INDEX_KEY = "Id_Num";
    public SimpleIdGenerator()
    {
    }
    public String nextComponentUuid(final Desktop desktop, final Component comp)
    {
        int i = Integer.parseInt(desktop.getAttribute(INDEX_KEY).toString());
        i++;
        desktop.setAttribute(INDEX_KEY, String.valueOf(i));
        return (new StringBuilder(PREFIX)).append(i).toString();
    }
    public String nextComponentUuid(final Desktop desktop, final Component comp, final ComponentInfo info)
    {
        return nextComponentUuid(desktop, comp);
    }
    public String nextDesktopId(final Desktop desktop)
    {
        System.out.println("new Desktop");
        if (desktop.getAttribute(INDEX_KEY) == null) {
            desktop.setAttribute(INDEX_KEY, "0");
        }
        return null;
    }
    public String nextPageUuid(final Page page)
    {
        return null;
    }
}
