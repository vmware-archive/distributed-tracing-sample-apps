// 2019. Mahesh Voleti (mvoleti@vmware.com)

package internal

import (
	"io"
	"log"
	"net/http"
	"os"

	opentracing "github.com/opentracing/opentracing-go"
	otrext "github.com/opentracing/opentracing-go/ext"
	jaeger "github.com/uber/jaeger-client-go"
	jaegercfg "github.com/uber/jaeger-client-go/config"
	jaegerlog "github.com/uber/jaeger-client-go/log"
	jmetrics "github.com/uber/jaeger-lib/metrics"
)

func NewGlobalTracer(serviceName string) io.Closer {

	config, enverr := jaegercfg.FromEnv()
	if enverr != nil {
		log.Println("Couldn't parse Jaeger env vars", enverr.Error())
		os.Exit(1)
	}

	config.ServiceName = serviceName
	config.Sampler.Type = jaeger.SamplerTypeConst
	config.Sampler.Param = 1
	config.Reporter.LogSpans = true

	if GlobalConfig.JaegerHostPort != "" {
		config.Reporter.LocalAgentHostPort = GlobalConfig.JaegerHostPort
	}

	log.Printf("Connecting to Jaeger @ %s\n", config.Reporter.LocalAgentHostPort)

	closer, err := config.InitGlobalTracer(
		serviceName,
		jaegercfg.Logger(jaegerlog.StdLogger),
		jaegercfg.Metrics(jmetrics.NullFactory),
	)
	if err != nil {
		log.Println("Couldn't initialize tracer", err.Error())
		os.Exit(1)
	}

	return closer
}

func NewServerSpan(req *http.Request, spanName string) opentracing.Span {
	tracer := opentracing.GlobalTracer()
	parentCtx, err := tracer.Extract(opentracing.HTTPHeaders, opentracing.HTTPHeadersCarrier(req.Header))
	var span opentracing.Span
	if err == nil { // has parent context
		span = tracer.StartSpan(spanName, opentracing.ChildOf(parentCtx))
	} else if err == opentracing.ErrSpanContextNotFound { // no parent
		span = tracer.StartSpan(spanName)
	} else {
		log.Printf("Error in extracting tracer context: %s", err.Error())
	}

	otrext.SpanKindRPCServer.Set(span)

	return span
}
