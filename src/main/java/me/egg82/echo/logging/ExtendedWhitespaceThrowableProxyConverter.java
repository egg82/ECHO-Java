package me.egg82.echo.logging;

import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;

public class ExtendedWhitespaceThrowableProxyConverter extends ExtendedThrowableProxyConverter {
    protected final String throwableProxyToString(IThrowableProxy tp) { return CoreConstants.LINE_SEPARATOR + super.throwableProxyToString(tp) + CoreConstants.LINE_SEPARATOR; }
}
