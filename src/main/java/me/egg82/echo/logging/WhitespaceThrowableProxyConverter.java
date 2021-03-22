package me.egg82.echo.logging;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;

public class WhitespaceThrowableProxyConverter extends ThrowableProxyConverter {
    @Override
    protected final String throwableProxyToString(IThrowableProxy tp) { return CoreConstants.LINE_SEPARATOR + super.throwableProxyToString(tp) + CoreConstants.LINE_SEPARATOR; }
}
