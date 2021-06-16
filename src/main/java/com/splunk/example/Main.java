package com.splunk.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    final InputStream inputStream = Main.class.getResourceAsStream("/logging.properties");
    try {
      LogManager.getLogManager().readConfiguration(inputStream);
    } catch (final IOException e) {
      Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
      Logger.getAnonymousLogger().severe(e.getMessage());
    }

    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(SimpleSpanProcessor.create(OtlpGrpcSpanExporter.builder().setEndpoint("https://ingest.us0.signalfx.com/v2/trace/otlp").build()))
        .build();

    OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(sdkTracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .buildAndRegisterGlobal();

    Tracer tracer =
        openTelemetry.getTracer("instrumentation-library-name", "1.0.0");

    Span span = tracer.spanBuilder("my span").startSpan();
    span.end();

    TimeUnit.SECONDS.sleep(10);
  }
}
