package io.github.jspinak.brobot.tools.logging.adapter;

import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter.OutputLevel;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Aspect that intercepts ConsoleReporter calls and routes them through the unified logging system.
 * This provides automatic integration without requiring changes to existing code.
 * 
 * <p>Enable this aspect by setting:</p>
 * <pre>
 * brobot.logging.console.intercept-legacy=true
 * </pre>
 * 
 * @see ConsoleReporter for the legacy API being intercepted
 * @see ConsoleReporterAdapter for the routing logic
 */
@Aspect
@Component
@ConditionalOnProperty(
    prefix = "brobot.logging.console",
    name = "intercept-legacy",
    havingValue = "true",
    matchIfMissing = true
)
@Slf4j
public class ConsoleReporterAspect {
    
    // Pointcuts for all ConsoleReporter static methods
    
    @Pointcut("execution(* io.github.jspinak.brobot.tools.logging.ConsoleReporter.print(String)) && args(str)")
    public void printString(String str) {}
    
    @Pointcut("execution(* io.github.jspinak.brobot.tools.logging.ConsoleReporter.print(io.github.jspinak.brobot.tools.logging.ConsoleReporter.OutputLevel, String)) && args(level, str)")
    public void printLevelString(OutputLevel level, String str) {}
    
    @Pointcut("execution(* io.github.jspinak.brobot.tools.logging.ConsoleReporter.println(String)) && args(str)")
    public void printlnString(String str) {}
    
    @Pointcut("execution(* io.github.jspinak.brobot.tools.logging.ConsoleReporter.println(io.github.jspinak.brobot.tools.logging.ConsoleReporter.OutputLevel, String)) && args(level, str)")
    public void printlnLevelString(OutputLevel level, String str) {}
    
    @Pointcut("execution(* io.github.jspinak.brobot.tools.logging.ConsoleReporter.format(String, Object...)) && args(format, arguments)")
    public void formatString(String format, Object[] arguments) {}
    
    @Pointcut("execution(* io.github.jspinak.brobot.tools.logging.ConsoleReporter.format(io.github.jspinak.brobot.tools.logging.ConsoleReporter.OutputLevel, String, Object...)) && args(level, format, arguments)")
    public void formatLevelString(OutputLevel level, String format, Object[] arguments) {}
    
    // Advice methods that intercept and redirect
    
    @Around("printString(str)")
    public Object interceptPrint(ProceedingJoinPoint joinPoint, String str) throws Throwable {
        log.trace("Intercepting ConsoleReporter.print(\"{}\")", str);
        return ConsoleReporterAdapter.routeToUnified(str, OutputLevel.HIGH);
    }
    
    @Around("printLevelString(level, str)")
    public Object interceptPrintLevel(ProceedingJoinPoint joinPoint, OutputLevel level, String str) throws Throwable {
        log.trace("Intercepting ConsoleReporter.print({}, \"{}\")", level, str);
        return ConsoleReporterAdapter.routeToUnified(str, level);
    }
    
    @Around("printlnString(str)")
    public Object interceptPrintln(ProceedingJoinPoint joinPoint, String str) throws Throwable {
        log.trace("Intercepting ConsoleReporter.println(\"{}\")", str);
        return ConsoleReporterAdapter.println(str, OutputLevel.HIGH);
    }
    
    @Around("printlnLevelString(level, str)")
    public Object interceptPrintlnLevel(ProceedingJoinPoint joinPoint, OutputLevel level, String str) throws Throwable {
        log.trace("Intercepting ConsoleReporter.println({}, \"{}\")", level, str);
        return ConsoleReporterAdapter.println(str, level);
    }
    
    @Around("formatString(format, arguments)")
    public Object interceptFormat(ProceedingJoinPoint joinPoint, String format, Object[] arguments) throws Throwable {
        log.trace("Intercepting ConsoleReporter.format(\"{}\", args)", format);
        return ConsoleReporterAdapter.format(format, OutputLevel.HIGH, arguments);
    }
    
    @Around("formatLevelString(level, format, arguments)")
    public Object interceptFormatLevel(ProceedingJoinPoint joinPoint, OutputLevel level, String format, Object[] arguments) throws Throwable {
        log.trace("Intercepting ConsoleReporter.format({}, \"{}\", args)", level, format);
        return ConsoleReporterAdapter.format(format, level, arguments);
    }
}