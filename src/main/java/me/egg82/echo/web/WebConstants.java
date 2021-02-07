package me.egg82.echo.web;

import java.util.concurrent.TimeUnit;
import me.egg82.echo.utils.TimeUtil;

public class WebConstants {
    private WebConstants() { }

    public static final TimeUtil.Time CONNECT_TIMEOUT = new TimeUtil.Time(15L, TimeUnit.SECONDS);
    public static final TimeUtil.Time READ_TIMEOUT = new TimeUtil.Time(25L, TimeUnit.SECONDS);
    public static final String USER_AGENT = "egg82/ECHO";
}
