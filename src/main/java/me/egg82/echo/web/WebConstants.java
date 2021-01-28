package me.egg82.echo.web;

import java.util.concurrent.TimeUnit;
import me.egg82.echo.utils.TimeUtil;

public class WebConstants {
    private WebConstants() { }

    public static final TimeUtil.Time TIMEOUT = new TimeUtil.Time(5L, TimeUnit.SECONDS);
    public static final String USER_AGENT = "egg82/ECHO";
}
